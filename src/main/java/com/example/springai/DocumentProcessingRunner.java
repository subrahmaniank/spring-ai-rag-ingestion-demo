package com.example.springai;

import com.example.springai.service.DocumentProcessingService;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DocumentProcessingRunner implements CommandLineRunner {

    private final DocumentProcessingService documentProcessingService;
    private final VectorStore vectorStore;

    public DocumentProcessingRunner(DocumentProcessingService documentProcessingService, VectorStore vectorStore) {
        this.documentProcessingService = documentProcessingService;
        this.vectorStore = vectorStore;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Starting document processing from: " + documentProcessingService.getInputDir().toAbsolutePath());

        if (!documentProcessingService.hasDocuments()) {
            System.out.println("No PDF documents found in input directory: " + documentProcessingService.getInputDir().toAbsolutePath() + ". Skipping ingestion on startup.");
            return;
        }

        try {
            documentProcessingService.processDocuments();
            System.out.println("Documents processed and stored in vector store: " + vectorStore.getName());
        } catch (Exception e) {
            System.err.println("Error processing documents: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
