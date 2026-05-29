package com.example.orchestrator;

import org.springaicommunity.agent.subagent.a2a.A2ASubagentDefinition;
import org.springaicommunity.agent.subagent.a2a.A2ASubagentExecutor;
import org.springaicommunity.agent.subagent.a2a.A2ASubagentResolver;
import org.springaicommunity.agent.common.task.subagent.SubagentReference;
import org.springaicommunity.agent.common.task.subagent.SubagentType;
import org.springaicommunity.agent.tools.task.TaskTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientCustomizer;
import org.springframework.beans.factory.annotation.Value;
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
                You are the main customer support orchestrator for SmartFlow CRM.
                Your role is to understand the customer's issue and route it to the correct specialist agent:
                - Use the billing agent for billing, invoices, payments, or refund questions.
                - Use the tech support agent for technical questions, product usage, setup, or troubleshooting.
                - Use the escalation agent for complex unresolved issues, complaints, or anything requiring human intervention.
                Always greet the customer, understand their issue fully, then route to the appropriate agent.
                Return the specialist agent's response directly to the customer.
                """);
    }

    @Bean
    ChatClientCustomizer addTaskTool(
            @Value("${agents.billing.url}") String billingUrl,
            @Value("${agents.tech-support.url}") String techSupportUrl,
            @Value("${agents.escalation.url}") String escalationUrl) {
        return builder -> {
            var taskTool = TaskTool.builder()
                    .subagentReferences(
                            new SubagentReference(billingUrl, A2ASubagentDefinition.KIND),
                            new SubagentReference(techSupportUrl, A2ASubagentDefinition.KIND),
                            new SubagentReference(escalationUrl, A2ASubagentDefinition.KIND))
                    .subagentTypes(
                            new SubagentType(
                                    new A2ASubagentResolver(),
                                    new A2ASubagentExecutor()))
                    .build();
            builder.defaultToolCallbacks(taskTool);
        };
    }
}
