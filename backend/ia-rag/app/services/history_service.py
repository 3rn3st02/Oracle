import json
from datetime import datetime, timezone
from pathlib import Path


class HistoryService:
    def __init__(self) -> None:
        self._path = Path(__file__).resolve().parents[2] / "data" / "history.json"
        self._data: dict = {}
        self._load()

    def _load(self) -> None:
        if self._path.exists():
            try:
                self._data = json.loads(self._path.read_text(encoding="utf-8"))
            except Exception:
                self._data = {}

    def _save(self) -> None:
        try:
            self._path.parent.mkdir(parents=True, exist_ok=True)
            self._path.write_text(
                json.dumps(self._data, ensure_ascii=False, indent=2), encoding="utf-8"
            )
        except Exception:
            pass

    def add_message(
        self,
        user_id: str,
        session_id: str,
        question: str,
        answer: str,
        sources: list,
    ) -> None:
        self._data.setdefault(user_id, {}).setdefault(session_id, []).append({
            "timestamp": datetime.now(timezone.utc).isoformat(),
            "question": question,
            "answer": answer,
            "sources": sources,
        })
        self._save()

    def get_sessions(self, user_id: str) -> dict:
        """Return session summaries for a user."""
        sessions = self._data.get(user_id, {})
        return {
            sid: {
                "message_count": len(msgs),
                "first_timestamp": msgs[0]["timestamp"] if msgs else None,
                "last_timestamp": msgs[-1]["timestamp"] if msgs else None,
                "last_question": msgs[-1]["question"] if msgs else None,
            }
            for sid, msgs in sessions.items()
        }

    def get_session(self, user_id: str, session_id: str) -> list:
        return self._data.get(user_id, {}).get(session_id, [])

    def get_last_n(self, user_id: str, session_id: str, n: int = 5) -> list:
        messages = self.get_session(user_id, session_id)
        return messages[-n:] if messages else []

    def get_all(self) -> dict:
        return self._data

    def total_users(self) -> int:
        return len(self._data)


history_service = HistoryService()
