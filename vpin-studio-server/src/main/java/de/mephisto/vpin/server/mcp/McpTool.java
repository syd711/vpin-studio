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
 * @param readOnly    true if the tool does not change anything on the cabinet
 * @param handler     executes the tool with the given arguments and returns a JSON serializable result
 */
public record McpTool(String name,
                      String title,
                      String description,
                      Map<String, Object> inputSchema,
                      boolean readOnly,
                      Function<McpArguments, Object> handler) {
}
