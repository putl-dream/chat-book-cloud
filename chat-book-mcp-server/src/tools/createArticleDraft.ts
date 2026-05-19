import type { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { z } from "zod";
import type { AppConfig } from "../config.js";
import type { ChatBookClient } from "../clients/chatBookClient.js";
import { scanSensitiveFields } from "../security/sensitiveScanner.js";

const createArticleDraftSchema = z.object({
  title: z.string().trim().min(1).max(255).describe("Draft title."),
  summary: z.string().trim().max(5000).optional().describe("Optional short summary."),
  content: z.string().trim().min(1).describe("Draft body, preferably Markdown."),
  instruction: z.string().trim().max(1000).optional().describe("Optional source note or instruction.")
});

export function registerCreateArticleDraftTool(
  server: McpServer,
  client: ChatBookClient,
  config: AppConfig
): void {
  server.registerTool(
    "create_article_draft",
    {
      title: "Create Article Draft",
      description: "Create a Chat Book platform article draft from Codex-provided Markdown content.",
      inputSchema: createArticleDraftSchema.shape
    },
    async (args) => {
      const input = createArticleDraftSchema.parse(args);
      if (input.content.length > config.maxContentLength) {
        throw new Error(`content exceeds ${config.maxContentLength} characters`);
      }

      const findings = scanSensitiveFields({
        title: input.title,
        summary: input.summary,
        content: input.content,
        instruction: input.instruction
      });
      if (findings.length > 0) {
        const details = findings.map((finding) => `${finding.field}: ${finding.type}`).join(", ");
        throw new Error(`Sensitive-looking content was detected and the draft was not created: ${details}`);
      }

      const result = await client.createArticleDraft(input);
      return {
        content: [
          {
            type: "text",
            text: JSON.stringify(result, null, 2)
          }
        ]
      };
    }
  );
}
