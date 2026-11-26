package de.mephisto.vpin.server.mcp;

import java.util.Collections;
import java.util.Map;

/**
 * Typed access to the arguments of an MCP tool call.
 */
public class McpArguments {
  private final Map<String, Object> values;

  public McpArguments(Map<String, Object> values) {
    this.values = values != null ? values : Collections.emptyMap();
  }

  public String getString(String name) {
    Object value = values.get(name);
    return value != null ? String.valueOf(value) : null;
  }

  public Integer getInt(String name) {
    Object value = values.get(name);
    if (value instanceof Number number) {
      return number.intValue();
    }
    if (value instanceof String s && !s.isBlank()) {
      try {
        return Integer.parseInt(s.trim());
      }
      catch (NumberFormatException e) {
        throw new McpToolException("Argument \"" + name + "\" must be a number.");
      }
    }
    return null;
  }

  public int getInt(String name, int defaultValue) {
    Integer value = getInt(name);
    return value != null ? value : defaultValue;
  }

  public int requireInt(String name) {
    Integer value = getInt(name);
    if (value == null) {
      throw new McpToolException("Missing argument \"" + name + "\".");
    }
    return value;
  }

  public boolean requireBoolean(String name) {
    if (values.get(name) == null) {
      throw new McpToolException("Missing argument \"" + name + "\".");
    }
    return getBoolean(name, false);
  }

  public boolean getBoolean(String name, boolean defaultValue) {
    Object value = values.get(name);
    if (value instanceof Boolean b) {
      return b;
    }
    if (value instanceof String s) {
      return Boolean.parseBoolean(s);
    }
    return defaultValue;
  }
}
