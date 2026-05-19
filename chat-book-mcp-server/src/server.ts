import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import { ChatBookClient } from "./clients/chatBookClient.js";
import { loadConfig } from "./config.js";
import { registerCreateArticleDraftTool } from "./tools/createArticleDraft.js";

async function main(): Promise<void> {
  const config = loadConfig();
  const client = new ChatBookClient(config);
  const server = new McpServer({
    name: "chat-book-mcp-server",
    version: "0.1.0"
  });

  registerCreateArticleDraftTool(server, client, config);

  const transport = new StdioServerTransport();
  await server.connect(transport);
}

main().catch((error) => {
  console.error(error instanceof Error ? error.message : error);
  process.exit(1);
});
