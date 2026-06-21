import hashlib
import json
from pathlib import Path

from app.utils.text_utils import normalize_question


class CacheService:
    def __init__(self) -> None:
        self._path = Path(__file__).resolve().parents[2] / "data" / "cache.json"
        self._cache: dict = {}
        self._load()

    def _load(self) -> None:
        if self._path.exists():
            try:
                self._cache = json.loads(self._path.read_text(encoding="utf-8"))
            except Exception:
                self._cache = {}

    def _save(self) -> None:
        try:
            self._path.parent.mkdir(parents=True, exist_ok=True)
            self._path.write_text(
                json.dumps(self._cache, ensure_ascii=False, indent=2), encoding="utf-8"
            )
        except Exception:
            pass

    def _key(self, question: str) -> str:
        return hashlib.md5(normalize_question(question).encode()).hexdigest()

    def get(self, question: str) -> dict | None:
        entry = self._cache.get(self._key(question))
        if entry is None:
            return None
        # Support both old format (flat dict) and new format ({data, normalized_q})
        if isinstance(entry, dict) and "data" in entry and "normalized_q" in entry:
            return entry["data"]
        return entry

    def set(self, question: str, result: dict) -> None:
        normalized = normalize_question(question)
        self._cache[self._key(question)] = {"data": result, "normalized_q": normalized}
        self._save()

    def find_similar(self, question: str, threshold: float = 0.8) -> dict | None:
        """Return cached result for a question with Jaccard similarity ≥ threshold."""
        normalized = normalize_question(question)
        words_a = set(normalized.split())
        if not words_a:
            return None
        for entry in self._cache.values():
            if not isinstance(entry, dict) or "normalized_q" not in entry:
                continue
            words_b = set(entry["normalized_q"].split())
            if not words_b:
                continue
            union = len(words_a | words_b)
            if union == 0:
                continue
            jaccard = len(words_a & words_b) / union
            if jaccard >= threshold:
                return entry["data"]
        return None

    def size(self) -> int:
        return len(self._cache)


cache_service = CacheService()
