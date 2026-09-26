package com.spring.ollama.service.etl;

import org.springframework.ai.document.Document;

import java.util.List;

public interface DataTransformer {

    List<Document> transform(List<Document> documents);

}
