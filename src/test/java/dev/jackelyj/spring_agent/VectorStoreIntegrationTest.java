package dev.jackelyj.spring_agent;

import dev.jackelyj.spring_agent.dto.DocumentSearchResult;
import dev.jackelyj.spring_agent.service.DocumentService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for VectorStore functionality.
 * 
 * These tests require:
 * - PostgreSQL + pgvector running on localhost:5432
 * - Ollama service running with nomic-embed-text model
 * 
 * Run with: ./gradlew test --tests VectorStoreIntegrationTest
 * Or: docker-compose up -d && ./gradlew test --tests VectorStoreIntegrationTest
 */
@SpringBootTest
@ActiveProfiles("postgres")
@Tag("integration")
@EnabledIfEnvironmentVariable(named = "ENABLE_INTEGRATION_TESTS", matches = "true")
class VectorStoreIntegrationTest {
    
    @Autowired(required = false)
    private DocumentService documentService;
    
    @Test
    void contextLoads() {
        // Verify the context loads successfully in postgres profile
        assertThat(documentService).isNotNull();
    }
    
    @Test
    void testAddDocuments() {
        // Given
        List<String> texts = List.of(
            "Spring AI is a framework for building AI applications with Java.",
            "PostgreSQL is a powerful open-source relational database.",
            "pgvector adds vector similarity search to PostgreSQL."
        );
        Map<String, Object> metadata = Map.of(
            "source", "test",
            "category", "technology"
        );
        
        // When
        List<String> ids = documentService.addDocuments(texts, metadata);
        
        // Then
        assertThat(ids).hasSize(3);
        assertThat(ids).allMatch(id -> id != null && !id.isEmpty());
    }
    
    @Test
    void testSearchSimilarDocuments() {
        // Given: Add test documents first
        List<String> texts = List.of(
            "Java is a programming language.",
            "Python is also a programming language.",
            "Coffee is a beverage."
        );
        documentService.addDocuments(texts, Map.of("test", "search"));
        
        // When
        List<DocumentSearchResult> results = documentService.searchSimilar(
            "programming", 2, 0.0);
        
        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSizeLessThanOrEqualTo(2);
        
        // Verify the results contain programming-related content
        assertThat(results)
            .extracting(DocumentSearchResult::content)
            .anyMatch(content -> content.contains("programming"));
    }
    
    @Test
    void testSearchWithFilter() {
        // Given
        List<String> texts = List.of(
            "Spring Boot makes it easy to create applications.",
            "Docker containers are portable and isolated."
        );
        documentService.addDocuments(texts, Map.of("category", "devops"));
        
        documentService.addDocuments(
            List.of("Database optimization is important."),
            Map.of("category", "database")
        );
        
        // When
        List<DocumentSearchResult> results = documentService.searchWithFilter(
            "application", "category == 'devops'", 10);
        
        // Then
        assertThat(results).isNotEmpty();
        
        // All results should have the devops category
        assertThat(results)
            .allMatch(result -> 
                result.metadata() != null && 
                "devops".equals(result.metadata().get("category"))
            );
    }
    
    @Test
    void testDeleteDocuments() {
        // Given: Add documents
        List<String> texts = List.of("Document to delete");
        List<String> ids = documentService.addDocuments(texts, Map.of());
        
        // When
        documentService.deleteDocuments(ids);
        
        // Then: Search should not find the deleted document
        List<DocumentSearchResult> results = documentService.searchSimilar(
            "Document to delete", 10, 0.0);
        
        assertThat(results)
            .noneMatch(result -> ids.contains(result.id()));
    }
    
    @Test
    void testSimilarityScores() {
        // Given
        List<String> texts = List.of(
            "Machine learning is a subset of artificial intelligence.",
            "Deep learning uses neural networks.",
            "Cats are domestic animals."
        );
        documentService.addDocuments(texts, Map.of());
        
        // When
        List<DocumentSearchResult> results = documentService.searchSimilar(
            "artificial intelligence and neural networks", 3, 0.3);
        
        // Then
        assertThat(results).isNotEmpty();
        
        // Verify similarity scores exist and are reasonable
        assertThat(results)
            .allMatch(result -> result.similarityScore() != null);
        
        // The most similar documents should have higher scores
        if (results.size() >= 2) {
            assertThat(results.get(0).content())
                .matches(content -> 
                    content.contains("intelligence") || 
                    content.contains("learning")
                );
        }
    }
}

