from pathlib import Path

from docx import Document
from pypdf import PdfReader


class IngestionService:
    def __init__(self) -> None:
        self.base_path = Path(__file__).resolve().parents[2]
        self.books_path = self.base_path / "data" / "books"
        self.processed_path = self.base_path / "data" / "processed"

    def extract_pdf_text(self, pdf_filename: str) -> str:
        pdf_path = self.books_path / pdf_filename

        if not pdf_path.exists():
            raise FileNotFoundError(f"No se encontró el archivo: {pdf_path}")

        reader = PdfReader(str(pdf_path))
        extracted_pages = []

        for page in reader.pages:
            page_text = page.extract_text() or ""
            extracted_pages.append(page_text.strip())

        return "\n\n".join(extracted_pages).strip()

    def extract_docx_text(self, docx_filename: str) -> str:
        docx_path = self.books_path / docx_filename

        if not docx_path.exists():
            raise FileNotFoundError(f"No se encontró el archivo: {docx_path}")

        doc = Document(str(docx_path))
        paragraphs = [p.text.strip() for p in doc.paragraphs if p.text.strip()]
        return "\n\n".join(paragraphs).strip()

    def save_text_output(self, output_filename: str, text: str) -> Path:
        self.processed_path.mkdir(parents=True, exist_ok=True)
        output_path = self.processed_path / output_filename
        output_path.write_text(text, encoding="utf-8")
        return output_path

    def ingest_pdf(self, pdf_filename: str) -> Path:
        text = self.extract_pdf_text(pdf_filename)
        output_filename = Path(pdf_filename).stem + ".txt"
        return self.save_text_output(output_filename, text)

    def ingest_docx(self, docx_filename: str) -> Path:
        text = self.extract_docx_text(docx_filename)
        output_filename = Path(docx_filename).stem + ".txt"
        return self.save_text_output(output_filename, text)

    def ingest_file(self, filename: str) -> Path:
        suffix = Path(filename).suffix.lower()
        if suffix == ".pdf":
            return self.ingest_pdf(filename)
        if suffix == ".docx":
            return self.ingest_docx(filename)
        raise ValueError(f"Formato no soportado: {suffix}. Use .pdf o .docx")


ingestion_service = IngestionService()
