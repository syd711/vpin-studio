package de.mephisto.vpin.server.mcp;

/**
 * A tool failure that is reported back to the AI as a tool error (isError=true) so it can correct its call.
 */
public class McpToolException extends RuntimeException {
  public McpToolException(String message) {
    super(message);
  }
}
