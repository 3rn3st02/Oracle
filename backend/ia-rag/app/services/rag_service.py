import json
import re
from pathlib import Path


class RagService:
    def __init__(self) -> None:
        self.base_path = Path(__file__).resolve().parents[2]
        self.processed_path = self.base_path / "data" / "processed"
        self.metadata_path = self.base_path / "data" / "books_metadata.json"

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
        """
        Intenta saltarse portada e índice buscando la segunda aparición de
        '1. Introducción', que normalmente marca el comienzo real del contenido.
        """
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

    def split_into_chunks(self, text: str, chunk_size: int = 1000) -> list:
        text = self.normalize_text(text)

        if not text:
            return []

        paragraphs = [p.strip() for p in text.split("\n\n") if p.strip()]
        chunks = []
        current = ""

        for paragraph in paragraphs:
            if len(current) + len(paragraph) + 2 <= chunk_size:
                current = f"{current}\n\n{paragraph}".strip()
            else:
                if current:
                    chunks.append(current)
                current = paragraph

        if current:
            chunks.append(current)

        return chunks

    def split_into_sections(self, text: str) -> list:
        text = self.normalize_text(text)

        if not text:
            return []

        lines = [line.strip() for line in text.splitlines() if line.strip()]
        sections = []
        current = []

        heading_pattern = re.compile(r"^\d+(\.\d+)?\.\s+")

        for line in lines:
            if heading_pattern.match(line) and current:
                sections.append(" ".join(current).strip())
                current = [line]
            else:
                current.append(line)

        if current:
            sections.append(" ".join(current).strip())

        return sections

    def extract_keywords(self, question: str) -> list:
        stopwords = {
            "que", "qué", "dice", "libro", "antiguo", "nuevo", "sobre",
            "del", "los", "las", "una", "uno", "unos", "unas", "para",
            "como", "cómo", "cual", "cuál", "cuáles", "cuando", "donde",
            "el", "la", "de", "en", "por", "con", "sin", "al", "se",
            "es", "son", "un", "cuales"
        }

        words = re.findall(r"\w+", question.lower())
        keywords = [word for word in words if len(word) > 3 and word not in stopwords]

        extras = [
            "montaje", "mantenimiento", "equipos", "sistemas", "circuito",
            "eléctrico", "electrico", "serie", "paralelo", "arquitectura",
            "neumann", "unidades", "funcionales", "ordenador", "ssd",
            "placa", "base", "memoria", "puertos", "audio", "vídeo", "video"
        ]

        question_lower = question.lower()
        for extra in extras:
            if extra in question_lower:
                keywords.append(extra)

        seen = set()
        unique_keywords = []
        for word in keywords:
            if word not in seen:
                seen.add(word)
                unique_keywords.append(word)

        return unique_keywords

    def is_noise_chunk(self, chunk: str) -> bool:
        chunk_lower = chunk.lower()

        noise_patterns = [
            "índice",
            "práctica profesional",
            "ficha de trabajo",
            "editex",
            "formación profesional básica",
            "informática y comunicaciones",
            "informática de oficina",
            "caso práctico inicial",
            "situación de partida",
            "pctown",
            "lorena trabaja",
            "descuento a los clientes"
        ]

        if len(chunk.strip()) < 80:
            return True

        if sum(char.isalpha() for char in chunk) < 40:
            return True

        if any(pattern in chunk_lower for pattern in noise_patterns):
            return True

        return False

    def score_chunk(self, keywords: list, chunk: str) -> int:
        chunk_lower = chunk.lower()
        score = 0

        for keyword in keywords:
            if keyword in chunk_lower:
                score += 2

        important_terms = [
            "montaje", "mantenimiento", "equipos", "sistemas", "circuito",
            "eléctrico", "serie", "paralelo", "arquitectura", "neumann",
            "funcionales", "ordenador", "ssd", "placa", "base"
        ]
        score += sum(1 for term in important_terms if term in chunk_lower)

        return score

    def extract_window_from_lines(self, lines: list, targets: list[str], window: int = 8) -> str:
        for i, line in enumerate(lines):
            line_lower = line.lower()
            if any(target in line_lower for target in targets):
                selected = []
                for j in range(i, min(len(lines), i + window)):
                    current = lines[j].strip()
                    if current:
                        selected.append(current)

                chunk = " ".join(selected).strip()
                if chunk:
                    return chunk

        return ""

    def retrieve_context(self, question: str) -> dict | None:
        books = self.load_books_metadata()
        keywords = self.extract_keywords(question)
        exact_question = question.lower()

        best_match = None
        best_score = 0

        for book in books:
            text = self.load_book_text(book["filename"])
            if not text:
                continue

            text = self.normalize_text(text)
            lines = [line.strip() for line in text.splitlines() if line.strip()]
            chunks = self.split_into_chunks(text)

            # Reglas exactas prioritarias
            if "circuito eléctrico" in exact_question or "circuito electrico" in exact_question:
                chunk = self.extract_window_from_lines(
                    lines,
                    ["un circuito eléctrico es", "un circuito electrico es"],
                    window=3
                )
                if chunk:
                    return {"chunk": chunk, "book": book}

            if "circuito en serie" in exact_question:
                chunk = self.extract_window_from_lines(lines, ["circuito en serie"], window=3)
                if chunk:
                    return {"chunk": chunk, "book": book}

            if "circuito en paralelo" in exact_question:
                chunk = self.extract_window_from_lines(lines, ["circuito en paralelo"], window=3)
                if chunk:
                    return {"chunk": chunk, "book": book}

            # PARCHE: Von Neumann con ventana amplia para que no se corte en "partes:"
            if "von neumann" in exact_question:
                chunk = self.extract_window_from_lines(
                    lines,
                    ["arquitectura de von neumann", "von neumann"],
                    window=28
                )
                if chunk and "índice" not in chunk.lower():
                    return {"chunk": chunk, "book": book}

            if "unidades funcionales" in exact_question:
                chunk = self.extract_window_from_lines(
                    lines,
                    ["unidades funcionales de un ordenador", "unidades funcionales"],
                    window=12
                )
                if chunk and "índice" not in chunk.lower():
                    return {"chunk": chunk, "book": book}

            if "placa base" in exact_question:
                chunk = self.extract_window_from_lines(lines, ["placa base"], window=10)
                if chunk and "caso práctico inicial" not in chunk.lower():
                    return {"chunk": chunk, "book": book}

            # Búsqueda general por puntuación
            for chunk in chunks:
                if self.is_noise_chunk(chunk):
                    continue

                score = self.score_chunk(keywords, chunk)

                if score > best_score:
                    best_score = score
                    best_match = {
                        "chunk": chunk,
                        "book": book
                    }

        if best_score < 2:
            return None

        return best_match

    def generate_answer(self, question: str, match: dict | None) -> str:
        if not match:
            return (
                f"No he encontrado todavía suficiente información relevante en los libros "
                f"para responder con claridad a la pregunta: '{question}'."
            )

        chunk = match["chunk"].strip()

        # Limpieza visual mínima
        chunk = chunk.replace("= ", "")
        chunk = re.sub(r"\s+", " ", chunk).strip()
        chunk = re.sub(r"^\d+\.\s*", "", chunk)
        chunk = chunk.strip(" .:")

        if not chunk:
            return (
                f"No he encontrado todavía suficiente información relevante en los libros "
                f"para responder con claridad a la pregunta: '{question}'."
            )

        return f"{chunk}."

    def ask(self, question: str, context: list[str] | None = None, user_id: str | None = None) -> dict:
        match = self.retrieve_context(question)
        answer = self.generate_answer(question, match)

        sources = []
        if match:
            sources = [
                {
                    "source": match["book"]["filename"],
                    "label": match["book"]["label"],
                    "version": match["book"]["version"]
                }
            ]

        return {
            "answer": answer,
            "sources": sources
        }


rag_service = RagService()
