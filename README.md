# Spring AI RAG Ingestion Demo

A minimal Spring Boot (CLI) application that demonstrates a Retrieval-Augmented Generation (RAG) ingestion pipeline:

- Reads PDF files from a configured input directory
- Splits text into chunks using Spring AI's TokenTextSplitter
- Generates embeddings with OpenAI
- Stores vectors into a pluggable VectorStore (Qdrant or Chroma)

This demo focuses on ingestion only (no web API). On startup, if PDFs are present, they are ingested into the configured vector store.

Repo: https://github.com/subrahmaniank/spring-ai-rag-ingestion-demo

## What's new (Chroma + Qdrant switchable via factory)

- Added a VectorStore factory pattern to support multiple providers
  - Choose provider via configuration: `app.vectorstore.provider: QDRANT | CHROMA`
  - Current implementations: Qdrant, Chroma
  - Easy to extend by adding another `VectorStoreFactory` implementation
- No changes needed in ingestion service — it depends only on `DocumentWriter`/`VectorStore`

Key new classes:
- `vectorstore/VectorStoreType.java` — enum of supported providers
- `vectorstore/props/*.java` — strongly-typed config properties for Qdrant/Chroma and provider switch
- `vectorstore/factory/*Factory.java` — provider-specific factories building the VectorStore
- `vectorstore/config/VectorStoreConfig.java` — selects factory at runtime based on `app.vectorstore.provider`

## Tech Stack

- Java 21, Spring Boot 3.4.x
- Spring AI 1.0.x
  - OpenAI embeddings (`text-embedding-3-small`)
  - Qdrant or Chroma vector store (switchable)
  - PDF page reader (`PagePdfDocumentReader`)
- Docker (optional) for local Qdrant/Chroma
- Maven

## How it works

1. The application runs in CLI mode (no web server).
2. On startup, `DocumentProcessingRunner` checks the input directory for `.pdf` files.
3. PDFs are read page-by-page via `PagePdfDocumentReader`.
4. All pages are split into overlapping chunks via `TokenTextSplitter`.
5. Chunks are embedded using OpenAI and written to the selected VectorStore via a `DocumentWriter` that delegates to `VectorStore.add`.

Key classes:
- `DocumentProcessingRunner` — triggers ingestion at startup and prints the active vector store name
- `DocumentProcessingService` — reads PDFs, splits text, and writes to the vector store
- `config/DocumentProcessingConfig` — configures the `TokenTextSplitter` and a `DocumentWriter` bound to the active `VectorStore`
- `vectorstore/...` — factory-pattern implementation for pluggable vector stores

## Prerequisites

- Java 21
- Maven 3.9+
- OpenAI API key
- One of:
  - Qdrant reachable over the network (or Docker)
  - ChromaDB reachable over the network (or Docker)

## Quick start

1) Start a vector database

Qdrant (Docker):
```bash
docker run -d --name qdrant \
  -p 6333:6333 -p 6334:6334 \
  -v qdrant_storage:/qdrant/storage \
  qdrant/qdrant:latest
```

Chroma (Docker):
```bash
docker run -it --rm --name chroma -p 8000:8000 ghcr.io/chroma-core/chroma:1.0.0
```

2) Set environment variables

Windows (cmd.exe):
```cmd
set OPENAI_API_KEY=sk-xxxx
rem Optional if your Qdrant requires auth
set QDRANT_API_KEY=your-qdrant-key
rem Optional if your Chroma requires auth
set CHROMA_API_KEY=your-chroma-key
```

PowerShell:
```powershell
$env:OPENAI_API_KEY="sk-xxxx"
# Optional if your Qdrant requires auth
$env:QDRANT_API_KEY="your-qdrant-key"
# Optional if your Chroma requires auth
$env:CHROMA_API_KEY="your-chroma-key"
```

macOS/Linux (bash/zsh):
```bash
export OPENAI_API_KEY=sk-xxxx
# Optional if your Qdrant requires auth
export QDRANT_API_KEY=your-qdrant-key
# Optional if your Chroma requires auth
export CHROMA_API_KEY=your-chroma-key
```

