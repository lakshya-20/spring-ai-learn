package com.spring.ollama.service.etl;

import org.springframework.ai.document.Document;

import java.util.List;

public interface DataLoader {

    List<Document> loadDocumentsFromJson();

    List<Document> loadDocumentsFromPdf();

}
