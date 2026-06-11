from typing import List, Optional

from pydantic import BaseModel, Field


class AskRequest(BaseModel):
    question: str = Field(..., min_length=1, description="Pregunta del usuario")
    user_id: Optional[str] = Field(default=None, description="Identificador opcional del usuario")
    context: List[str] = Field(default_factory=list, description="Contexto adicional opcional")

