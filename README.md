# Spring AI RAG Ingestion Demo

A minimal Spring Boot (CLI) application that demonstrates a Retrieval-Augmented Generation (RAG) ingestion pipeline:

- Reads PDF files from a configured input directory
- Splits text into chunks using Spring AI's TokenTextSplitter
- Generates embeddings with OpenAI
- Stores vectors into a Qdrant vector database

This demo focuses on ingestion only (no web API). On startup, if PDFs are present, they are ingested into Qdrant.

Repo: https://github.com/subrahmaniank/spring-ai-rag-ingestion-demo

## Tech Stack

- Java 21, Spring Boot 3.4.x
- Spring AI 1.0.x
  - OpenAI embeddings (`text-embedding-3-small`)
  - Qdrant vector store
  - PDF page reader (`PagePdfDocumentReader`)
- Qdrant (vector DB; gRPC 6334, HTTP 6333)
- Maven

## How it works

1. The application runs in CLI mode (no web server).
2. On startup, `DocumentProcessingRunner` checks the input directory for `.pdf` files.
3. PDFs are read page-by-page via `PagePdfDocumentReader`.
4. All pages are split into overlapping chunks via `TokenTextSplitter`.
5. Chunks are embedded using OpenAI and written to Qdrant (`collection-name: documents`).

Key classes:
- `DocumentProcessingRunner` — triggers ingestion at startup
- `DocumentProcessingService` — reads PDFs, splits text, and writes to the vector store
- `DocumentProcessingConfig` — configures the `TokenTextSplitter` and `DocumentWriter` beans

## Prerequisites

- Java 21
- Maven 3.9+
- Docker (to run Qdrant locally), or a Qdrant instance reachable over the network
- OpenAI API key

## Quick start

1) Start Qdrant (Docker)
- Exposes both REST (6333) and gRPC (6334)
- Uses a named volume for persistence

```bash
docker run -d --name qdrant \
  -p 6333:6333 -p 6334:6334 \
  -v qdrant_storage:/qdrant/storage \
  qdrant/qdrant:latest
```

2) Set environment variables

Windows (cmd.exe):
```cmd
set OPENAI_API_KEY=sk-xxxx
rem Optional if your Qdrant requires auth
set QDRANT_API_KEY=your-qdrant-key
```

PowerShell:
```powershell
$env:OPENAI_API_KEY="sk-xxxx"
# Optional if your Qdrant requires auth
$env:QDRANT_API_KEY="your-qdrant-key"
```

macOS/Linux (bash/zsh):
```bash
export OPENAI_API_KEY=sk-xxxx
# Optional if your Qdrant requires auth
export QDRANT_API_KEY=your-qdrant-key
```

3) Put PDFs into the input folder

- Default location is configured in `application.yml`:
  - `app.input-dir: 'd:\projects\spring-ai-rag-ingestion-demo\input'` (Windows path)
- This repository already includes a few PDFs in `input/` for testing.

Tip: For cross-platform use, you can change to a relative path (e.g. `./input`).

4) Run the app

Using Maven:
```bash
mvn spring-boot:run
```

Or build a jar and run:
```bash
mvn -q -DskipTests clean package
java -jar target/spring-ai-rag-ingestion-demo-0.0.1-SNAPSHOT.jar
```

Expected console output includes:
- Starting document processing from: <input-dir>
- Documents processed and stored in Qdrant successfully!

If no PDFs are found, the runner logs a message and exits without error.

## Verify ingestion in Qdrant

Use the REST API on port 6333.

- Check collection exists:
```bash
curl http://localhost:6333/collections/documents
```

- Count points (vectors) in the collection:
```bash
curl -X POST http://localhost:6333/collections/documents/points/count ^
  -H "Content-Type: application/json" ^
  -d "{\"exact\": true}"
```
On macOS/Linux replace the line continuation as needed:
```bash
curl -s -X POST http://localhost:6333/collections/documents/points/count \
  -H "Content-Type: application/json" \
  -d '{"exact": true}'
```

A successful response looks like:
```json
{"result":{"count":1234},"status":"ok","time":0.001}
```

## Configuration

All configuration is centralized in `src/main/resources/application.yml`.

Important properties:
```yaml
spring:
  main:
    web-application-type: none   # CLI mode
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:}
      embedding:
        model: text-embedding-3-small
    vectorstore:
      qdrant:
        host: localhost
        port: 6334               # gRPC port
        collection-name: documents
        use-tls: false
        initialize-schema: true
        api-key: ${QDRANT_API_KEY:}

app:
  input-dir: 'd:\projects\spring-ai-rag-ingestion-demo\input'
```

Notes:
- `initialize-schema: true` lets Spring AI automatically create the collection if it does not exist.
- If you change `collection-name`, the new collection will be created on next run.
- For Windows paths in YAML, keep the value quoted to preserve backslashes.
- To make the input directory portable, consider setting `app.input-dir: ./input`.

### Tuning chunking

`DocumentProcessingConfig` sets:
```java
@Bean
public TokenTextSplitter tokenTextSplitter() {
    return new TokenTextSplitter(); // default values
}
```

To customize chunk size and overlap (example: 800 tokens, 200 overlap):
```java
@Bean
public TokenTextSplitter tokenTextSplitter() {
    return new TokenTextSplitter(800, 200);
}
```

## Project structure

```
.
├─ input/                          # Place PDFs here (sample PDFs included)
├─ src/
│  ├─ main/java/com/example/springai/
│  │  ├─ SpringAiRagIngestionDemoApplication.java
│  │  ├─ DocumentProcessingRunner.java            # CLI entry: triggers ingestion
│  │  ├─ config/DocumentProcessingConfig.java     # TokenTextSplitter, writer
│  │  ├─ service/DocumentProcessingService.java   # Read PDFs, split, write to Qdrant
│  │  └─ controller/DocumentController.java       # Placeholder (no web API)
│  └─ main/resources/
│     ├─ application.yml
│     └─ application.properties
├─ pom.xml
└─ LICENSE
```

## Resetting / re-ingesting

- Remove the Qdrant collection:
```bash
curl -X DELETE http://localhost:6333/collections/documents
```
- Add/modify PDFs in the input folder, then re-run the app to ingest again.

## Troubleshooting

- OpenAI errors (401/403): Ensure `OPENAI_API_KEY` is set and valid.
- Qdrant connection refused:
  - Ensure the container is running: `docker ps`
  - Ensure gRPC port 6334 is mapped (`-p 6334:6334`)
  - Host/port in `application.yml` must match your deployment
- No documents ingested:
  - Check `app.input-dir` points to the actual folder with `.pdf` files
  - Confirm file extensions are `.pdf` (case-insensitive)
- Windows path issues:
  - Keep backslash paths quoted in YAML or switch to a relative path like `./input`
- Qdrant with API key:
  - Set `QDRANT_API_KEY` and ensure your instance enforces/accepts that key

## License

MIT © 2025 Subrahmanian Kumaraswamy
