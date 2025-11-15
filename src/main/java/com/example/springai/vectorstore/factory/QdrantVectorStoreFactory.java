package com.example.springai.vectorstore.factory;

import com.example.springai.vectorstore.VectorStoreType;
import com.example.springai.vectorstore.props.QdrantProperties;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.stereotype.Component;

@Component
public class QdrantVectorStoreFactory implements VectorStoreFactory {

    private final QdrantProperties props;
    private final EmbeddingModel embeddingModel;

    public QdrantVectorStoreFactory(QdrantProperties props, EmbeddingModel embeddingModel) {
        this.props = props;
        this.embeddingModel = embeddingModel;
    }

    @Override
    public VectorStoreType getType() {
        return VectorStoreType.QDRANT;
    }

    @Override
    public VectorStore create() {
        QdrantGrpcClient.Builder grpcBuilder = QdrantGrpcClient.newBuilder(props.getHost(), props.getPort(), props.isUseTls());
        if (props.getApiKey() != null && !props.getApiKey().isBlank()) {
            grpcBuilder = grpcBuilder.withApiKey(props.getApiKey());
        }
        QdrantGrpcClient grpcClient = grpcBuilder.build();
        QdrantClient client = new QdrantClient(grpcClient);
        return QdrantVectorStore.builder(client, embeddingModel)
                .collectionName(props.getCollectionName())
                .initializeSchema(props.isInitializeSchema())
                .build();
    }
}
