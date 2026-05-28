package com.example.techsupport;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TechSupportTools {

    private final VectorStore vectorStore;

    @Tool(description = "Search product documentation to answer technical questions. " +
            "Use this to find relevant information about product features, setup, troubleshooting, and usage.")
    public String searchProductDocs(
            @ToolParam(description = "The technical question or search query") String query) {

        List<Document> results = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(3)
                        .build()
        );

        if (results.isEmpty()) {
            return "No relevant documentation found for: " + query;
        }

        StringBuilder response = new StringBuilder();
        for (int i = 0; i < results.size(); i++) {
            response.append("--- Result ").append(i + 1).append(" ---\n");
            response.append(results.get(i).getText()).append("\n\n");
        }
        return response.toString();
    }
}
