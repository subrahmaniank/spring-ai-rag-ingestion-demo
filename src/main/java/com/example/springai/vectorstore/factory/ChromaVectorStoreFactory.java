package com.example.springai.vectorstore.factory;

import com.example.springai.vectorstore.VectorStoreType;
import com.example.springai.vectorstore.props.ChromaProperties;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.chroma.ChromaApi;
import org.springframework.ai.vectorstore.chroma.ChromaVectorStore;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ChromaVectorStoreFactory implements VectorStoreFactory {

    private final ChromaProperties props;
    private final EmbeddingModel embeddingModel;
    private final RestClient.Builder restClientBuilder;

    public ChromaVectorStoreFactory(ChromaProperties props, EmbeddingModel embeddingModel) {
        this.props = props;
        this.embeddingModel = embeddingModel;
        this.restClientBuilder = RestClient.builder().requestFactory(new SimpleClientHttpRequestFactory());
    }

    @Override
    public VectorStoreType getType() {
        return VectorStoreType.CHROMA;
    }

    @Override
    public VectorStore create() {
        String baseUrl = props.getClient().getHost() + ":" + props.getClient().getPort();
        ChromaApi api = new ChromaApi(baseUrl, restClientBuilder);
        if (props.getClient().getKeyToken() != null && !props.getClient().getKeyToken().isBlank()) {
            api = api.withKeyToken(props.getClient().getKeyToken());
        }
        if (props.getClient().getUsername() != null && props.getClient().getPassword() != null
                && !props.getClient().getUsername().isBlank() && !props.getClient().getPassword().isBlank()) {
            api = api.withBasicAuth(props.getClient().getUsername(), props.getClient().getPassword());
        }
        return ChromaVectorStore.builder(api, embeddingModel)
                .tenantName(props.getTenantName())
                .databaseName(props.getDatabaseName())
                .collectionName(props.getCollectionName())
                .initializeSchema(props.isInitializeSchema())
                .build();
    }
}
