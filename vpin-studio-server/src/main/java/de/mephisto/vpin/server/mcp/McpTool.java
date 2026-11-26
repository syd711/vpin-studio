package de.mephisto.vpin.server.mcp;

import java.util.Map;
import java.util.function.Function;

/**
 * A tool offered through the MCP endpoint.
 *
 * @param name        the unique tool name, e.g. "search_tables"
 * @param title       a human readable title
 * @param description what the tool does, read by the AI to decide when to use it
 * @param inputSchema JSON schema of the arguments
 * @param readOnly    true if the tool does not change anything on the cabinet; the other tools are only offered when
 *                    the read-only mode is switched off in the MCP settings
 * @param destructive true if the tool deletes data that cannot be restored
 * @param handler     executes the tool with the given arguments and returns a JSON serializable result
 */
public record McpTool(String name,
                      String title,
                      String description,
                      Map<String, Object> inputSchema,
                      boolean readOnly,
                      boolean destructive,
                      Function<McpArguments, Object> handler) {

  public static McpTool read(String name, String title, String description, Map<String, Object> inputSchema, Function<McpArguments, Object> handler) {
    return new McpTool(name, title, description, inputSchema, true, false, handler);
  }

  public static McpTool write(String name, String title, String description, Map<String, Object> inputSchema, Function<McpArguments, Object> handler) {
    return new McpTool(name, title, description, inputSchema, false, false, handler);
  }

  public static McpTool delete(String name, String title, String description, Map<String, Object> inputSchema, Function<McpArguments, Object> handler) {
    return new McpTool(name, title, description, inputSchema, false, true, handler);
  }
}
