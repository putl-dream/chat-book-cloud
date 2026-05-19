export type AppConfig = {
  apiBaseUrl: string;
  mcpToken: string;
  maxContentLength: number;
};

export function loadConfig(env: NodeJS.ProcessEnv = process.env): AppConfig {
  const apiBaseUrl = normalizeBaseUrl(env.CHAT_BOOK_API_BASE_URL || "http://localhost:8080/api");
  const mcpToken = env.CHAT_BOOK_MCP_TOKEN;
  if (!mcpToken || !mcpToken.trim()) {
    throw new Error("CHAT_BOOK_MCP_TOKEN is required");
  }

  return {
    apiBaseUrl,
    mcpToken: mcpToken.trim(),
    maxContentLength: parsePositiveInt(env.CHAT_BOOK_MCP_MAX_CONTENT_LENGTH, 100_000)
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
