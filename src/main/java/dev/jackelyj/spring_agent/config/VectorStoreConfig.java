package dev.jackelyj.spring_agent.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Configuration for PgVector Vector Store and Embedding Model.
 * 
 * SOLID Principles:
 * - SRP: Only responsible for configuring vector store beans
 * - OCP: Can be extended with additional vector store configurations
 * - DIP: Provides VectorStore and EmbeddingModel abstractions
 */
@Configuration
@ConditionalOnProperty(name = "spring.datasource.url", matchIfMissing = false)
public class VectorStoreConfig {
    
    @Value("${spring.ai.vectorstore.pgvector.dimensions:1024}")
    private int dimensions;
    
    @Value("${spring.ai.vectorstore.pgvector.schema-name:public}")
    private String schemaName;
    
    @Value("${spring.ai.vectorstore.pgvector.table-name:vector_store}")
    private String tableName;
    
    @Value("${spring.ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;
    
    /**
     * Configure Ollama Embedding Model.
     * Uses nomic-embed-text model with 1024 dimensions.
     */
    @Bean
    public EmbeddingModel embeddingModel() {
        OllamaApi ollamaApi = OllamaApi.builder()
                .baseUrl(ollamaBaseUrl)
                .build();
        
        return OllamaEmbeddingModel.builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaOptions.builder()
                        .model("nomic-embed-text")
                        .build())
                .build();
    }
    
    /**
     * Configure PgVector Vector Store.
     * 
     * This bean provides vector similarity search capabilities using PostgreSQL + pgvector.
     */
    @Bean
    public VectorStore vectorStore(JdbcTemplate jdbcTemplate, EmbeddingModel embeddingModel) {
        return PgVectorStore.builder(jdbcTemplate, embeddingModel)
                .dimensions(dimensions)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .initializeSchema(true)
                .schemaName(schemaName)
                .vectorTableName(tableName)
                .maxDocumentBatchSize(10000)
                .build();
    }
}

