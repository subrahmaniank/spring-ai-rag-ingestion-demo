package com.example.springai.config;

import org.springframework.ai.document.DocumentWriter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class DocumentProcessingConfig {









    @Bean
    public TokenTextSplitter tokenTextSplitter() {
        // Split documents into chunks with overlap
        return new TokenTextSplitter();
    }

    @Bean
    public DocumentWriter vectorStoreWriter(VectorStore vectorStore) {
        return documents -> {
            if (documents != null && !documents.isEmpty()) {
                vectorStore.add(documents);
            }
        };
    }
}
