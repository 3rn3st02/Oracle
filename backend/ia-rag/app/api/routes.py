import json
import os
import time
import uuid
from collections import defaultdict
from datetime import datetime, timedelta, timezone
from pathlib import Path

from fastapi import APIRouter, Depends, File, HTTPException, Request, UploadFile, status
from fastapi.responses import StreamingResponse

from app.core.config import get_settings
from app.models.request_models import AskRequest, FeedbackRequest, ResumenRequest
from app.models.response_models import (
    AskResponse,
    BooksResponse,
    DeleteResponse,
    FeedbackResponse,
    HealthResponse,
    ResumenResponse,
    StatsResponse,
    UploadResponse,
)
from app.services.cache_service import cache_service
from app.services.feedback_service import feedback_service
from app.services.ingestion_service import MAX_FILE_SIZE_BYTES, ingestion_service
from app.services.rag_service import NO_CONTEXT_REPLY, rag_service

router = APIRouter()
settings = get_settings()

_ALLOWED_EXTENSIONS = {".pdf", ".docx"}

# ── Rate limiting (10 req/min per IP) ────────────────────────────────────────

_rate_buckets: dict[str, list] = defaultdict(list)


def _check_rate_limit(ip: str, max_per_minute: int = 10) -> bool:
    now = datetime.now(timezone.utc)
    window = now - timedelta(minutes=1)
    _rate_buckets[ip] = [t for t in _rate_buckets[ip] if t > window]
    if len(_rate_buckets[ip]) >= max_per_minute:
        return False
    _rate_buckets[ip].append(now)
    return True


def _rate_limit(request: Request):
    ip = request.client.host if request.client else "unknown"
    if not _check_rate_limit(ip):
        raise HTTPException(
            status_code=status.HTTP_429_TOO_MANY_REQUESTS,
            detail="Demasiadas peticiones. Espera un momento antes de volver a preguntar. ⏳",
        )


# ── API key protection ────────────────────────────────────────────────────────

def _require_api_key(request: Request):
    if not settings.upload_api_key:
        return
    key = request.headers.get("X-API-Key", "")
    if key != settings.upload_api_key:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="API key inválida o ausente.",
        )


# ── Helpers ───────────────────────────────────────────────────────────────────

def _ok(data) -> dict:
    return {"status": "ok", "error": None, "data": data}


# ── Endpoints ─────────────────────────────────────────────────────────────────

@router.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    from app.services.cache_service import cache_service as cs
    count = rag_service.docs_count()
    groq_ok = rag_service._groq is not None
    stats = feedback_service.stats()
    return HealthResponse(**_ok({
        "service": settings.app_name,
        "version": settings.app_version,
        "docs_count": count,
        "rag_has_content": count > 0,
        "groq_configured": groq_ok,
        "cache_size": cs.size(),
        "questions_today": stats["today"],
        "questions_total": stats["total"],
    }))


@router.get("/debug")
def debug(request: Request):
    if settings.production:
        raise HTTPException(status_code=status.HTTP_404_NOT_FOUND, detail="Not found")
    svc = rag_service
    processed = svc.processed_path
    books = svc.load_books_metadata()
    files_in_processed = list(processed.iterdir()) if processed.exists() else []
    book_info = []
    for book in books:
        text = svc.load_book_text(book["filename"])
        sections = svc.split_into_sections(text) if text else []
        book_info.append({
            "filename": book["filename"],
            "file_exists": (processed / book["filename"]).exists(),
            "chars_loaded": len(text),
            "sections": len(sections),
        })
    return _ok({
        "cwd": os.getcwd(),
        "base_path": str(svc.base_path),
        "processed_path": str(processed),
        "processed_exists": processed.exists(),
        "metadata_exists": svc.metadata_path.exists(),
        "files_in_processed": [f.name for f in files_in_processed],
        "books_metadata": books,
        "books_detail": book_info,
    })


@router.get("/books", response_model=BooksResponse)
def list_books() -> BooksResponse:
    books = rag_service.load_books_metadata()
    result = []
    for b in books:
        txt_path = rag_service.processed_path / b["filename"]
        uploaded_at = (
            datetime.fromtimestamp(txt_path.stat().st_mtime).isoformat()
            if txt_path.exists() else None
        )
        result.append({
            "filename": b["filename"],
            "label": b["label"],
            "version": b["version"],
            "uploaded_at": uploaded_at,
        })
    return BooksResponse(**_ok(result))


@router.delete("/books/{filename}", response_model=DeleteResponse, dependencies=[Depends(_require_api_key)])
def delete_book(filename: str) -> DeleteResponse:
    books = rag_service.load_books_metadata()
    txt_filename = filename if filename.endswith(".txt") else Path(filename).stem + ".txt"
    if not any(b["filename"] == txt_filename for b in books):
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"No se encontró el libro '{filename}'",
        )
    ingestion_service.remove_book(txt_filename)
    return DeleteResponse(**_ok({"deleted": txt_filename}))


@router.post("/upload", response_model=UploadResponse, status_code=status.HTTP_201_CREATED,
             dependencies=[Depends(_require_api_key)])
