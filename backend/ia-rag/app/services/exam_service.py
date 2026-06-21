import json
import re
from typing import Optional

from groq import Groq

from app.core.config import get_settings


_EXAM_SYSTEM = (
    "Eres un generador de preguntas de examen para el módulo "
    "'Montaje y Mantenimiento de Sistemas Microinformáticos'. "
    "Genera exactamente 5 preguntas tipo test basadas SOLO en el texto proporcionado. "
    "Cada pregunta tiene 4 opciones (A, B, C, D), solo una correcta. "
    "Devuelve ÚNICAMENTE un JSON válido sin texto adicional:\n"
    '{"questions":[{"question":"...","options":{"A":"...","B":"...","C":"...","D":"..."},'
    '"correct":"A","explanation":"..."}]}'
)


class ExamService:
    def __init__(self) -> None:
        settings = get_settings()
        self._groq = Groq(api_key=settings.groq_api_key) if settings.groq_api_key else None

    def generate(self, text_excerpt: str, unit_label: str) -> dict:
        if not self._groq:
            return {"error": "Servicio de IA no configurado."}

        excerpt = text_excerpt[:2500].strip()
        try:
            resp = self._groq.chat.completions.create(
                model="llama-3.1-8b-instant",
                messages=[
                    {"role": "system", "content": _EXAM_SYSTEM},
                    {"role": "user", "content": f"Unidad: {unit_label}\n\nTexto:\n{excerpt}"},
                ],
                temperature=0.4,
                max_tokens=900,
                timeout=30,
            )
            raw = resp.choices[0].message.content.strip()
            m = re.search(r'\{.*\}', raw, re.DOTALL)
            if not m:
                return {"error": "No se pudo generar el examen. Inténtalo de nuevo."}
            data = json.loads(m.group(0))
            questions = data.get("questions", [])
            return {"unit": unit_label, "questions": questions, "count": len(questions)}
        except json.JSONDecodeError:
            return {"error": "Error al procesar las preguntas generadas. Inténtalo de nuevo."}
        except Exception as e:
            import logging
            logging.getLogger(__name__).error("ExamService.generate error: %s", e)
            return {"error": "El servicio de IA no está disponible en este momento."}

    def check(
        self,
        user_answer: str,
        correct_answer: str,
        explanation: Optional[str] = None,
    ) -> dict:
        user_norm = user_answer.strip().upper()
        correct_norm = correct_answer.strip().upper()
        correct = user_norm == correct_norm
        message = (
            f"¡Correcto! 🎉 La respuesta es **{correct_norm}**."
            if correct
            else f"Incorrecto. 😕 La respuesta correcta era **{correct_norm}**."
        )
        return {
            "correct": correct,
            "user_answer": user_norm,
            "correct_answer": correct_norm,
            "message": message,
            "explanation": explanation or "",
        }


exam_service = ExamService()
