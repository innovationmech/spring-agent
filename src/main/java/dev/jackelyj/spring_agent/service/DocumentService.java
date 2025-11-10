package dev.jackelyj.spring_agent.service;

import dev.jackelyj.spring_agent.dto.DocumentSearchResult;

import java.util.List;
import java.util.Map;

/**
 * Document service interface for managing vector store documents.
 * 
 * This interface follows the Interface Segregation Principle (ISP) by defining
 * only the essential operations for document management.
 * 
 * Clients depend on this abstraction rather than concrete implementations,
 * following the Dependency Inversion Principle (DIP).
 */
public interface DocumentService {
    
    /**
     * Add documents to the vector store with metadata.
     * 
     * @param texts List of document texts to add
     * @param metadata Common metadata to apply to all documents
     * @return List of generated document IDs
     */
    List<String> addDocuments(List<String> texts, Map<String, Object> metadata);
    
    /**
     * Search for documents similar to the given query.
     * 
     * @param query Search query text
     * @param topK Number of top results to return
     * @param threshold Similarity threshold (0.0 to 1.0)
     * @return List of matching documents with similarity scores
     */
    List<DocumentSearchResult> searchSimilar(String query, int topK, double threshold);
    
    /**
     * Search documents with metadata filtering.
     * 
     * @param query Search query text
     * @param filterExpression Filter expression (e.g., "source == 'user_upload'")
     * @param topK Number of top results to return
     * @return List of matching documents with similarity scores
     */
    List<DocumentSearchResult> searchWithFilter(String query, String filterExpression, int topK);
    
    /**
     * Delete documents by their IDs.
     * 
     * @param documentIds List of document IDs to delete
     */
    void deleteDocuments(List<String> documentIds);
    
    /**
     * Delete documents matching a filter expression.
     * 
     * @param filterExpression Filter expression for documents to delete
     */
    void deleteByFilter(String filterExpression);
    
    /**
     * Get the total count of documents in the vector store.
     * 
     * @return Total number of documents
     */
    long getDocumentCount();
}

