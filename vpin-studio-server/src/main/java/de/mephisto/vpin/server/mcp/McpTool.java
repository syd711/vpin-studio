package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.restclient.mcp.McpToolGroup;

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
 * @param group       the permission group of a changing tool, null for reading tools
 * @param handler     executes the tool with the given arguments and returns a JSON serializable result
 */
public record McpTool(String name,
                      String title,
                      String description,
                      Map<String, Object> inputSchema,
                      boolean readOnly,
                      boolean destructive,
                      McpToolGroup group,
                      Function<McpArguments, Object> handler) {

  public static McpTool read(String name, String title, String description, Map<String, Object> inputSchema, Function<McpArguments, Object> handler) {
    return new McpTool(name, title, description, inputSchema, true, false, null, handler);
  }

  public static McpTool write(McpToolGroup group, String name, String title, String description, Map<String, Object> inputSchema, Function<McpArguments, Object> handler) {
    return new McpTool(name, title, description, inputSchema, false, false, group, handler);
  }

  public static McpTool delete(McpToolGroup group, String name, String title, String description, Map<String, Object> inputSchema, Function<McpArguments, Object> handler) {
    return new McpTool(name, title, description, inputSchema, false, true, group, handler);
  }
}
