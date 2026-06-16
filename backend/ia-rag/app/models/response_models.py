from typing import Any, Dict, List, Optional

from pydantic import BaseModel, Field


class ErrorResponse(BaseModel):
    code: str = Field(..., description="Código de error estable")
    message: str = Field(..., description="Mensaje legible del error")
    details: Dict[str, Any] = Field(default_factory=dict, description="Detalles adicionales")


class AskResponse(BaseModel):
    answer: Optional[str] = Field(default=None, description="Respuesta generada")
    sources: List[dict] = Field(default_factory=list, description="Fuentes o referencias")
    status: str = Field(..., description="ok o error")
    error: Optional[ErrorResponse] = Field(default=None, description="Error si aplica")
    request_id: str = Field(..., description="Identificador único de request")
    latency_ms: int = Field(..., description="Tiempo de respuesta en milisegundos")


class HealthResponse(BaseModel):
    status: str
    service: str
    version: str
    initialized: bool
