package de.mephisto.vpin.server.mcp;

import java.util.List;

/**
 * A Spring bean offering tools for the MCP endpoint. All providers are collected by {@link McpResource}.
 */
public interface McpToolProvider {

  List<McpTool> getTools();
}
