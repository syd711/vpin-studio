package de.mephisto.vpin.server.mcp;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

  public Boolean getBooleanOrNull(String name) {
    return values.get(name) != null ? getBoolean(name, false) : null;
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

  public Object get(String name) {
    return values.get(name);
  }

  public boolean has(String name) {
    return values.get(name) != null;
  }

  /**
   * @return a JSON object argument, null if missing
   */
  @SuppressWarnings("unchecked")
  public Map<String, Object> getMap(String name) {
    Object value = values.get(name);
    if (value == null) {
      return null;
    }
    if (value instanceof Map<?, ?> map) {
      return (Map<String, Object>) map;
    }
    throw new McpToolException("Argument \"" + name + "\" must be a JSON object.");
  }

  /**
   * @return a list of numbers, given as JSON array or comma separated string; empty if missing
   */
  public List<Integer> getIntList(String name) {
    List<Integer> result = new ArrayList<>();
    for (String value : getStringList(name)) {
      try {
        result.add(Integer.parseInt(value));
      }
      catch (NumberFormatException e) {
        throw new McpToolException("Argument \"" + name + "\" must only contain numbers, got \"" + value + "\".");
      }
    }
    return result;
  }

  /**
   * @return a list of strings, given as JSON array or comma separated string; empty if missing
   */
  public List<String> getStringList(String name) {
    Object value = values.get(name);
    List<String> result = new ArrayList<>();
    if (value instanceof Collection<?> collection) {
      for (Object element : collection) {
        if (element != null && !String.valueOf(element).isBlank()) {
          result.add(element instanceof Number n && n.doubleValue() == n.intValue() ? String.valueOf(n.intValue()) : String.valueOf(element).trim());
        }
      }
    }
    else if (value != null) {
      for (String element : String.valueOf(value).split(",")) {
        if (!element.isBlank()) {
          result.add(element.trim());
        }
      }
    }
    return result;
  }
}