3) Put PDFs into the input folder

- Default location is configured in `application.yml`:
  - `app.input-dir: 'd:\projects\spring-ai-rag-ingestion-demo\input'` (Windows path)
- This repository already includes a few PDFs in `input/` for testing.
- Tip: For cross-platform use, you can change to a relative path (e.g. `./input`).

4) Choose the vector store provider

In `src/main/resources/application.yml` set:
```yaml
app:
  vectorstore:
    provider: QDRANT  # or CHROMA
```

5) Run the app

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
- Documents processed and stored in vector store: <provider-name>

If no PDFs are found, the runner logs a message and exits without error.

## Verify ingestion

Qdrant (REST port 6333):
- Check collection exists:
```bash
curl http://localhost:6333/collections/documents
```
- Count points (vectors) in the collection:
```bash
curl -s -X POST http://localhost:6333/collections/documents/points/count \
  -H "Content-Type: application/json" \
  -d '{"exact": true}'
```

Chroma (REST port 8000):
- Health (optional):
```bash
curl http://localhost:8000/api/v1/heartbeat
```
- Collections list (Chroma API may vary by version):
```bash
curl -s http://localhost:8000/api/v1/collections
```

## Configuration

All configuration is centralized in `src/main/resources/application.yml`.

Important properties (OpenAI + providers):
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
      chroma:
        client:
          host: http://localhost
          port: 8000
          # key-token: ${CHROMA_API_KEY:}
          # username: ${CHROMA_USERNAME:}
          # password: ${CHROMA_PASSWORD:}
        tenant-name: SpringAiTenant
        database-name: SpringAiDatabase
        collection-name: documents
        initialize-schema: true

app:
  input-dir: 'd:\projects\spring-ai-rag-ingestion-demo\input'
  vectorstore:
    provider: QDRANT   # or CHROMA
```

Notes:
- Switch providers by changing `app.vectorstore.provider` to `QDRANT` or `CHROMA`.
- `initialize-schema: true` lets Spring AI create collections/tenants/dbs as needed.
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

## Extending to another Vector DB

- Create a new enum entry in `VectorStoreType`
- Implement a new `VectorStoreFactory` that returns that type and builds the provider-specific VectorStore
- Add a matching `@ConfigurationProperties` class for the provider settings
- The active provider is selected automatically by `VectorStoreConfig` based on `app.vectorstore.provider`

## Project structure

```
.
├─ input/
├─ src/
│  ├─ main/java/com/example/springai/
│  │  ├─ SpringAiRagIngestionDemoApplication.java
│  │  ├─ DocumentProcessingRunner.java            # CLI entry: triggers ingestion
│  │  ├─ config/DocumentProcessingConfig.java     # TokenTextSplitter, writer
│  │  ├─ service/DocumentProcessingService.java   # Read PDFs, split, write to VectorStore
│  │  └─ vectorstore/
│  │     ├─ VectorStoreType.java
│  │     ├─ config/VectorStoreConfig.java
│  │     ├─ factory/{Qdrant,Chroma}VectorStoreFactory.java
│  │     └─ props/{VectorStoreProviderProperties,QdrantProperties,ChromaProperties}.java
│  └─ main/resources/
│     └─ application.yml
├─ pom.xml
└─ LICENSE
```

## Troubleshooting

- OpenAI errors (401/403): Ensure `OPENAI_API_KEY` is set and valid.
- Qdrant connection refused:
  - Ensure the container is running: `docker ps`
  - Ensure gRPC port 6334 is mapped (`-p 6334:6334`)
  - Host/port in `application.yml` must match your deployment
- Chroma connection refused:
  - Ensure the container is running and port 8000 is mapped
  - Check `spring.ai.vectorstore.chroma.client.host/port`
  - If using auth, set `key-token` or `username/password` accordingly
- No documents ingested:
  - Check `app.input-dir` points to the actual folder with `.pdf` files
  - Confirm file extensions are `.pdf` (case-insensitive)
- Windows path issues:
  - Keep backslash paths quoted in YAML or switch to a relative path like `./input`

## License

MIT © 2025 Subrahmanian Kumaraswamy
