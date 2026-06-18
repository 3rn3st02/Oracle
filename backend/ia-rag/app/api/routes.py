import os
import time
import uuid
from datetime import datetime
from pathlib import Path

from fastapi import APIRouter, File, HTTPException, UploadFile, status

from app.core.config import get_settings
from app.models.request_models import AskRequest
from app.models.response_models import (
    AskResponse,
    BooksResponse,
    DeleteResponse,
    HealthResponse,
    UploadResponse,
)
from app.services.ingestion_service import MAX_FILE_SIZE_BYTES, ingestion_service
from app.services.rag_service import rag_service

router = APIRouter()
settings = get_settings()

_ALLOWED_EXTENSIONS = {".pdf", ".docx"}


def _ok(data) -> dict:
    return {"status": "ok", "error": None, "data": data}


@router.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    count = rag_service.docs_count()
    return HealthResponse(**_ok({
        "service": settings.app_name,
        "version": settings.app_version,
        "docs_count": count,
        "rag_has_content": count > 0,
    }))


@router.get("/debug")
def debug():
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


@router.delete("/books/{filename}", response_model=DeleteResponse)
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


@router.post("/upload", response_model=UploadResponse, status_code=status.HTTP_201_CREATED)
async def upload_book(file: UploadFile = File(...)) -> UploadResponse:
    suffix = Path(file.filename).suffix.lower()
    if suffix not in _ALLOWED_EXTENSIONS:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=(
                f"Formato no permitido '{suffix}'. "
                f"Solo se aceptan: {', '.join(sorted(_ALLOWED_EXTENSIONS))}"
            ),
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
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Error al procesar el archivo: {e}",
        )

    label = Path(file.filename).stem
    ingestion_service.update_metadata(file.filename, label)

    return UploadResponse(**_ok({"filename": file.filename, "label": label}))


@router.post("/ask", response_model=AskResponse)
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
        "request_id": request_id,
        "latency_ms": latency_ms,
    }))
