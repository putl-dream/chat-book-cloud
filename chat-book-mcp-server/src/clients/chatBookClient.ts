import type { AppConfig } from "../config.js";
import type { CommonResult, CreateArticleDraftInput, CreateArticleDraftResult } from "../types.js";

export class ChatBookClient {
  private readonly apiBaseUrl: string;
  private readonly mcpToken: string;

  constructor(config: AppConfig) {
    this.apiBaseUrl = config.apiBaseUrl;
    this.mcpToken = config.mcpToken;
  }

  async createArticleDraft(input: CreateArticleDraftInput): Promise<CreateArticleDraftResult> {
    const response = await fetch(`${this.apiBaseUrl}/mcp/drafts`, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${this.mcpToken}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify(input)
    });

    const text = await response.text();
    const result = parseCommonResult<CreateArticleDraftResult>(text);
    if (!response.ok) {
      throw new Error(result?.msg || `Chat Book API request failed with HTTP ${response.status}`);
    }
    if (!result) {
      throw new Error("Chat Book API returned an empty response");
    }
    if (result.code !== 200 || !result.data) {
      throw new Error(result.msg || `Chat Book API returned code ${result.code}`);
    }
    return result.data;
  }
}

function parseCommonResult<T>(text: string): CommonResult<T> | null {
  if (!text.trim()) {
    return null;
  }
  try {
    return JSON.parse(text) as CommonResult<T>;
  } catch {
    throw new Error("Chat Book API returned invalid JSON");
  }
}
