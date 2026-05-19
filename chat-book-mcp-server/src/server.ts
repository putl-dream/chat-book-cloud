import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { StreamableHTTPServerTransport } from "@modelcontextprotocol/sdk/server/streamableHttp.js";
import http, { type IncomingMessage, type ServerResponse } from "node:http";
import { ChatBookClient } from "./clients/chatBookClient.js";
import { type AppConfig, loadConfig } from "./config.js";
import { registerCreateArticleDraftTool } from "./tools/createArticleDraft.js";

function createMcpServer(config: AppConfig): McpServer {
  const client = new ChatBookClient(config);
  const server = new McpServer({
    name: "chat-book-mcp-server",
    version: "0.1.0"
  });

  registerCreateArticleDraftTool(server, client, config);
  return server;
}

async function runStdio(config: AppConfig): Promise<void> {
  const server = createMcpServer(config);
  const transport = new StdioServerTransport();
  await server.connect(transport);
}

async function runHttp(config: AppConfig): Promise<void> {
  const httpServer = http.createServer((request, response) => {
    void handleHttpRequest(config, request, response);
  });

  httpServer.on("clientError", (_error, socket) => {
    socket.end("HTTP/1.1 400 Bad Request\r\n\r\n");
  });

  await new Promise<void>((resolve) => {
    httpServer.listen(config.httpPort, config.httpHost, resolve);
  });

  console.log(`chat-book-mcp-server listening on ${config.httpHost}:${config.httpPort}`);
}

async function handleHttpRequest(
  config: AppConfig,
  request: IncomingMessage,
  response: ServerResponse
): Promise<void> {
  try {
    const url = new URL(request.url || "/", "http://localhost");
    setCommonHeaders(response);

    if (request.method === "OPTIONS") {
      response.writeHead(204);
      response.end();
      return;
    }

    if (url.pathname === "/health") {
      writeJson(response, 200, {
        status: "UP",
        service: "chat-book-mcp-server",
        transport: "http"
      });
      return;
    }

    if (url.pathname !== "/mcp") {
      writeJson(response, 404, { error: "Not found" });
      return;
    }

    if (!isAuthorized(config, request)) {
      writeJson(response, 401, { error: "Unauthorized" });
      return;
    }

    const parsedBody = request.method === "POST" ? await readJsonBody(request) : undefined;
    const server = createMcpServer(config);
    const transport = new StreamableHTTPServerTransport({
      sessionIdGenerator: undefined
    });

    response.on("close", () => {
      void transport.close();
      void server.close();
    });

    await server.connect(transport);
    await transport.handleRequest(request, response, parsedBody);
  } catch (error) {
    const message = error instanceof Error ? error.message : "Internal server error";
    if (!response.headersSent) {
      writeJson(response, 500, { error: message });
    } else {
      response.end();
    }
  }
}

function isAuthorized(config: AppConfig, request: IncomingMessage): boolean {
  return request.headers.authorization === `Bearer ${config.httpAuthToken}`;
}

function setCommonHeaders(response: ServerResponse): void {
  response.setHeader("Access-Control-Allow-Origin", "*");
  response.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Mcp-Session-Id");
  response.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
}

function writeJson(response: ServerResponse, statusCode: number, body: unknown): void {
  response.writeHead(statusCode, { "Content-Type": "application/json" });
  response.end(JSON.stringify(body));
}

async function readJsonBody(request: IncomingMessage): Promise<unknown> {
  const chunks: Buffer[] = [];
  for await (const chunk of request) {
    chunks.push(Buffer.isBuffer(chunk) ? chunk : Buffer.from(chunk));
  }
  const rawBody = Buffer.concat(chunks).toString("utf8").trim();
  if (!rawBody) {
    return undefined;
  }
  return JSON.parse(rawBody);
}

async function main(): Promise<void> {
  const config = loadConfig();
  if (config.transport === "http") {
    await runHttp(config);
    return;
  }
  await runStdio(config);
}

main().catch((error) => {
  console.error(error instanceof Error ? error.message : error);
  process.exit(1);
});
