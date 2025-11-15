package com.example.springai.vectorstore.factory;

import com.example.springai.vectorstore.VectorStoreType;
import org.springframework.ai.vectorstore.VectorStore;

public interface VectorStoreFactory {
    VectorStoreType getType();
    VectorStore create();
}
