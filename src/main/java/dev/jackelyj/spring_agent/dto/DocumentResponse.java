package dev.jackelyj.spring_agent.dto;

import java.util.List;

/**
 * Response DTO for document operations.
 * 
 * This record follows the Single Responsibility Principle (SRP)
 * by only representing the document operation response.
 */
public record DocumentResponse(
    boolean success,
    String message,
    List<String> documentIds,
    int count
) {
    /**
     * Create a success response.
     */
    public static DocumentResponse success(String message, List<String> documentIds) {
        return new DocumentResponse(true, message, documentIds, documentIds.size());
    }
    
    /**
     * Create an error response.
     */
    public static DocumentResponse error(String message) {
        return new DocumentResponse(false, message, List.of(), 0);
    }
}

