# AI Customer Support Platform

An AI-powered customer support platform built with Spring Boot and Spring AI, demonstrating **Agent-to-Agent (A2A) communication**. A central orchestrator routes customer queries to specialist sub-agents based on the nature of the request.

---

## Architecture

```
Customer
   │
   ▼
┌─────────────────────────────┐
│     Orchestrator (8080)     │  ← Entry point — routes via TaskTool + A2A
└────────────┬────────────────┘
             │
     ┌───────┼───────────┐
     ▼       ▼           ▼
┌─────────┐ ┌──────────┐ ┌────────────┐
│ Billing │ │   Tech   │ │ Escalation │
│  Agent  │ │ Support  │ │   Agent    │
│ (8081)  │ │  (8082)  │ │  (8083)    │
└────┬────┘ └────┬─────┘ └────────────┘
     │           │
     ▼           ▼
┌─────────┐ ┌──────────────────┐
│ billing │ │ techsupport_db   │
│   _db   │ │ + pgvector (RAG) │
└─────────┘ └──────────────────┘
```

### How It Works

1. Customer sends a message to the **Orchestrator** via REST API
2. The Orchestrator's LLM (Claude Haiku) reads the message and decides which specialist agent to call
3. It routes via **TaskTool** using the A2A protocol over HTTP
4. The specialist agent processes the request using its tools and returns a response
5. The Orchestrator returns the final response to the customer

---

## Agents

### Orchestrator (port 8080)
The main entry point. Uses `TaskTool` with `SubagentReference` to discover and invoke sub-agents via the A2A protocol. Exposes a REST API for clients.

### Billing Agent (port 8081)
Handles billing and invoice queries. Backed by PostgreSQL with JPA. Tools:
- Get invoices by customer
- Get invoices by customer and status
- Get all invoices by status
- Get invoice status by ID
- Get outstanding balance
- Get overdue invoices

### Tech Support Agent (port 8082)
Answers technical product questions using **RAG** (Retrieval-Augmented Generation). Uses Ollama (`nomic-embed-text`) for embeddings and pgvector for similarity search. Tools:
- Search product documentation

### Escalation Agent (port 8083)
Handles complex issues requiring human attention. Creates and tracks escalation tickets in memory. Tools:
- Create escalation ticket (with priority: LOW / MEDIUM / HIGH / CRITICAL)
- Get ticket status
- Get open tickets by customer

---

## Tech Stack

| Component | Technology |
|---|---|
| Framework | Spring Boot 4.0.6 |
| AI Framework | Spring AI 2.0.0-M8 |
| A2A Protocol | spring-ai-a2a 0.2.0 |
| Agent Utils | spring-ai-agent-utils 0.7.0 |
| Chat Model | Anthropic Claude Haiku |
| Embedding Model | Ollama — nomic-embed-text |
| Vector Store | pgvector (PostgreSQL) |
| Database | PostgreSQL 17 |
| Language | Java 25 |
| Build | Maven (multi-module) |
| Infrastructure | Docker Compose |

---

## Project Structure

