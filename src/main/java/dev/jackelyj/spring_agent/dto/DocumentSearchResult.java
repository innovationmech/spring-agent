package dev.jackelyj.spring_agent.dto;

import java.util.Map;

/**
 * Result DTO for document similarity search.
 * 
 * This record follows the Single Responsibility Principle (SRP)
 * by only representing a single search result.
 */
public record DocumentSearchResult(
    String id,
    String content,
    Map<String, Object> metadata,
    Double similarityScore
) {
    /**
     * Create a result from document data.
     */
    public static DocumentSearchResult of(String id, String content, 
                                         Map<String, Object> metadata, 
                                         Double score) {
        return new DocumentSearchResult(id, content, metadata, score);
    }
}

