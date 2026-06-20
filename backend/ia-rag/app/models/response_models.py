from typing import Any, Optional

from pydantic import BaseModel, Field


class BaseResponse(BaseModel):
    status: str = Field(..., description="'ok' o 'error'")
    error: Optional[str] = Field(default=None, description="Mensaje de error si aplica")
    data: Any = Field(default=None, description="Datos de la respuesta")


class AskResponse(BaseResponse):
    """
    data: {answer, sources, request_id, latency_ms}
    """


class HealthResponse(BaseResponse):
    """
    data: {service, version, docs_count, rag_has_content}
    """


class BooksResponse(BaseResponse):
    """
    data: [{filename, label, version, uploaded_at}]
    """


class UploadResponse(BaseResponse):
    """
    data: {filename, label}
    """


class DeleteResponse(BaseResponse):
    """
    data: {deleted}
    """


class FeedbackResponse(BaseResponse):
    """
    data: {recorded: true}
    """


class StatsResponse(BaseResponse):
    """
    data: {total, positive, negative, top_questions, today}
    """


class ResumenResponse(BaseResponse):
    """
    data: {unidad, resumen}
    """
