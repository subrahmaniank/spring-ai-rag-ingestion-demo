package com.example.springai.vectorstore.props;

import com.example.springai.vectorstore.VectorStoreType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.vectorstore")
public class VectorStoreProviderProperties {
    /**
     * Provider to use: QDRANT or CHROMA
     */
    private VectorStoreType provider = VectorStoreType.QDRANT;

    public VectorStoreType getProvider() {
        return provider;
    }

    public void setProvider(VectorStoreType provider) {
        this.provider = provider;
    }
}
