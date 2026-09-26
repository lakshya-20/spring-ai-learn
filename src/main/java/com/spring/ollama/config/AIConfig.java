package com.spring.ollama.config;

import com.spring.ollama.advisor.TokenPrinterAdvisor;
import com.spring.ollama.service.tools.SimpleDateTimeTool;
import com.spring.ollama.utils.MyLoggingAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
//import org.springframework.ai.chat.memory.MessageWindowChatMemory;
//import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class AIConfig {

//    @Bean(name = "jdbcChatMemory")
//    public ChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository) {
//        return MessageWindowChatMemory.builder()
//                .chatMemoryRepository(jdbcChatMemoryRepository)
//                .build();
//    }

    @Bean(name = "ollamaChatClient")
    public ChatClient ollamaChatClient(OllamaChatModel ollamaChatModel,
                                       ChatMemory chatMemory,
                                       VectorStore vectorStore,
                                       SyncMcpToolCallbackProvider syncMcpToolCallbackProvider) {

        MessageChatMemoryAdvisor messageChatMemoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory).build();

        QuestionAnswerAdvisor questionAnswerAdvisor = getSimpleRAGFlowAdvisor(vectorStore);

        RetrievalAugmentationAdvisor retrievalAugmentationAdvisor = getAdvancedRAGFlowAdvisor(vectorStore, ollamaChatModel);


        return ChatClient.builder(ollamaChatModel)
                .defaultAdvisors(
                        new TokenPrinterAdvisor(),
                        messageChatMemoryAdvisor,
                        retrievalAugmentationAdvisor,
                        MyLoggingAdvisor.builder().build())
                .defaultTools(new SimpleDateTimeTool(), syncMcpToolCallbackProvider)
                .build();
    }

    @Bean(name = "googleChatClient")
    public ChatClient googleChatClient(GoogleGenAiChatModel googleGenAiChatModel) {
        return ChatClient.builder(googleGenAiChatModel)
                .defaultAdvisors(new SafeGuardAdvisor(List.of("python", "Python")))
                .build();
    }


    private QuestionAnswerAdvisor getSimpleRAGFlowAdvisor(VectorStore vectorStore) {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(5).similarityThreshold(0.6).build())
                .build();
    }

    private RetrievalAugmentationAdvisor getAdvancedRAGFlowAdvisor(VectorStore vectorStore,
                                                                   OllamaChatModel ollamaChatModel) {
        ChatClient.Builder chatClientBuilder = ChatClient.builder(ollamaChatModel);
        return RetrievalAugmentationAdvisor.builder()
                .queryTransformers(
                        RewriteQueryTransformer.builder().chatClientBuilder(chatClientBuilder).build(),
                        TranslationQueryTransformer.builder().chatClientBuilder(chatClientBuilder).targetLanguage("English").build()
                )
                .queryExpander(MultiQueryExpander.builder().chatClientBuilder(chatClientBuilder).numberOfQueries(3).build())
                .documentRetriever(
                        VectorStoreDocumentRetriever.builder()
                                .topK(5)
                                .similarityThreshold(0.6)
                                .vectorStore(vectorStore)
                                .build()
                )
                .documentJoiner(new ConcatenationDocumentJoiner())
                .queryAugmenter(ContextualQueryAugmenter.builder().allowEmptyContext(true).build())
                .build();

    }


}
