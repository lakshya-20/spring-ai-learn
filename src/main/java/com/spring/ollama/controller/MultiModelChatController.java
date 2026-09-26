package com.spring.ollama.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping
public class MultiModelChatController {

    private ChatClient ollamaChatClient;
    private ChatClient googleChatClient;


    public MultiModelChatController(@Qualifier("ollamaChatClient") ChatClient ollamaChatClient,
                                    @Qualifier("googleChatClient") ChatClient googleChatClient) {
        this.ollamaChatClient = ollamaChatClient;
        this.googleChatClient = googleChatClient;
    }

    @GetMapping("/chat")
    public ResponseEntity<String> chat(@RequestParam("query") String query,
                                       @RequestParam("conversationId") String conversationId) {
        String responseContent = this.ollamaChatClient
                .prompt()
                .user(query)
                .system("You are a receptionist")
                .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
        return ResponseEntity.ok(responseContent);
    }

    @GetMapping("/stream")
    public ResponseEntity<Flux<String>> stream(@RequestParam("query") String query,
                                               @RequestParam("conversationId") String conversationId) {
        Flux<String> flux = this.googleChatClient
                .prompt()
                .user(query)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
        return ResponseEntity.ok(flux);
    }
}
