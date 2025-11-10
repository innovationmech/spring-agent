package dev.jackelyj.spring_agent.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for adding documents to the vector store.
 * 
 * This record follows the Single Responsibility Principle (SRP)
 * by only representing the document upload request data.
 */
public record DocumentRequest(
    @NotNull(message = "Texts cannot be null")
    @NotEmpty(message = "At least one text must be provided")
    List<String> texts,
    
    Map<String, Object> metadata
) {
    /**
     * Convenience constructor with no metadata.
     */
    public DocumentRequest(List<String> texts) {
        this(texts, Map.of());
    }
}

