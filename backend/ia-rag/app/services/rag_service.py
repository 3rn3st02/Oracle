import json
import re
import unicodedata
from pathlib import Path

from groq import Groq

from app.core.config import get_settings

TOP_SECTIONS = 3


class RagService:
    def __init__(self) -> None:
        self.base_path = Path(__file__).resolve().parents[2]
        self.processed_path = self.base_path / "data" / "processed"
        self.metadata_path = self.base_path / "data" / "books_metadata.json"
        settings = get_settings()
        self._groq = Groq(api_key=settings.groq_api_key) if settings.groq_api_key else None

    def is_initialized(self) -> bool:
        return True

    def load_books_metadata(self) -> list:
        if not self.metadata_path.exists():
            return []
        return json.loads(self.metadata_path.read_text(encoding="utf-8"))

    def load_book_text(self, filename: str) -> str:
        file_path = self.processed_path / filename
        if not file_path.exists():
            return ""
        return file_path.read_text(encoding="utf-8", errors="ignore")

    def strip_front_matter(self, text: str) -> str:
        markers = ["1. Introducción", "1. Introduccion"]
        for marker in markers:
            first = text.find(marker)
            if first == -1:
                continue
            second = text.find(marker, first + len(marker))
            if second != -1:
                return text[second:]
            return text[first:]
        return text

    def normalize_text(self, text: str) -> str:
        text = self.strip_front_matter(text)
        text = text.replace("\x0c", "\n")
        text = re.sub(r"[ \t]+", " ", text)
        text = re.sub(r"\n{2,}", "\n\n", text)
        return text.strip()

    def split_into_sections(self, text: str) -> list:
        text = self.normalize_text(text)
        if not text:
            return []

        lines = [line.strip() for line in text.splitlines() if line.strip()]
        sections = []
        current = []
        heading_pattern = re.compile(r"^\d+(\.\d+)*\.?\s+\S")

        for line in lines:
            if heading_pattern.match(line) and current:
                sections.append(" ".join(current).strip())
                current = [line]
            else:
                current.append(line)

        if current:
            sections.append(" ".join(current).strip())

        return sections

    @staticmethod
    def _to_ascii(text: str) -> str:
        return unicodedata.normalize("NFKD", text).encode("ascii", "ignore").decode("ascii")

    def extract_keywords(self, question: str) -> list:
        stopwords = {
            "que", "dice", "libro", "antiguo", "nuevo", "sobre",
            "del", "los", "las", "una", "uno", "unos", "unas", "para",
            "como", "cual", "cuales", "cuando", "donde",
            "el", "la", "de", "en", "por", "con", "sin", "al", "se",
            "es", "son", "un",
        }
        normalized = self._to_ascii(question.lower())
        words = re.findall(r"\w+", normalized)
        keywords = [w for w in words if len(w) >= 3 and w not in stopwords]

        seen = set()
        unique = []
        for w in keywords:
            if w not in seen:
                seen.add(w)
                unique.append(w)
        return unique

    def is_noise_section(self, section: str) -> bool:
        if len(section.strip()) < 80:
            return True
        if sum(ch.isalpha() for ch in section) < 40:
            return True
        return False

    def score_section(self, keywords: list, section: str) -> int:
        section_ascii = self._to_ascii(section.lower())

        heading_match = re.match(r"^[\d.]+\s+(.+?)(?:\s{2}|\Z)", section_ascii)
        heading = heading_match.group(1) if heading_match else ""

        score = 0
        for kw in keywords:
            count = section_ascii.count(kw)
            if count > 0:
                score += 2 + min(count - 1, 3)
            if kw in heading:
                score += 3
        return score

    def remove_noise_markers(self, text: str) -> str:
        text_lower = text.lower()
        for marker in [
            "caso práctico inicial",
            "caso practico inicial",
            "práctica profesional",
            "ficha de trabajo",
            "situación de partida",
        ]:
            pos = text_lower.find(marker)
            if pos != -1:
                return text[:pos].strip()
        return text

    def retrieve_context(self, question: str) -> list[dict]:
        books = self.load_books_metadata()
        keywords = self.extract_keywords(question)

        if not keywords:
            return []

        scored = []

        for book in books:
            text = self.load_book_text(book["filename"])
            if not text:
                continue

            sections = self.split_into_sections(text)

            for section in sections:
                if self.is_noise_section(section):
                    continue

                score = self.score_section(keywords, section)
                if score >= 2:
                    scored.append({"chunk": section, "book": book, "score": score})

        if not scored:
            return []

        scored.sort(key=lambda x: x["score"], reverse=True)
        return scored[:TOP_SECTIONS]

    def generate_answer(self, question: str, matches: list[dict]) -> str:
        if not matches:
            return (
                f"No he encontrado suficiente información relevante en los libros "
                f"para responder con claridad a la pregunta: '{question}'."
            )

        if not self._groq:
            return (
                "El servicio de IA no está configurado. "
                "Añade GROQ_API_KEY al archivo .env para activarlo."
            )

        chunks = []
        for i, m in enumerate(matches, 1):
            chunk = self.remove_noise_markers(m["chunk"].strip())
            chunk = re.sub(r"\s+", " ", chunk).strip()
            chunks.append(f"[Fragmento {i}]\n{chunk}")

        context = "\n\n".join(chunks)

        system_prompt = (
            "Eres un asistente educativo especializado en informática y electrónica. "
            "Responde la pregunta del usuario basándote ÚNICAMENTE en los fragmentos del libro que se te proporcionan. "
            "Si la información necesaria no está en los fragmentos, indícalo con claridad. "
            "Responde en español, de forma concisa y clara."
        )

        user_message = (
            f"Fragmentos del libro:\n\"\"\"\n{context}\n\"\"\"\n\n"
            f"Pregunta: {question}"
        )

        response = self._groq.chat.completions.create(
            model="llama-3.3-70b-versatile",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_message},
            ],
            temperature=0.2,
            max_tokens=1024,
        )

        return response.choices[0].message.content.strip()

    def ask(self, question: str, context: list[str] | None = None, user_id: str | None = None) -> dict:
        matches = self.retrieve_context(question)
        answer = self.generate_answer(question, matches)

        seen = set()
        sources = []
        for m in matches:
            key = m["book"]["filename"]
            if key not in seen:
                seen.add(key)
                sources.append({
                    "source": m["book"]["filename"],
                    "label": m["book"]["label"],
                    "version": m["book"]["version"],
                })

        return {"answer": answer, "sources": sources}


rag_service = RagService()