async def upload_book(file: UploadFile = File(...)) -> UploadResponse:
    suffix = Path(file.filename).suffix.lower()
    if suffix not in _ALLOWED_EXTENSIONS:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"Formato no permitido '{suffix}'. Solo se aceptan: {', '.join(sorted(_ALLOWED_EXTENSIONS))}",
        )

    content = await file.read()
    if len(content) > MAX_FILE_SIZE_BYTES:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=f"El archivo supera el límite de {MAX_FILE_SIZE_BYTES // (1024 * 1024)} MB",
        )

    dest = ingestion_service.books_path / file.filename
    dest.write_bytes(content)

    try:
        ingestion_service.ingest_file(file.filename)
    except Exception as e:
        dest.unlink(missing_ok=True)
        raise HTTPException(status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
                            detail=f"Error al procesar el archivo: {e}")

    label = Path(file.filename).stem
    ingestion_service.update_metadata(file.filename, label)
    return UploadResponse(**_ok({"filename": file.filename, "label": label}))


@router.post("/ask", response_model=AskResponse, dependencies=[Depends(_rate_limit)])
def ask(payload: AskRequest) -> AskResponse:
    start = time.perf_counter()
    request_id = str(uuid.uuid4())

    result = rag_service.ask(
        question=payload.question,
        context=payload.context,
        user_id=payload.user_id,
    )

    latency_ms = int((time.perf_counter() - start) * 1000)

    return AskResponse(**_ok({
        "answer": result["answer"],
        "sources": result["sources"],
        "related_question": result.get("related_question"),
        "from_cache": result.get("from_cache", False),
        "request_id": request_id,
        "latency_ms": latency_ms,
    }))


@router.post("/ask/stream", dependencies=[Depends(_rate_limit)])
async def ask_stream(payload: AskRequest):
    request_id = str(uuid.uuid4())

    async def generate():
        # Hardcoded / calculator (no streaming needed)
        special = rag_service.detect_special_question(payload.question)
        if special:
            yield f"data: {json.dumps({'token': special['answer'], 'done': False})}\n\n"
            yield f"data: {json.dumps({'token': '', 'done': True, 'request_id': request_id})}\n\n"
            return

        from app.services.calculator_service import calculator_service
        calc = calculator_service.calculate(payload.question)
        if calc:
            yield f"data: {json.dumps({'token': calc['answer'], 'done': False})}\n\n"
            yield f"data: {json.dumps({'token': '', 'done': True, 'request_id': request_id})}\n\n"
            return

        # Cache
        cached = cache_service.get(payload.question)
        if cached:
            yield f"data: {json.dumps({'token': cached['answer'], 'done': False, 'from_cache': True})}\n\n"
            yield f"data: {json.dumps({'token': '', 'done': True, 'request_id': request_id})}\n\n"
            return

        matches = rag_service.retrieve_context(payload.question)
        if not matches:
            yield f"data: {json.dumps({'token': NO_CONTEXT_REPLY, 'done': False})}\n\n"
            yield f"data: {json.dumps({'token': '', 'done': True, 'request_id': request_id})}\n\n"
            return

        sources = []
        seen: set[str] = set()
        for m in matches:
            key = m["book"]["filename"]
            if key not in seen:
                seen.add(key)
                sources.append({"source": key, "label": m["book"]["label"], "version": m["book"]["version"]})

        async for token in rag_service.stream_answer(payload.question, matches):
            yield f"data: {json.dumps({'token': token, 'done': False})}\n\n"

        yield f"data: {json.dumps({'token': '', 'done': True, 'sources': sources, 'request_id': request_id})}\n\n"

    return StreamingResponse(generate(), media_type="text/event-stream")


@router.post("/feedback", response_model=FeedbackResponse)
def add_feedback(payload: FeedbackRequest) -> FeedbackResponse:
    feedback_service.add(
        request_id=payload.request_id,
        useful=payload.useful,
        question=payload.question or "",
        answer=payload.answer or "",
    )
    return FeedbackResponse(**_ok({"recorded": True}))


@router.get("/feedback/stats", response_model=StatsResponse)
def feedback_stats() -> StatsResponse:
    return StatsResponse(**_ok(feedback_service.stats()))


@router.post("/ask/resumen", response_model=ResumenResponse, dependencies=[Depends(_rate_limit)])
def ask_resumen(payload: ResumenRequest) -> ResumenResponse:
    books = rag_service.load_books_metadata()
    target = f"Unidad {payload.unidad}"
    book = next((b for b in books if str(payload.unidad) in b["label"]), None)

    if not book:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail=f"No se encontró la Unidad {payload.unidad} en los documentos.",
        )

    text = rag_service.load_book_text(book["filename"])
    sections = rag_service.split_into_sections(text)
    # Take up to 8 section titles for the outline
    titles = [s["title"] for s in sections if s.get("heading") and not rag_service.is_noise_section(s)][:8]

    resumen = f"📚 **Resumen de {book['label']}**\n\n"
    resumen += "\n".join(f"- {t}" for t in titles) if titles else "No se encontraron secciones."

    return ResumenResponse(**_ok({"unidad": payload.unidad, "resumen": resumen, "label": book["label"]}))
