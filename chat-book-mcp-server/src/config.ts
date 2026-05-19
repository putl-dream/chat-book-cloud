export type AppConfig = {
  apiBaseUrl: string;
  mcpToken: string;
  maxContentLength: number;
  transport: "stdio" | "http";
  httpHost: string;
  httpPort: number;
  httpAuthToken?: string;
};

export function loadConfig(env: NodeJS.ProcessEnv = process.env): AppConfig {
  const apiBaseUrl = normalizeBaseUrl(env.CHAT_BOOK_API_BASE_URL || "http://localhost:8080/api");
  const mcpToken = env.CHAT_BOOK_MCP_TOKEN;
  if (!mcpToken || !mcpToken.trim()) {
    throw new Error("CHAT_BOOK_MCP_TOKEN is required");
  }

  const transport = parseTransport(env.CHAT_BOOK_MCP_TRANSPORT);
  const httpAuthToken = env.CHAT_BOOK_MCP_SERVER_AUTH_TOKEN?.trim();
  if (transport === "http" && !httpAuthToken) {
    throw new Error("CHAT_BOOK_MCP_SERVER_AUTH_TOKEN is required when CHAT_BOOK_MCP_TRANSPORT=http");
  }

  return {
    apiBaseUrl,
    mcpToken: mcpToken.trim(),
    maxContentLength: parsePositiveInt(env.CHAT_BOOK_MCP_MAX_CONTENT_LENGTH, 100_000),
    transport,
    httpHost: env.CHAT_BOOK_MCP_HTTP_HOST?.trim() || "0.0.0.0",
    httpPort: parsePositiveInt(env.CHAT_BOOK_MCP_HTTP_PORT, 3001),
    httpAuthToken
  };
}

function normalizeBaseUrl(value: string): string {
  return value.replace(/\/+$/, "");
}

function parsePositiveInt(value: string | undefined, fallback: number): number {
  if (!value) {
    return fallback;
  }
  const parsed = Number.parseInt(value, 10);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
}

function parseTransport(value: string | undefined): "stdio" | "http" {
  if (!value || !value.trim()) {
    return "stdio";
  }
  const normalized = value.trim().toLowerCase();
  if (normalized === "stdio" || normalized === "http") {
    return normalized;
  }
  throw new Error("CHAT_BOOK_MCP_TRANSPORT must be either stdio or http");
}
