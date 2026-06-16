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
