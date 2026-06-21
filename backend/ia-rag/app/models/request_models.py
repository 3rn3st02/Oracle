from typing import List, Optional

from pydantic import BaseModel, Field


class AskRequest(BaseModel):
    question: str = Field(..., min_length=3, max_length=500)
    user_id: Optional[str] = Field(default=None)
    session_id: Optional[str] = Field(default=None)
    context: List[str] = Field(default_factory=list)


class FeedbackRequest(BaseModel):
    request_id: str = Field(..., description="ID de la respuesta valorada")
    useful: bool = Field(..., description="True si fue útil, False si no")
    question: Optional[str] = Field(default=None)
    answer: Optional[str] = Field(default=None)


class ResumenRequest(BaseModel):
    unidad: int = Field(..., ge=1, le=10, description="Número de unidad a resumir")


class ExamGenerateRequest(BaseModel):
    unidad: int = Field(..., ge=1, le=10, description="Número de unidad")


class ExamCheckRequest(BaseModel):
    question: str = Field(..., description="Pregunta del examen")
    user_answer: str = Field(..., description="Letra de la respuesta del alumno (A/B/C/D)")
    correct_answer: str = Field(..., description="Letra de la respuesta correcta")
    explanation: Optional[str] = Field(default=None, description="Explicación de la respuesta correcta")
