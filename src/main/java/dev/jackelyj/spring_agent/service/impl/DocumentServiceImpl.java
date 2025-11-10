package dev.jackelyj.spring_agent.service.impl;

import dev.jackelyj.spring_agent.dto.DocumentSearchResult;
import dev.jackelyj.spring_agent.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of DocumentService for managing vector store documents.
 * 
 * SOLID Principles:
 * - SRP: Only responsible for document management logic
 * - OCP: Extensible through the DocumentService interface
 * - LSP: Fully substitutable for DocumentService interface
 * - DIP: Depends on VectorStore abstraction, not concrete implementation
 */
@Service
@ConditionalOnBean(VectorStore.class)
public class DocumentServiceImpl implements DocumentService {
    
    private static final Logger log = LoggerFactory.getLogger(DocumentServiceImpl.class);
    
    private final VectorStore vectorStore;
    
    public DocumentServiceImpl(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }
    
    @Override
    public List<String> addDocuments(List<String> texts, Map<String, Object> metadata) {
        log.info("Adding {} documents to vector store", texts.size());
        
        List<Document> documents = texts.stream()
                .map(text -> {
                    String id = UUID.randomUUID().toString();
                    return Document.builder()
                            .id(id)
                            .text(text)
                            .metadata(metadata)
                            .build();
                })
                .collect(Collectors.toList());
        
        vectorStore.add(documents);
        
        List<String> ids = documents.stream()
                .map(Document::getId)
                .collect(Collectors.toList());
        
        log.info("Successfully added {} documents with IDs: {}", ids.size(), ids);
        return ids;
    }
    
    @Override
    public List<DocumentSearchResult> searchSimilar(String query, int topK, double threshold) {
        log.debug("Searching for similar documents: query='{}', topK={}, threshold={}", 
                 query, topK, threshold);
        
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold)
                .build();
        
        List<Document> results = vectorStore.similaritySearch(request);
        
        log.debug("Found {} similar documents", results.size());
        
        return convertToSearchResults(results);
    }
    
    @Override
    public List<DocumentSearchResult> searchWithFilter(String query, String filterExpression, int topK) {
        log.debug("Searching with filter: query='{}', filter='{}', topK={}", 
                 query, filterExpression, topK);
        
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .filterExpression(filterExpression)
                .build();
        
        List<Document> results = vectorStore.similaritySearch(request);
        
        log.debug("Found {} documents matching filter", results.size());
        
        return convertToSearchResults(results);
    }
    
    @Override
    public void deleteDocuments(List<String> documentIds) {
        log.info("Deleting {} documents by IDs", documentIds.size());
        vectorStore.delete(documentIds);
        log.info("Successfully deleted documents");
    }
    
    @Override
    public void deleteByFilter(String filterExpression) {
        log.info("Deleting documents by filter: {}", filterExpression);
        
        // First, search for documents matching the filter to get their IDs
        SearchRequest searchRequest = SearchRequest.builder()
                .query("")  // Empty query to match all
                .topK(10000)  // Large number to get all matches
                .filterExpression(filterExpression)
                .build();
        
        List<Document> matchingDocs = vectorStore.similaritySearch(searchRequest);
        
        if (!matchingDocs.isEmpty()) {
            List<String> ids = matchingDocs.stream()
                    .map(Document::getId)
                    .collect(Collectors.toList());
            
            vectorStore.delete(ids);
            log.info("Deleted {} documents matching filter", ids.size());
        } else {
            log.info("No documents found matching filter");
        }
    }
    
    @Override
    public long getDocumentCount() {
        // Note: VectorStore interface doesn't provide a count method
        // This is a limitation we'll need to work around
        log.warn("Document count is not directly supported by VectorStore interface");
        return -1;  // Indicates not available
    }
    
    /**
     * Convert Spring AI Documents to our DTO.
     */
    private List<DocumentSearchResult> convertToSearchResults(List<Document> documents) {
        return documents.stream()
                .map(doc -> {
                    // Extract similarity score from metadata if available
                    Double score = null;
                    if (doc.getMetadata() != null && doc.getMetadata().containsKey("distance")) {
                        Object distance = doc.getMetadata().get("distance");
                        if (distance instanceof Number) {
                            // Convert distance to similarity (1 - distance for cosine)
                            score = 1.0 - ((Number) distance).doubleValue();
                        }
                    }
                    
                    return DocumentSearchResult.of(
                            doc.getId(),
                            doc.getText(),
                            doc.getMetadata(),
                            score
                    );
                })
                .collect(Collectors.toList());
    }
}

