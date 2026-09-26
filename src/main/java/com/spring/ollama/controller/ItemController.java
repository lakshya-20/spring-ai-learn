package com.spring.ollama.controller;

import com.spring.ollama.model.Item;
import com.spring.ollama.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/item")
@RequiredArgsConstructor
public class ItemController {

    @Autowired
    private final ItemService itemService;

    @GetMapping("/list")
    public List<Item> chat(@RequestParam("q") String query) {
        return itemService.getItems(query);
    }

}
