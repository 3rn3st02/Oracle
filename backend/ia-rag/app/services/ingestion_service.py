from pathlib import Path

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

    def save_text_output(self, output_filename: str, text: str) -> Path:
        self.processed_path.mkdir(parents=True, exist_ok=True)
        output_path = self.processed_path / output_filename
        output_path.write_text(text, encoding="utf-8")
        return output_path

    def ingest_pdf(self, pdf_filename: str) -> Path:
        text = self.extract_pdf_text(pdf_filename)
        output_filename = Path(pdf_filename).stem + ".txt"
        return self.save_text_output(output_filename, text)


ingestion_service = IngestionService()
