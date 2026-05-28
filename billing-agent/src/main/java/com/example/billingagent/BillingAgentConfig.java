package com.example.billingagent;

import io.a2a.spec.AgentCapabilities;
import io.a2a.spec.AgentCard;
import io.a2a.spec.AgentSkill;
import io.a2a.server.agentexecution.AgentExecutor;
import org.springaicommunity.a2a.server.executor.DefaultAgentExecutor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BillingAgentConfig {

    @Bean
    public AgentCard agentCard(
            @Value("${server.port:8081}") int port,
            @Value("${a2a.agent.name}") String name,
            @Value("${a2a.agent.description}") String description,
            @Value("${a2a.agent.version}") String version) {

        return new AgentCard.Builder()
                .name(name)
                .description(description)
                .url("http://localhost:" + port + "/a2a/")
                .version(version)
                .capabilities(new AgentCapabilities.Builder().streaming(false).build())
                .defaultInputModes(List.of("text"))
                .defaultOutputModes(List.of("text"))
                .skills(List.of(new AgentSkill.Builder()
                        .id("billing_agent")
                        .name("Billing Support Agent")
                        .description("Handles billing, invoice, and payment queries")
                        .tags(List.of("billing", "invoice", "payment"))
                        .build()))
                .protocolVersion("0.3.0")
                .build();
    }

    @Bean
    public AgentExecutor agentExecutor(ChatClient chatClient) {
        return new DefaultAgentExecutor(chatClient, (chat, ctx) -> {
            String userMessage = DefaultAgentExecutor.extractTextFromMessage(ctx.getMessage());
            return chat.prompt().user(userMessage).call().content();
        });
    }
}
