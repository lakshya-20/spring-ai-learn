package com.spring.ollama.service;

import com.spring.ollama.service.etl.DataLoader;
import com.spring.ollama.service.etl.DataTransformer;
import com.spring.ollama.utils.KnowledgeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VectorStoreService {

    private final VectorStore vectorStore;

    private final ChatClient chatClient;

    private final DataLoader dataLoader;

    private final DataTransformer dataTransformer;

    private Logger logger = LoggerFactory.getLogger(VectorStoreService.class);

    public VectorStoreService(VectorStore vectorStore,
                              @Qualifier("ollamaChatClient") ChatClient chatClient,
                              DataLoader dataLoader,
                              DataTransformer dataTransformer) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient;
        this.dataLoader = dataLoader;
        this.dataTransformer = dataTransformer;
    }

    public boolean load() {
        try {
            logger.info("Loading vectorDB");
            List<String> list = KnowledgeHelper.getData();
            List<Document> documents = list.stream().map(Document::new).toList();
            this.vectorStore.add(documents);
            logger.info("Loaded vectorDB");
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public String chat(String query, String conversationId) {
        /**
         * Below code is commented out because QuestionAnswerAdvisor is
         * configured at OllamaChatClient config
         */
//        SearchRequest searchRequest = SearchRequest.builder()
//                .topK(5)
//                .similarityThreshold(0.6)
//                .query(query)
//                .build();
//        List<Document> documents = this.vectorStore.similaritySearch(searchRequest);
//        List<String> documentList = documents.stream().map(Document::toString).toList();
//        String context = String.join(",", documentList);
//
//        logger.info("Vector store context: " + context);

        return this.chatClient.prompt()
                .user(query)
                .system("If relevant context is provided, prioritize it when answering.\n" +
                        "If no relevant context is provided, use your general knowledge to answer the user's question.")
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    public boolean loadJsonDocuments() {
        try {
            List<Document> jsonDocuments = this.dataLoader.loadDocumentsFromJson();
            List<Document> transformedJsonDocuments = this.dataTransformer.transform(jsonDocuments);
            this.vectorStore.add(transformedJsonDocuments);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean loadPdfDocuments() {
        try {
            List<Document> pdfDocuments = this.dataLoader.loadDocumentsFromPdf();
            List<Document> transformedPdfDocuments = this.dataTransformer.transform(pdfDocuments);
            this.vectorStore.add(transformedPdfDocuments);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