```
customer-support-platform/
├── pom.xml                          ← Parent POM — manages all versions
├── docker-compose.yml               ← PostgreSQL + Ollama infrastructure
├── docker/
│   └── init-db.sql                  ← Creates billing_db + techsupport_db
│
├── orchestrator/                    ← Port 8080
│   └── src/main/java/
│       ├── OrchestratorAgentConfig  ← AgentCard + AgentExecutor
│       ├── ChatClientConfig         ← TaskTool with 3 SubagentReferences
│       └── SupportController        ← POST /api/support/chat
│
├── billing-agent/                   ← Port 8081
│   └── src/main/java/
│       ├── Invoice                  ← JPA entity
│       ├── InvoiceDto               ← Record (response DTO)
│       ├── InvoiceStatus            ← Enum: PAID, UNPAID, REFUNDED
│       ├── InvoiceRepository        ← Spring Data JPA
│       ├── BillingTools             ← @Tool methods
│       ├── ChatClientConfig         ← ChatClient + system prompt
│       └── BillingAgentConfig       ← AgentCard + AgentExecutor
│
├── tech-support-agent/              ← Port 8082
│   └── src/main/java/
│       ├── TechSupportTools         ← searchProductDocs @Tool (RAG)
│       ├── DocumentService          ← TikaDocumentReader + TokenTextSplitter
│       ├── DocumentIngestionController ← POST /api/docs/upload
│       ├── ChatClientConfig         ← ChatClient + system prompt
│       └── TechSupportAgentConfig   ← AgentCard + AgentExecutor
│
└── escalation-agent/                ← Port 8083
    └── src/main/java/
        ├── EscalationTicket         ← Record
        ├── EscalationTools          ← @Tool methods (ConcurrentHashMap)
        ├── ChatClientConfig         ← ChatClient + system prompt
        └── EscalationAgentConfig    ← AgentCard + AgentExecutor
```

---

## Getting Started

### Prerequisites
- Java 25
- Maven 3.8+
- Docker Desktop
- Anthropic API Key

### 1. Start Infrastructure

```bash
docker-compose up -d
```

This starts PostgreSQL (with pgvector) and Ollama. Two databases are created automatically: `billing_db` and `techsupport_db`.

### 2. Pull the Embedding Model

```bash
docker exec customer-support-ollama ollama pull nomic-embed-text
```

### 3. Set Environment Variable

```bash
export ANTHROPIC_API_KEY=your-api-key-here
```

### 4. Run the Agents (in order)

Start each module from IntelliJ or via Maven:

```bash
# Terminal 1
cd billing-agent && mvn spring-boot:run

# Terminal 2
cd tech-support-agent && mvn spring-boot:run

# Terminal 3
cd escalation-agent && mvn spring-boot:run

# Terminal 4 — start last
cd orchestrator && mvn spring-boot:run
```

---

## API Endpoints

### Chat with the Orchestrator

```http
POST http://localhost:8080/a2a/api/support/chat
Content-Type: application/json

{
  "customerId": "C001",
  "message": "Why is my invoice still showing as unpaid?"
}
```

### Upload Product Documentation (Tech Support RAG)

```http
POST http://localhost:8082/a2a/api/docs/upload
Content-Type: multipart/form-data

file: <your-document.pdf or .txt>
```

### Agent Cards (A2A Discovery)

Each agent exposes its capabilities at:

| Agent | Agent Card URL |
|---|---|
| Orchestrator | `GET http://localhost:8080/a2a/.well-known/agent.json` |
| Billing Agent | `GET http://localhost:8081/a2a/.well-known/agent.json` |
| Tech Support Agent | `GET http://localhost:8082/a2a/.well-known/agent.json` |
| Escalation Agent | `GET http://localhost:8083/a2a/.well-known/agent.json` |

---

## Example Conversations

**Billing query** — routed to billing-agent:
> "What is my outstanding balance for customer ID C001?"

**Technical query** — routed to tech-support-agent:
> "How do I import contacts from a CSV file?"

**Escalation** — routed to escalation-agent:
> "I've been charged twice and nobody is helping me. I need this escalated."

---

## Key Concepts Demonstrated

- **A2A Protocol** — Agents discover and communicate with each other using standardised JSON-RPC over HTTP
- **AgentCard** — Each agent publishes its capabilities at `/.well-known/agent.json`
- **TaskTool** — The orchestrator uses `TaskTool` with `SubagentReference` to invoke sub-agents as LLM tools
- **RAG** — Tech support answers are grounded in product documentation using vector similarity search
- **ChatClientCustomizer** — System prompts and tools are configured per-agent without coupling to the `ChatClient` bean
- **Multi-module Maven** — All agents share version management through a parent POM
