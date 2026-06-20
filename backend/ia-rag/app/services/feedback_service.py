import json
from collections import Counter
from datetime import datetime, timezone
from pathlib import Path


class FeedbackService:
    def __init__(self) -> None:
        self._path = Path(__file__).resolve().parents[2] / "data" / "feedback.json"
        self._data: list[dict] = []
        self._load()

    def _load(self) -> None:
        if self._path.exists():
            try:
                self._data = json.loads(self._path.read_text(encoding="utf-8"))
            except Exception:
                self._data = []

    def _save(self) -> None:
        try:
            self._path.parent.mkdir(parents=True, exist_ok=True)
            self._path.write_text(
                json.dumps(self._data, ensure_ascii=False, indent=2), encoding="utf-8"
            )
        except Exception:
            pass

    def add(self, request_id: str, useful: bool, question: str = "", answer: str = "") -> None:
        self._data.append({
            "request_id": request_id,
            "useful": useful,
            "question": question,
            "answer": answer[:200],
            "timestamp": datetime.now(timezone.utc).isoformat(),
        })
        self._save()

    def stats(self) -> dict:
        total = len(self._data)
        positive = sum(1 for f in self._data if f.get("useful"))
        negative = total - positive

        # Top 5 most liked questions
        liked = [f.get("question", "") for f in self._data if f.get("useful") and f.get("question")]
        top_questions = [q for q, _ in Counter(liked).most_common(5)]

        # Questions answered today
        today = datetime.now(timezone.utc).date().isoformat()
        today_count = sum(
            1 for f in self._data
            if f.get("timestamp", "").startswith(today)
        )

        return {
            "total": total,
            "positive": positive,
            "negative": negative,
            "top_questions": top_questions,
            "today": today_count,
        }


feedback_service = FeedbackService()
