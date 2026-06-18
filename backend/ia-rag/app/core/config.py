from functools import lru_cache
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "oraculo-ia-rag"
    app_version: str = "0.5"
    debug: bool = False

    api_prefix: str = ""
    request_timeout_seconds: int = 20
    groq_api_key: str = ""
    # Orígenes CORS permitidos separados por coma. Configura via ALLOWED_ORIGINS en .env
    allowed_origins: str = "*"

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore"
    )


@lru_cache
def get_settings() -> Settings:
    return Settings()
