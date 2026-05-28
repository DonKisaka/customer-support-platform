package com.example.escalationagent;

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
                You are an escalation support agent for SmartFlow CRM.
                Your role is to handle customer issues that could not be resolved by billing or tech support agents.
                Always collect the customer ID and a clear description of their issue before creating a ticket.
                Assign priority based on impact: CRITICAL for system outages, HIGH for data loss, MEDIUM for feature issues, LOW for general inquiries.
                After creating a ticket, reassure the customer that a human agent will follow up shortly.
                Be empathetic, professional, and clear in your communication.
                """);
    }

    @Bean
    ChatClientCustomizer addEscalationTools(EscalationTools escalationTools) {
        return builder -> builder.defaultTools(escalationTools);
    }
}
