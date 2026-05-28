package com.example.techsupport;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder.build();
    }

    @Bean
    ChatClientCustomizer addSystemPrompt() {
        return builder -> builder.defaultSystem("""
                You are a technical support agent for SmartFlow CRM.
                Your role is to answer technical questions about the product using the available documentation.
                Always use the searchProductDocs tool to find relevant information before answering.
                If the documentation does not contain the answer, clearly say so and advise the customer to contact support.
                Be concise, accurate, and professional.
                """);
    }

    @Bean
    ChatClientCustomizer addTechSupportTools(TechSupportTools techSupportTools) {
        return builder -> builder.defaultTools(techSupportTools);
    }
}
