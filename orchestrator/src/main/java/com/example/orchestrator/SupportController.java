package com.example.orchestrator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {

    private final ChatClient chatClient;

    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        log.info("Received support request from customer: {}", request.customerId());
        String response = chatClient.prompt()
                .user(request.message())
                .call()
                .content();
        return ResponseEntity.ok(response);
    }

    public record ChatRequest(String customerId, String message) {}
}
