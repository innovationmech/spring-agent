package dev.jackelyj.spring_agent.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for document search operations.
 * 
 * This record follows the Single Responsibility Principle (SRP)
 * by only representing the search request parameters.
 */
public record DocumentSearchRequest(
    @NotBlank(message = "Query cannot be blank")
    String query,
    
    @Min(value = 1, message = "topK must be at least 1")
    @Max(value = 100, message = "topK cannot exceed 100")
    Integer topK,
    
    @Min(value = 0, message = "Threshold must be between 0 and 1")
    @Max(value = 1, message = "Threshold must be between 0 and 1")
    Double threshold,
    
    String filterExpression
) {
    /**
     * Default constructor with standard values.
     */
    public DocumentSearchRequest(String query) {
        this(query, 5, 0.0, null);
    }
    
    /**
     * Constructor with topK.
     */
    public DocumentSearchRequest(String query, Integer topK) {
        this(query, topK, 0.0, null);
    }
}

