from typing import Any, Optional

from pydantic import BaseModel, Field


class BaseResponse(BaseModel):
    status: str = Field(..., description="'ok' o 'error'")
    error: Optional[str] = Field(default=None)
    data: Any = Field(default=None)


class AskResponse(BaseResponse):
    """data: {answer, sources, request_id, latency_ms, related_question, from_cache}"""


class HealthResponse(BaseResponse):
    """data: {service, version, docs_count, rag_has_content, groq_configured, cache_size, questions_today, questions_total}"""


class BooksResponse(BaseResponse):
    """data: [{filename, label, version, uploaded_at}]"""


class UploadResponse(BaseResponse):
    """data: {filename, label}"""


class DeleteResponse(BaseResponse):
    """data: {deleted}"""


class FeedbackResponse(BaseResponse):
    """data: {recorded, message}"""


class StatsResponse(BaseResponse):
    """data: {total, positive, negative, top_questions, today}"""


class ResumenResponse(BaseResponse):
    """data: {unidad, resumen, label}"""


class HistoryResponse(BaseResponse):
    """data: sessions dict or message list"""


class ExamGenerateResponse(BaseResponse):
    """data: {unit, questions, count}"""


class ExamCheckResponse(BaseResponse):
    """data: {correct, user_answer, correct_answer, message, explanation}"""


class AdminStatsResponse(BaseResponse):
    """data: {questions_total, questions_today, active_users, usage_by_unit, usage_pct_by_unit}"""


class AdminConversationsResponse(BaseResponse):
    """data: full history dict"""


class AdminFeedbackResponse(BaseResponse):
    """data: {total, positive, negative, entries}"""
