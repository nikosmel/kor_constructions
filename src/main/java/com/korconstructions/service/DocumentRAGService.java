package com.korconstructions.service;

import com.korconstructions.model.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentRAGService {

    private final VectorStore vectorStore;
    private final ChatClient.Builder chatClientBuilder;
    private final DocumentTextExtractor textExtractor;

    /**
     * Index a document by extracting text and storing embeddings in vector database
     * @return the extracted text for storage in the database
     */
    public String indexDocument(Document document, File file) {
        log.info("Indexing document: {} ({})", document.getTitle(), file.getName());

        // Extract text from document
        String extractedText = textExtractor.extractText(file);

        if (extractedText == null || extractedText.trim().isEmpty()) {
            log.error("No text extracted from document: {} - File exists: {}, Size: {}",
                document.getTitle(), file.exists(), file.length());
            throw new RuntimeException("Failed to extract text from document. The file may be empty, corrupted, or in an unsupported format.");
        }

        log.info("Extracted {} characters from document: {}", extractedText.length(), document.getTitle());

        // Split text into chunks (800 chars with 150 char overlap for better context)
        List<String> chunks = splitIntoChunks(extractedText, 800);
        log.info("Split into {} chunks for document: {}", chunks.size(), document.getTitle());

        // Create AI documents with metadata
        List<org.springframework.ai.document.Document> aiDocuments = chunks.stream()
            .map(chunk -> {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("document_id", document.getId().toString());
                metadata.put("title", document.getTitle());
                metadata.put("type", document.getType().toString());
                if (document.getBuilding() != null) {
                    metadata.put("building_id", document.getBuilding().getId().toString());
                    metadata.put("building_name", document.getBuilding().getName());
                }

                return new org.springframework.ai.document.Document(chunk, metadata);
            })
            .collect(Collectors.toList());

        // Store in vector database in smaller batches to avoid token limits
        int batchSize = 10;
        int totalChunks = aiDocuments.size();
        log.info("=== Starting vector indexing ===");
        log.info("Total chunks to index: {}, Batch size: {}", totalChunks, batchSize);

        for (int i = 0; i < totalChunks; i += batchSize) {
            int endIndex = Math.min(i + batchSize, totalChunks);
            List<org.springframework.ai.document.Document> batch = aiDocuments.subList(i, endIndex);
            int batchNum = (i / batchSize) + 1;
            int totalBatches = (totalChunks + batchSize - 1) / batchSize;

            try {
                log.info("Processing batch {}/{} (chunks {}-{})...",
                    batchNum, totalBatches, i, endIndex - 1);

                vectorStore.add(batch);

                log.info("✓ Batch {}/{} indexed successfully ({} chunks)",
                    batchNum, totalBatches, batch.size());
            } catch (Exception e) {
                log.error("✗ Failed to index batch {}/{} (chunks {}-{}): {} - {}",
                    batchNum, totalBatches, i, endIndex - 1,
                    e.getClass().getSimpleName(), e.getMessage());
                log.error("Full error:", e);
                throw new RuntimeException("Failed to create embeddings for document batch " +
                    batchNum + "/" + totalBatches + ". " + e.getMessage(), e);
            }
        }

        log.info("Successfully indexed {} chunks in {} batches for document: {}",
            chunks.size(), (totalChunks + batchSize - 1) / batchSize, document.getTitle());

        return extractedText;
    }

    /**
     * Delete document embeddings from vector store
     */
    public void deleteDocumentEmbeddings(Long documentId) {
        // Note: Spring AI VectorStore doesn't have built-in delete by metadata
        // This would require a custom implementation or database-level deletion
        // For now, we'll log it - you can implement custom deletion logic later
        log.info("Delete request for document embeddings: {}", documentId);
    }

    /**
     * Ask a question and get AI-powered answer based on document context
     */
    public String askQuestion(String question, Long buildingId) {
        log.info("=== Starting AI Question Processing ===");
        log.info("Question: '{}'", question);
        if (buildingId != null) {
            log.info("Filtering for building ID: {}", buildingId);
        }

        // Search for relevant document chunks
        // Higher topK and lower threshold to capture related information across chunks
        SearchRequest searchRequest = SearchRequest.query(question)
            .withTopK(15)
            .withSimilarityThreshold(0.4);

        log.info("Searching vector store with topK=15, similarityThreshold=0.4...");
        List<org.springframework.ai.document.Document> relevantDocs = vectorStore.similaritySearch(searchRequest);
        log.info("Vector search returned {} chunks", relevantDocs.size());

        // Hybrid approach: If vector search returns few results, try keyword search
        if (relevantDocs.size() < 5) {
            log.info("Vector search returned few results, trying keyword-based fallback...");
            List<org.springframework.ai.document.Document> keywordResults = keywordSearch(question);
            log.info("Keyword search returned {} additional chunks", keywordResults.size());

            // Merge results, avoiding duplicates
            for (org.springframework.ai.document.Document keywordDoc : keywordResults) {
                boolean alreadyExists = relevantDocs.stream()
                    .anyMatch(doc -> doc.getId().equals(keywordDoc.getId()));
                if (!alreadyExists) {
                    relevantDocs.add(keywordDoc);
                }
            }
            log.info("After merging: {} total chunks", relevantDocs.size());
        }

        if (relevantDocs.isEmpty()) {
            log.warn("No relevant documents found for question");
            return "Δεν βρέθηκαν σχετικά έγγραφα για την ερώτησή σας.";
        }

        // Log details about retrieved chunks
        log.info("--- Retrieved Chunks ---");
        for (int i = 0; i < relevantDocs.size(); i++) {
            org.springframework.ai.document.Document doc = relevantDocs.get(i);
            String title = (String) doc.getMetadata().get("title");
            String docId = (String) doc.getMetadata().get("document_id");
            String contentPreview = doc.getContent().substring(0, Math.min(100, doc.getContent().length()));
            log.info("Chunk {}: Doc='{}' (ID={}), Content='{}'...",
                i + 1, title, docId, contentPreview.replace("\n", " "));
        }

        // Filter by building if specified
        int beforeFilter = relevantDocs.size();
        if (buildingId != null) {
            relevantDocs = relevantDocs.stream()
                .filter(doc -> {
                    Object bldgId = doc.getMetadata().get("building_id");
                    return bldgId != null && bldgId.toString().equals(buildingId.toString());
                })
                .collect(Collectors.toList());
            log.info("After building filter: {} -> {} chunks", beforeFilter, relevantDocs.size());
        }

        if (relevantDocs.isEmpty()) {
            log.warn("No documents found after building filter");
            return "Δεν βρέθηκαν σχετικά έγγραφα για το συγκεκριμένο κτίριο.";
        }

        // Build context from relevant documents
        log.info("Building context from {} chunks...", relevantDocs.size());
        String context = relevantDocs.stream()
            .map(doc -> {
                String title = (String) doc.getMetadata().get("title");
                return "Από έγγραφο '" + title + "':\n" + doc.getContent();
            })
            .collect(Collectors.joining("\n\n---\n\n"));

        int contextLength = context.length();
        log.info("Context built: {} characters", contextLength);

        // Create prompt for AI
        String prompt = String.format("""
            Βασιζόμενος στα παρακάτω έγγραφα, απάντησε στην ερώτηση του χρήστη.
            Αν η απάντηση δεν βρίσκεται στα έγγραφα, πες το ξεκάθαρα.

            ΕΓΓΡΑΦΑ:
            %s

            ΕΡΩΤΗΣΗ:
            %s

            ΑΠΑΝΤΗΣΗ:
            """, context, question);

        log.info("Prompt created: {} ", prompt);
        log.info("Prompt created: {} characters total", prompt.length());
        log.info("Sending request to OpenAI GPT-4...");

        // Get AI response
        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
            .user(prompt)
            .call()
            .content();

        log.info("✓ AI response received: {} characters", response.length());
        log.info("Response preview: '{}'...",
            response.substring(0, Math.min(150, response.length())).replace("\n", " "));
        log.info("=== Question Processing Complete ===");

        return response;
    }

    /**
     * Split text into chunks for better vector search
     * Uses multiple strategies to ensure proper chunking
     */
    private List<String> splitIntoChunks(String text, int maxChunkSize) {
        List<String> chunks = new java.util.ArrayList<>();

        // Add overlap between chunks for better context (helps with split information)
        int overlap = 150;

        log.info("Splitting text of {} characters into chunks of max {} chars", text.length(), maxChunkSize);

        // If text is smaller than max chunk size, return as single chunk
        if (text.length() <= maxChunkSize) {
            log.info("Text fits in single chunk");
            chunks.add(text);
            return chunks;
        }

        // Try splitting by paragraphs first (double newline)
        String[] paragraphs = text.split("\n\n");
        log.info("Found {} paragraphs", paragraphs.length);

        // If we have good paragraphs, use them
        if (paragraphs.length > 1) {
            StringBuilder currentChunk = new StringBuilder();

            for (String paragraph : paragraphs) {
                // If adding this paragraph exceeds size, save current chunk
                if (currentChunk.length() + paragraph.length() > maxChunkSize && currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk = new StringBuilder();

                    // Add overlap from previous chunk
                    if (chunks.size() > 0) {
                        String lastChunk = chunks.get(chunks.size() - 1);
                        int overlapStart = Math.max(0, lastChunk.length() - overlap);
                        currentChunk.append(lastChunk.substring(overlapStart)).append(" ");
                    }
                }

                // If single paragraph is too large, split it further
                if (paragraph.length() > maxChunkSize) {
                    // Split by sentences or force split
                    String[] sentences = paragraph.split("\\. ");
                    for (String sentence : sentences) {
                        if (currentChunk.length() + sentence.length() > maxChunkSize && currentChunk.length() > 0) {
                            chunks.add(currentChunk.toString().trim());
                            currentChunk = new StringBuilder();
                        }
                        currentChunk.append(sentence).append(". ");
                    }
                } else {
                    currentChunk.append(paragraph).append("\n\n");
                }
            }

            if (currentChunk.length() > 0) {
                chunks.add(currentChunk.toString().trim());
            }
        } else {
            // No clear paragraphs, force split by character count
            log.warn("No paragraph breaks found, using character-based splitting");
            int position = 0;
            int chunkCount = 0;

            while (position < text.length()) {
                int end = Math.min(position + maxChunkSize, text.length());

                // Try to break at a space if possible (but not too far back)
                if (end < text.length()) {
                    int searchStart = Math.max(position, end - 100); // Look back max 100 chars
                    int lastSpace = text.lastIndexOf(' ', end);
                    if (lastSpace > searchStart) {
                        end = lastSpace + 1; // Include the space
                    }
                }

                String chunk = text.substring(position, end).trim();
                if (!chunk.isEmpty()) {
                    chunks.add(chunk);
                    chunkCount++;
                }

                // Move position forward, with overlap
                int nextPosition = end - overlap;

                // Prevent infinite loop: ensure we always move forward
                if (nextPosition <= position) {
                    nextPosition = end;
                }

                position = nextPosition;

                // Safety check to prevent infinite loops
                if (chunkCount > 1000) {
                    log.error("Too many chunks created ({}), stopping to prevent memory issues", chunkCount);
                    break;
                }
            }
        }

        log.info("Created {} chunks from text", chunks.size());
        return chunks;
    }

    /**
     * Keyword-based search fallback for Greek text
     * Extracts Greek words from question and searches vector store content
     */
    private List<org.springframework.ai.document.Document> keywordSearch(String question) {
        // Extract Greek keywords (words with 4+ characters, ignoring common words)
        String[] commonWords = {"μπορεις", "πεις", "λεει", "συγγραφη", "υποχρεωσεων", "για", "τις", "τους", "την", "του", "στην", "στο", "στον", "και", "ειναι", "θα", "με", "από", "που", "αυτο", "αυτη", "αυτος"};

        String[] words = question.toLowerCase()
            .replaceAll("[.,;:!?]", "")
            .split("\\s+");

        List<String> keywords = new java.util.ArrayList<>();
        for (String word : words) {
            if (word.length() >= 4 && !java.util.Arrays.asList(commonWords).contains(word)) {
                keywords.add(word);
            }
        }

        log.info("Extracted keywords: {}", keywords);

        if (keywords.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        // Search vector store directly via similarity search with extracted keywords
        // This is a simplified approach - in production you'd use a custom repository query
        List<org.springframework.ai.document.Document> results = new java.util.ArrayList<>();

        for (String keyword : keywords) {
            try {
                SearchRequest keywordRequest = SearchRequest.query(keyword)
                    .withTopK(5)
                    .withSimilarityThreshold(0.3);
                List<org.springframework.ai.document.Document> keywordDocs = vectorStore.similaritySearch(keywordRequest);

                for (org.springframework.ai.document.Document doc : keywordDocs) {
                    // Check if content actually contains the keyword (case-insensitive)
                    if (doc.getContent().toLowerCase().contains(keyword)) {
                        boolean exists = results.stream()
                            .anyMatch(r -> r.getId().equals(doc.getId()));
                        if (!exists) {
                            results.add(doc);
                            log.info("Found chunk via keyword '{}': {}", keyword,
                                doc.getContent().substring(0, Math.min(80, doc.getContent().length())));
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Keyword search failed for '{}': {}", keyword, e.getMessage());
            }
        }

        return results;
    }
}
