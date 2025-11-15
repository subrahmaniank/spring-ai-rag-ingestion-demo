package com.example.springai.vectorstore.config;

import com.example.springai.vectorstore.VectorStoreType;
import com.example.springai.vectorstore.factory.VectorStoreFactory;
import com.example.springai.vectorstore.props.ChromaProperties;
import com.example.springai.vectorstore.props.QdrantProperties;
import com.example.springai.vectorstore.props.VectorStoreProviderProperties;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties({VectorStoreProviderProperties.class, QdrantProperties.class, ChromaProperties.class})
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(List<VectorStoreFactory> factories, VectorStoreProviderProperties providerProps) {
        VectorStoreType desired = providerProps.getProvider();
        return factories.stream()
                .filter(f -> f.getType() == desired)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No VectorStoreFactory found for provider: " + desired))
                .create();
    }
}
