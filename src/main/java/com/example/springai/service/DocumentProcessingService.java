package com.example.springai.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentWriter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class DocumentProcessingService {

    private final TokenTextSplitter tokenTextSplitter;
    private final DocumentWriter vectorStoreWriter;
    private final Path inputDir;

    public DocumentProcessingService(
            TokenTextSplitter tokenTextSplitter,
            VectorStore vectorStore,
            @Value("${app.input-dir}") String inputDir) {
        this.tokenTextSplitter = tokenTextSplitter;
        this.vectorStoreWriter = documents -> {
            if (documents != null && !documents.isEmpty()) {
                vectorStore.add(documents);
            }
        };
        this.inputDir = Paths.get(inputDir);
    }

    public void processDocuments() throws IOException {
        if (!Files.isDirectory(inputDir)) {
            throw new IOException("Input directory does not exist: " + inputDir.toAbsolutePath());
        }

        List<Document> allDocuments = new ArrayList<>();

        try (Stream<Path> paths = Files.list(inputDir)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().toLowerCase().endsWith(".pdf"))
                    .forEach(p -> {
                        try {
                            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(new FileSystemResource(p.toFile()));
                            List<Document> docs = pdfReader.read();
                            if (docs != null && !docs.isEmpty()) {
                                allDocuments.addAll(docs);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Failed reading PDF: " + p, e);
                        }
                    });
        }

        if (allDocuments.isEmpty()) {
            return;
        }

        // Split documents into chunks
        List<Document> splitDocuments = tokenTextSplitter.apply(allDocuments);

        // Write chunks to vector store
        vectorStoreWriter.accept(splitDocuments);
    }

    public boolean hasDocuments() {
        try {
            if (!Files.isDirectory(inputDir)) {
                return false;
            }
            try (Stream<Path> paths = Files.list(inputDir)) {
                return paths.anyMatch(p -> Files.isRegularFile(p) && p.toString().toLowerCase().endsWith(".pdf"));
            }
        } catch (IOException e) {
            return false;
        }
    }

    public Path getInputDir() {
        return inputDir;
    }
}
