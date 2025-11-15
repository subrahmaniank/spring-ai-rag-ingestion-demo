package com.example.springai.vectorstore.props;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.ai.vectorstore.chroma")
public class ChromaProperties {

    public static class Client {
        private String host = "http://localhost";
        private int port = 8000;
        private String keyToken;
        private String username;
        private String password;

        public String getHost() { return host; }
        public void setHost(String host) { this.host = host; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getKeyToken() { return keyToken; }
        public void setKeyToken(String keyToken) { this.keyToken = keyToken; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    private Client client = new Client();

    private String tenantName = "SpringAiTenant";
    private String databaseName = "SpringAiDatabase";
    private String collectionName = "documents";
    private boolean initializeSchema = true;

    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }

    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
    public String getDatabaseName() { return databaseName; }
    public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
    public String getCollectionName() { return collectionName; }
    public void setCollectionName(String collectionName) { this.collectionName = collectionName; }
    public boolean isInitializeSchema() { return initializeSchema; }
    public void setInitializeSchema(boolean initializeSchema) { this.initializeSchema = initializeSchema; }
}
