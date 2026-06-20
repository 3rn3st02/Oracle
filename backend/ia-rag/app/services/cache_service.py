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

    def _key(self, question: str) -> str:
        normalized = normalize_question(question)
        return hashlib.md5(normalized.encode()).hexdigest()

    def get(self, question: str) -> dict | None:
        return self._cache.get(self._key(question))

    def set(self, question: str, result: dict) -> None:
        self._cache[self._key(question)] = result
        try:
            self._path.parent.mkdir(parents=True, exist_ok=True)
            self._path.write_text(
                json.dumps(self._cache, ensure_ascii=False, indent=2), encoding="utf-8"
            )
        except Exception:
            pass

    def size(self) -> int:
        return len(self._cache)


cache_service = CacheService()
