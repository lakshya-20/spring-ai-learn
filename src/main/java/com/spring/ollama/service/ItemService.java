package com.spring.ollama.service;

import com.spring.ollama.model.Item;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ItemService {
    private ChatClient chatClient;

    public ItemService(@Qualifier("ollamaChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public List<Item> getItems(String query) {

        return this.chatClient
                .prompt(query)
                .call()
                .entity(new ParameterizedTypeReference<List<Item>>() {
                });
    }
}
