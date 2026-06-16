import os
import time
import uuid

from fastapi import APIRouter, HTTPException, status

from app.core.config import get_settings
from app.models.request_models import AskRequest
from app.models.response_models import AskResponse, HealthResponse
from app.services.rag_service import rag_service

router = APIRouter()
settings = get_settings()


@router.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(
        status="ok",
        service=settings.app_name,
        version=settings.app_version,
        initialized=rag_service.is_initialized()
    )


@router.get("/debug")
def debug():
    svc = rag_service
    processed = svc.processed_path
    metadata = svc.metadata_path

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

    return {
        "cwd": os.getcwd(),
        "base_path": str(svc.base_path),
        "processed_path": str(processed),
        "processed_exists": processed.exists(),
        "metadata_path": str(metadata),
        "metadata_exists": metadata.exists(),
        "files_in_processed": [f.name for f in files_in_processed],
        "books_metadata": books,
        "books_detail": book_info,
    }


@router.post("/ask", response_model=AskResponse)
def ask(payload: AskRequest) -> AskResponse:
    start = time.perf_counter()
    request_id = str(uuid.uuid4())

    if not rag_service.is_initialized():
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail={
                "code": "BACKEND_NOT_INITIALIZED",
                "message": "El backend de IA aún no está inicializado.",
                "details": {}
            }
        )

    result = rag_service.ask(
        question=payload.question,
        context=payload.context,
        user_id=payload.user_id
    )

    latency_ms = int((time.perf_counter() - start) * 1000)

    return AskResponse(
        answer=result["answer"],
        sources=result["sources"],
        status="ok",
        error=None,
        request_id=request_id,
        latency_ms=latency_ms
    )
