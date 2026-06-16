from fastapi import FastAPI, Request, status
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from starlette.exceptions import HTTPException as StarletteHTTPException

from app.api.routes import router
from app.core.config import get_settings

settings = get_settings()

app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    debug=settings.debug
)

app.include_router(router)


@app.exception_handler(RequestValidationError)
async def validation_exception_handler(request: Request, exc: RequestValidationError):
    return JSONResponse(
        status_code=status.HTTP_400_BAD_REQUEST,
        content={
            "answer": None,
            "sources": [],
            "status": "error",
            "error": {
                "code": "VALIDATION_ERROR",
                "message": "La request enviada no es válida.",
                "details": {
                    "errors": exc.errors()
                }
            },
            "request_id": "unknown",
            "latency_ms": 0
        }
    )


@app.exception_handler(StarletteHTTPException)
async def http_exception_handler(request: Request, exc: StarletteHTTPException):

    if exc.status_code == status.HTTP_404_NOT_FOUND:
        return JSONResponse(
            status_code=status.HTTP_404_NOT_FOUND,
            content={
                "answer": None,
                "sources": [],
                "status": "error",
                "error": {
                    "code": "ENDPOINT_NOT_FOUND",
                    "message": "El endpoint solicitado no existe.",
                    "details": {
                        "path": str(request.url.path)
                    }
                },
                "request_id": "unknown",
                "latency_ms": 0
            }
        )

    if exc.status_code == status.HTTP_503_SERVICE_UNAVAILABLE and isinstance(exc.detail, dict):
        return JSONResponse(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            content={
                "answer": None,
                "sources": [],
                "status": "error",
                "error": {
                    "code": exc.detail.get("code", "SERVICE_UNAVAILABLE"),
                    "message": exc.detail.get("message", "Servicio no disponible."),
                    "details": exc.detail.get("details", {})
                },
                "request_id": "unknown",
                "latency_ms": 0
            }
        )

    return JSONResponse(
        status_code=exc.status_code,
        content={
            "answer": None,
            "sources": [],
            "status": "error",
            "error": {
                "code": "HTTP_ERROR",
                "message": str(exc.detail),
                "details": {}
            },
            "request_id": "unknown",
            "latency_ms": 0
        }
    )


@app.exception_handler(Exception)
async def generic_exception_handler(request: Request, exc: Exception):
    return JSONResponse(
        status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
        content={
            "answer": None,
            "sources": [],
            "status": "error",
            "error": {
                "code": "INTERNAL_ERROR",
                "message": "Se ha producido un error interno.",
                "details": {}
            },
            "request_id": "unknown",
            "latency_ms": 0
        }
    )
