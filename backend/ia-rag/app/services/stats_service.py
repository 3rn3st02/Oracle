import json
from datetime import date
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
            self._data["today_date"] = today

    def record_question(
        self,
        user_id: Optional[str] = None,
        unit_label: Optional[str] = None,
    ) -> None:
        self._reset_today_if_needed()
        self._data["questions_total"] = self._data.get("questions_total", 0) + 1
        self._data["questions_today"] = self._data.get("questions_today", 0) + 1
        if unit_label:
            by_unit = self._data.setdefault("questions_by_unit", {})
            by_unit[unit_label] = by_unit.get(unit_label, 0) + 1
        if user_id:
            users = self._data.setdefault("active_user_ids", [])
            if user_id not in users:
                users.append(user_id)
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
            "usage_by_unit": by_unit,
            "usage_pct_by_unit": usage_pct,
        }

    @property
    def questions_total(self) -> int:
        return self._data.get("questions_total", 0)

    @property
    def questions_today(self) -> int:
        self._reset_today_if_needed()
        return self._data.get("questions_today", 0)


stats_service = StatsService()
