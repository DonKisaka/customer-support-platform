package com.example.techsupport;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {
    private final VectorStore vectorStore;

    public DocumentService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    public void ingestDocument(Resource file) {
        TikaDocumentReader reader = new TikaDocumentReader(file);
        List<Document> documents = reader.get();

        TokenTextSplitter splitter =TokenTextSplitter.builder().build();
        List<Document> chunks = splitter.apply(documents);

        vectorStore.add(chunks);
    }
}
