import json
from datetime import date, datetime, timezone
from pathlib import Path
from typing import Optional


class StatsService:
    def __init__(self) -> None:
        self._path = Path(__file__).resolve().parents[2] / "data" / "stats.json"
        self._data: dict = {
            "questions_total": 0,
            "questions_today": 0,
            "today_date": "",
            "questions_by_unit": {},
            "active_user_ids": [],
        }
        self._load()

    def _load(self) -> None:
        if self._path.exists():
            try:
                self._data = json.loads(self._path.read_text(encoding="utf-8"))
            except Exception:
                pass

    def _save(self) -> None:
        try:
            self._path.parent.mkdir(parents=True, exist_ok=True)
            self._path.write_text(
                json.dumps(self._data, ensure_ascii=False, indent=2), encoding="utf-8"
            )
        except Exception:
            pass

    def _reset_today_if_needed(self) -> None:
        today = date.today().isoformat()
        if self._data.get("today_date") != today:
            self._data["questions_today"] = 0
            self._data["active_user_ids_today"] = []
            self._data["today_date"] = today

    def record_question(
        self,
        user_id: Optional[str] = None,
        unit_label: Optional[str] = None,
        unit_labels: Optional[list] = None,
    ) -> None:
        self._reset_today_if_needed()
        self._data["questions_total"] = self._data.get("questions_total", 0) + 1
        self._data["questions_today"] = self._data.get("questions_today", 0) + 1
        by_unit = self._data.setdefault("questions_by_unit", {})
        labels = unit_labels if unit_labels else ([unit_label] if unit_label else [])
        for label in labels:
            by_unit[label] = by_unit.get(label, 0) + 1
        if user_id:
            users = self._data.setdefault("active_user_ids", [])
            if user_id not in users:
                users.append(user_id)
            users_today = self._data.setdefault("active_user_ids_today", [])
            if user_id not in users_today:
                users_today.append(user_id)
        self._save()

    def get_stats(self) -> dict:
        self._reset_today_if_needed()
        total = self._data.get("questions_total", 0)
        by_unit = self._data.get("questions_by_unit", {})
        usage_pct = {k: round(v / total * 100, 1) for k, v in by_unit.items()} if total > 0 else {}
        return {
            "questions_total": total,
            "questions_today": self._data.get("questions_today", 0),
            "active_users": len(self._data.get("active_user_ids", [])),
            "active_users_today": len(self._data.get("active_user_ids_today", [])),
            "usage_by_unit": by_unit,
            "usage_pct_by_unit": usage_pct,
        }

    def add_exam_score(self, user_id: str, unit_label: str, correct: int, total: int) -> None:
        scores = self._data.setdefault("exam_scores", [])
        scores.append({
            "user_id": user_id,
            "unit": unit_label,
            "correct": correct,
            "total": total,
            "percentage": round(correct / total * 100) if total > 0 else 0,
            "timestamp": datetime.now(timezone.utc).isoformat(),
        })
        self._save()

    def get_exam_scores(self, user_id: str) -> list:
        scores = self._data.get("exam_scores", [])
        return [s for s in scores if s.get("user_id") == user_id]

    @property
    def questions_total(self) -> int:
        return self._data.get("questions_total", 0)

    @property
    def questions_today(self) -> int:
        self._reset_today_if_needed()
        return self._data.get("questions_today", 0)


stats_service = StatsService()
