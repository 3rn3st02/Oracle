import os
import sys

from fastapi.testclient import TestClient

sys.path.insert(0, os.path.abspath(os.path.join(os.path.dirname(__file__), "..")))

from app.main import app

client = TestClient(app)


def test_health_returns_ok():
    response = client.get("/health")

    assert response.status_code == 200
    data = response.json()

    assert data["status"] == "ok"
    assert data["service"] == "oraculo-ia-rag"
    assert data["version"] == "0.1.0"
    assert data["initialized"] is True


def test_ask_returns_ok():
    payload = {
        "question": "¿Qué es ORACULO?",
        "user_id": "test-user",
        "context": []
    }

    response = client.post("/ask", json=payload)

    assert response.status_code == 200
    data = response.json()

    assert data["status"] == "ok"
    assert data["answer"] is not None
    assert isinstance(data["sources"], list)
    assert data["error"] is None
    assert "request_id" in data
    assert "latency_ms" in data


def test_ask_invalid_request_returns_400():
    payload = {
        "user_id": "test-user",
        "context": []
    }

    response = client.post("/ask", json=payload)

    assert response.status_code == 400
    data = response.json()

    assert data["status"] == "error"
    assert data["answer"] is None
    assert data["error"]["code"] == "VALIDATION_ERROR"

def test_ask_unknown_topic_returns_no_relevant_information():
    payload = {
        "question": "¿Qué es un SSD?",
        "user_id": "test-user",
        "context": []
    }

    response = client.post("/ask", json=payload)

    assert response.status_code == 200
    data = response.json()

    assert data["status"] == "ok"
    assert data["sources"] == []
    assert "No he encontrado todavía suficiente información relevante" in data["answer"]
