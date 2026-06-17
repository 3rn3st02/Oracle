import subprocess
from functools import lru_cache
from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict


def _version_from_git() -> str:
    try:
        r = subprocess.run(
            ["git", "describe", "--tags", "--abbrev=0"],
            capture_output=True,
            text=True,
            cwd=Path(__file__).resolve().parents[4],
            timeout=2,
        )
        if r.returncode == 0:
            return r.stdout.strip().lstrip("v")
    except Exception:
        pass
    return "dev"


class Settings(BaseSettings):
    app_name: str = "oraculo-ia-rag"
    app_version: str = _version_from_git()
    debug: bool = False

    api_prefix: str = ""
    request_timeout_seconds: int = 20
    backend_initialized: bool = True
    groq_api_key: str = ""

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore"
    )


@lru_cache
def get_settings() -> Settings:
    return Settings()
