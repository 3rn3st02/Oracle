import json
import math
import random
import re
import unicodedata
from pathlib import Path

from groq import Groq

from app.core.config import get_settings

MAX_CONTEXT_SECTIONS = 5
TOP_CANDIDATES = 3


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

    # ------------------------------------------------------------------ #
    # Text normalization                                                   #
    # ------------------------------------------------------------------ #

    def strip_front_matter(self, text: str) -> str:
        for marker in ("1. Introducción", "1. Introduccion"):
            first = text.find(marker)
            if first == -1:
                continue
            second = text.find(marker, first + len(marker))
            return text[second:] if second != -1 else text[first:]
        return text

    def normalize_text(self, text: str) -> str:
        text = self.strip_front_matter(text)
        text = text.replace("\x0c", "\n")
        text = re.sub(r"[ \t]+", " ", text)
        text = re.sub(r"\n{2,}", "\n\n", text)
        return text.strip()

    # ------------------------------------------------------------------ #
    # Section splitting — returns structured dicts                         #
    # ------------------------------------------------------------------ #

    _HEADING = re.compile(r"^(\d+(?:\.\d+)*)\.?\s+\S")

    def split_into_sections(self, text: str) -> list[dict]:
        text = self.normalize_text(text)
        if not text:
            return []

        lines = [l.strip() for l in text.splitlines() if l.strip()]
        sections = []
        current: list[str] = []

        for line in lines:
            if self._HEADING.match(line) and current:
                sections.append(self._build_section(current))
                current = [line]
            else:
                current.append(line)

        if current:
            sections.append(self._build_section(current))

        return sections

    def _build_section(self, lines: list[str]) -> dict:
        first = lines[0]
        text = " ".join(lines).strip()

        m = re.match(r"^(\d+(?:\.\d+)*)\.?\s+(.*)", first)
        if m:
            heading = m.group(1)
            title = m.group(2).strip()[:80]
        else:
            heading = ""
            title = first[:80]

        level = heading.count(".") + 1 if heading else 0
        parent = ".".join(heading.split(".")[:-1]) if "." in heading else ""

        return {
            "heading": heading,
            "title": title,
            "text": text,
            "level": level,
            "parent": parent,
        }

    # ------------------------------------------------------------------ #
    # Keyword extraction                                                   #
    # ------------------------------------------------------------------ #

    @staticmethod
    def _to_ascii(text: str) -> str:
        return unicodedata.normalize("NFKD", text).encode("ascii", "ignore").decode("ascii")

    _STOPWORDS = {
        "que", "dice", "libro", "antiguo", "nuevo", "sobre", "del", "los",
        "las", "una", "uno", "unos", "unas", "para", "como", "cual", "cuales",
        "cuando", "donde", "el", "la", "de", "en", "por", "con", "sin", "al",
        "se", "es", "son", "un", "sus", "has", "hay", "este", "esta", "estos",
        "estan", "ser", "fue", "son", "han", "muy", "mas", "pero", "sino",
        "bien", "mal", "puede", "pueden",
    }

    _LIST_TRIGGERS = {
        "cuales", "tipos", "partes", "componentes", "enumera", "lista",
        "cuantos", "menciona", "diferencias", "caracteristicas", "niveles",
        "elementos", "funciones", "formas", "clases",
    }

    def extract_keywords(self, question: str) -> list[str]:
        normalized = self._to_ascii(question.lower())
        words = re.findall(r"\w+", normalized)
        keywords = [w for w in words if len(w) >= 3 and w not in self._STOPWORDS]

        seen: set[str] = set()
        unique = []
        for w in keywords:
            if w not in seen:
                seen.add(w)
                unique.append(w)
        return unique

    def _is_list_question(self, question: str) -> bool:
        normalized = self._to_ascii(question.lower())
        words = set(re.findall(r"\w+", normalized))
        return bool(words & self._LIST_TRIGGERS)

    @staticmethod
    def _keyword_variants(kw: str) -> list[str]:
        """Return singular/plural variants to handle basic Spanish morphology."""
        variants = [kw]
        if kw.endswith("s") and len(kw) > 4:
            variants.append(kw[:-1])  # buses → bus
        elif kw.endswith("es") and len(kw) > 4:
            variants.append(kw[:-2])  # tipos → tipo
        else:
            variants.append(kw + "s")  # bus → buses
        return variants

    # ------------------------------------------------------------------ #
    # Noise detection                                                      #
    # ------------------------------------------------------------------ #

    def is_noise_section(self, section: dict) -> bool:
        text = section["text"]
        if len(text.strip()) < 80:
            return True
        if sum(ch.isalpha() for ch in text) < 40:
            return True
        return False

    # ------------------------------------------------------------------ #
    # Scoring                                                              #
    # ------------------------------------------------------------------ #

    def _compute_idf(self, keywords: list[str], sections: list[dict]) -> dict[str, float]:
        N = max(len(sections), 1)
        idf: dict[str, float] = {}
        for kw in keywords:
            variants = self._keyword_variants(kw)
            df = sum(
                1 for s in sections
                if any(v in self._to_ascii(s["text"].lower()) for v in variants)
            )
            idf[kw] = math.log((N + 1) / (df + 1)) + 1
        return idf

    def score_section(self, keywords: list[str], question: str, section: dict,
                      is_list_question: bool = False, idf: dict | None = None) -> float:
        text_ascii = self._to_ascii(section["text"].lower())
        title_ascii = self._to_ascii(section["title"].lower())
        score = 0.0

        for kw in keywords:
            weight = idf[kw] if idf else 1.0
            for variant in self._keyword_variants(kw):
                count = text_ascii.count(variant)
                if count > 0:
                    score += (2 + min(count - 1, 3)) * weight
                    if variant in title_ascii:
                        score += 3 * weight
                    break

        # Consecutive keyword-pair phrase bonus
        q_ascii = self._to_ascii(question.lower())
        q_words = re.findall(r"\w+", q_ascii)
        q_kws = [w for w in q_words if w not in self._STOPWORDS and len(w) >= 3]
        for i in range(len(q_kws) - 1):
            phrase = q_kws[i] + " " + q_kws[i + 1]
            if phrase in text_ascii:
                score += 4.0

        # List-question boost: reward sections with enumerated content
        if is_list_question:
            list_items = len(re.findall(r"(?:^|\s)(?:\d+[\.\)]\s|[A-Z][\.\)]\s)", section["text"]))
            score += min(list_items * 2, 8)

        return score

    # ------------------------------------------------------------------ #
    # Noise marker removal                                                 #
    # ------------------------------------------------------------------ #

    def remove_noise_markers(self, text: str) -> str:
        text_lower = text.lower()
        for marker in (
            "caso práctico inicial", "caso practico inicial",
            "práctica profesional", "ficha de trabajo", "situación de partida",
        ):
            pos = text_lower.find(marker)
            if pos != -1:
                return text[:pos].strip()
        return text

    # ------------------------------------------------------------------ #
    # Context retrieval with parent→child expansion                        #
    # ------------------------------------------------------------------ #

    def retrieve_context(self, question: str) -> list[dict]:
        books = self.load_books_metadata()
        keywords = self.extract_keywords(question)
        is_list_q = self._is_list_question(question)

        if not keywords:
            return []

        book_data: dict[str, tuple[list[dict], dict]] = {}
        scored: list[dict] = []

        for book in books:
            text = self.load_book_text(book["filename"])
            if not text:
                continue
            sections = self.split_into_sections(text)
            book_data[book["filename"]] = (sections, book)

            valid = [s for s in sections if not self.is_noise_section(s)]
            idf = self._compute_idf(keywords, valid)

            for section in valid:
                score = self.score_section(keywords, question, section, is_list_q, idf)
                if score >= 2:
                    scored.append({"section": section, "book": book, "score": score})

        if not scored:
            return []

        scored.sort(key=lambda x: x["score"], reverse=True)
        top = scored[:TOP_CANDIDATES]

        # Build children/sibling index per book
        children_of: dict[str, dict[str, list[dict]]] = {}
        for fname, (sections, _) in book_data.items():
            index: dict[str, list[dict]] = {}
            for s in sections:
                if s["parent"]:
                    index.setdefault(s["parent"], []).append(s)
            children_of[fname] = index

        result: list[dict] = []
        seen: set[str] = set()

        for item in top:
            sec = item["section"]
            book = item["book"]
            heading = sec["heading"]
            fname = book["filename"]
            key = f"{fname}:{heading}"

            if key not in seen:
                seen.add(key)
                result.append({"chunk": sec["text"], "book": book, "score": item["score"]})

            children = children_of.get(fname, {}).get(heading, [])
            for child in children[:3]:
                child_key = f"{fname}:{child['heading']}"
                if child_key not in seen and not self.is_noise_section(child):
                    seen.add(child_key)
                    result.append({"chunk": child["text"], "book": book, "score": 0})

            # If this is a child, include siblings that also scored well
            if sec["parent"]:
                siblings = [
                    x for x in scored
                    if x["section"]["parent"] == sec["parent"]
                    and x["book"]["filename"] == fname
                    and f"{fname}:{x['section']['heading']}" not in seen
                ]
                for sib in siblings[:2]:
                    sib_key = f"{fname}:{sib['section']['heading']}"
                    seen.add(sib_key)
                    result.append({"chunk": sib["section"]["text"], "book": book, "score": sib["score"]})

            if len(result) >= MAX_CONTEXT_SECTIONS:
                break

        return result[:MAX_CONTEXT_SECTIONS]

    # ------------------------------------------------------------------ #
    # Answer generation                                                    #
    # ------------------------------------------------------------------ #

    _OFF_TOPIC_REPLIES = [
        "Céntrate en clase 😄",
        "Eso no entra en el examen... que yo sepa 🤔",
        "Mi libro no llega hasta ahí, ¡pero tú sí puedes! 📚",
        "Houston, tenemos un problema: eso no está en el temario 🚀",
        "Pregúntale eso a Google, yo solo sé de lo mío 😅",
        "¿Eso lo vas a poner en el trabajo final? Porque no te lo voy a poder ayudar 😬",
    ]

    def generate_answer(self, question: str, matches: list[dict]) -> str:
        if not matches:
            return random.choice(self._OFF_TOPIC_REPLIES)

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
            "Si la pregunta es sobre tipos, partes o listas, enuméralos claramente. "
            "Si la información necesaria no está en los fragmentos, indícalo con claridad. "
            "IMPORTANTE: Empieza siempre la respuesta directamente con la definición o el contenido. "
            "Nunca empieces con 'Según el Fragmento', 'En el Fragmento', 'De acuerdo con' ni referencias al fragmento. "
            "Responde en español, de forma concisa y clara."
        )

        user_message = (
            f"Fragmentos del libro:\n\"\"\"\n{context}\n\"\"\"\n\n"
            f"Pregunta: {question}"
        )

        response = self._groq.chat.completions.create(
            model="llama-3.1-8b-instant",
            messages=[
                {"role": "system", "content": system_prompt},
                {"role": "user", "content": user_message},
            ],
            temperature=0.2,
            max_tokens=1024,
        )

        return response.choices[0].message.content.strip()

    # ------------------------------------------------------------------ #
    # Special hardcoded responses                                          #
    # ------------------------------------------------------------------ #

    _SPECIAL_SOURCE = [{"source": "Oráculo Info", "label": "Oráculo Info", "version": "static"}]

    _WHO_AM_I_TRIGGERS = [
        "quien eres", "que eres", "presentate", "hablame de ti",
        "como te llamas", "cual es tu nombre", "quien es oraculo",
        "que es oraculo", "quien soy", "quien soy yo",
    ]
    _WHO_AM_I_REPLY = (
        "Soy Oráculo, una IA especializada en Montaje y Mantenimiento de sistemas informáticos. "
        "Fui creada por Arandeitors y Archvaro, dos apasionados de la programación e informática. "
        "Mi cerebro piensa en Python y mi interfaz habla Kotlin. ¿En qué puedo ayudarte hoy?"
    )

    _CREATORS_TRIGGERS = [
        "quienes son los creadores", "quien te hizo", "quien os hizo",
        "quien te creo", "quien os creo", "quienes te crearon",
        "quienes son tus creadores", "quien hizo oraculo",
        "quien te programo", "quien te diseno", "quien esta detras",
        "quien hizo oraculo", "los creadores", "tus creadores",
        "quien te desarrollo",
    ]
    _CREATORS_REPLY = (
        "Archvaro — Guatemalteco de nacimiento, informático por elección. "
        "El tipo que hace que las cosas funcionen.\n"
        "Arandeitors — Vasco de origen, programador y fanático del Clash of Clans "
        "y Kingdom Hearts. El que le da alma al código."
    )

    @staticmethod
    def _normalize_question(text: str) -> str:
        result = unicodedata.normalize("NFKD", text.lower()).encode("ascii", "ignore").decode("ascii")
        result = re.sub(r"[^\w\s]", " ", result)
        result = re.sub(r"\s+", " ", result).strip()
        return result

    def detect_special_question(self, question: str) -> dict | None:
        normalized = self._normalize_question(question)

        for trigger in self._WHO_AM_I_TRIGGERS:
            if trigger in normalized:
                return {"answer": self._WHO_AM_I_REPLY, "sources": self._SPECIAL_SOURCE}

        for trigger in self._CREATORS_TRIGGERS:
            if trigger in normalized:
                return {"answer": self._CREATORS_REPLY, "sources": self._SPECIAL_SOURCE}

        return None

    # ------------------------------------------------------------------ #
    # Public API                                                           #
    # ------------------------------------------------------------------ #

    def ask(self, question: str, context: list[str] | None = None, user_id: str | None = None) -> dict:
        special = self.detect_special_question(question)
        if special:
            return special

        matches = self.retrieve_context(question)
        answer = self.generate_answer(question, matches)

        seen: set[str] = set()
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
