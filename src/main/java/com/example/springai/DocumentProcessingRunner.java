package com.example.springai;

import com.example.springai.service.DocumentProcessingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DocumentProcessingRunner implements CommandLineRunner {

    private final DocumentProcessingService documentProcessingService;

    public DocumentProcessingRunner(DocumentProcessingService documentProcessingService) {
        this.documentProcessingService = documentProcessingService;
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
            System.out.println("Documents processed and stored in Qdrant successfully!");
        } catch (Exception e) {
            System.err.println("Error processing documents: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
