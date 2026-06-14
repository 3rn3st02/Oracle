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
        Intenta saltarse portada e índice buscando la segunda aparición de:
        - 1. Introducción
        - 1. Introduccion
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

    def split_into_sections(self, text: str) -> list:
        """
        Divide el libro en secciones usando títulos como:
        1. Introducción
        2. Arquitectura de Von Neumann
        3. Unidades funcionales de un ordenador
        4.1. Fases de búsqueda y de ejecución
        """
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
            "es", "son", "un", "cuales", "cuál"
        }

        words = re.findall(r"\w+", question.lower())
        keywords = [word for word in words if len(word) > 3 and word not in stopwords]

        extras = [
            "montaje", "mantenimiento", "equipos", "sistemas",
            "circuito", "eléctrico", "electrico", "serie", "paralelo",
            "arquitectura", "neumann", "unidades", "funcionales",
            "ordenador", "ssd", "placa", "base", "memoria",
            "puertos", "audio", "video", "vídeo"
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

    def is_noise_section(self, section: str) -> bool:
        section_lower = section.lower()

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
            "descuento a los clientes",
            "en resumen",
            "¿qué otras tendencias",
            "la placa base vamos a conocer",
            "jerarquía de memorias",
            "bus de control",
            "bus de direcciones",
            "bus de datos"
        ]

        if len(section.strip()) < 80:
            return True

        if sum(char.isalpha() for char in section) < 40:
            return True

        if any(pattern in section_lower for pattern in noise_patterns):
            return True

        return False

    def score_section(self, keywords: list, section: str) -> int:
        section_lower = section.lower()
        score = 0

        for keyword in keywords:
            if keyword in section_lower:
                score += 2

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

    def remove_noise_markers(self, text: str) -> str:
        text_lower = text.lower()

        for marker in [
            "caso práctico inicial",
            "caso practico inicial",
            "práctica profesional",
            "ficha de trabajo",
            "situación de partida"
        ]:
            pos = text_lower.find(marker)
            if pos != -1:
                return text[:pos].strip()

        return text

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
            sections = self.split_into_sections(text)

            # Reglas útiles para conceptos ya probados
            if "circuito eléctrico" in exact_question or "circuito electrico" in exact_question:
                chunk = self.extract_window_from_lines(
                    lines,
                    ["un circuito eléctrico es", "un circuito electrico es"],
                    window=3
                )
                if chunk:
                    return {"chunk": chunk, "book": book}

            if "circuito en serie" in exact_question:
                chunk = self.extract_window_from_lines(
                    lines,
                    ["circuito en serie"],
                    window=3
                )
                if chunk:
                    return {"chunk": chunk, "book": book}

            if "circuito en paralelo" in exact_question:
                chunk = self.extract_window_from_lines(
                    lines,
                    ["circuito en paralelo"],
                    window=3
                )
                if chunk:
                    return {"chunk": chunk, "book": book}

            if "von neumann" in exact_question:
                chunk = self.extract_window_from_lines(
                    lines,
                    ["arquitectura de von neumann", "von neumann"],
                    window=28
                )
                if chunk and "índice" not in chunk.lower():
                    return {"chunk": chunk, "book": book}

            # v0.2.4 -> unidades funcionales por secciones, evitando mezclas
            if "unidades funcionales" in exact_question:
                for section in sections:
                    section_lower = section.lower()
                    if (
                        (
                            "unidades funcionales de un ordenador" in section_lower
                            or "unidades funcionales" in section_lower
                        )
                        and "índice" not in section_lower
                        and "en resumen" not in section_lower
                        and "arquitectura de von neumann" not in section_lower
                        and "¿" not in section
                    ):
                        return {"chunk": section, "book": book}

            # v0.2.4 -> placa base por secciones, evitando casos laterales
            if "placa base" in exact_question:
                for section in sections:
                    section_lower = section.lower()
                    if (
                        "placa base" in section_lower
                        and "caso práctico inicial" not in section_lower
                        and "caso practico inicial" not in section_lower
                        and "en resumen" not in section_lower
                        and "antecedentes de los factores de forma" not in section_lower
                        and "¿" not in section
                    ):
                        return {"chunk": section, "book": book}

            # Búsqueda general por secciones
            for section in sections:
                if self.is_noise_section(section):
                    continue

                score = self.score_section(keywords, section)

                if score > best_score:
                    best_score = score
                    best_match = {
                        "chunk": section,
                        "book": book
                    }

        # v0.2.3 -> si la coincidencia es floja, no inventar
        if not best_match:
            return None

        best_chunk_lower = best_match["chunk"].lower()
        matched_keywords = [kw for kw in keywords if kw in best_chunk_lower]

        if best_score < 4 or len(matched_keywords) == 0:
            return None

        return best_match

    def generate_answer(self, question: str, match: dict | None) -> str:
        if not match:
            return (
                f"No he encontrado todavía suficiente información relevante en los libros "
                f"para responder con claridad a la pregunta: '{question}'."
            )

        chunk = match["chunk"].strip()
        chunk = self.remove_noise_markers(chunk)

        # Limpieza mínima visual
        chunk = chunk.replace("= ", "")
        chunk = re.sub(r"\s+", " ", chunk).strip()
        chunk = re.sub(r"^\d+(\.\d+)?\.\s*", "", chunk)
        chunk = chunk.strip(" .:")

        if not chunk:
            return (
                f"No he encontrado todavía suficiente información relevante en los libros "
                f"para responder con claridad a la pregunta: '{question}'."
            )

        if chunk.endswith("."):
            return chunk

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
