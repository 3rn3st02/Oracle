from functools import lru_cache
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    app_name: str = "Oráculo"
    app_version: str = "0.6"
    debug: bool = False

    api_prefix: str = ""
    request_timeout_seconds: int = 20
    groq_api_key: str = ""
    allowed_origins: str = "*"
    upload_api_key: str = ""
    production: bool = False

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore"
    )


@lru_cache
def get_settings() -> Settings:
    return Settings()
