package com.spring.ollama.controller;

import com.spring.ollama.service.VectorStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vector-store")
@RequiredArgsConstructor
public class VectorStoreController {

    @Autowired
    private VectorStoreService vectorStoreService;

    @PostMapping("/load")
    public String load() {
        if(this.vectorStoreService.load()) return "Data Loaded";
        else return "Some error occurred";
    }

    @PostMapping("/chat")
    public String chat(@RequestParam("query") String query, @RequestParam("conversationId") String conversationId) {
        return this.vectorStoreService.chat(query, conversationId);
    }

    @PostMapping("/load/json")
    public String loadJsonDocuments() {
        if(this.vectorStoreService.loadJsonDocuments()) return "Json Documents Loaded";
        else return "Something went wrong";
    }

    @PostMapping("/load/pdf")
    public String loadPdfDocuments() {
        if(this.vectorStoreService.loadPdfDocuments()) return "PDF Documents Loaded";
        else return "Something went wrong";
    }

}
