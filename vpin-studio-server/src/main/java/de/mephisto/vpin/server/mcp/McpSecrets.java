package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.Secret;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Finds and masks the secret values of settings before they are handed to an AI assistant.
 * <p>
 * A value is secret if its field is annotated with {@link Secret}, or - as a safety net for fields nobody annotated -
 * if the name of a text field or JSON property looks like a credential (password, token, API key, secret).
 * Nested objects, lists and maps are searched as well.
 */
final class McpSecrets {

  static final String MASK = "********";

  private static final Pattern SECRET_NAME = Pattern.compile("(?i).*(password|passwd|secret|token|apikey|api_key|credential).*");
  private static final int MAX_DEPTH = 12;

  private McpSecrets() {
  }

  /**
   * @return the settings as JSON with every secret value that is set replaced by {@link #MASK}
   */
  static JsonNode mask(Object settings) {
    JsonNode tree = JsonSettings.objectMapper.valueToTree(settings);
    for (List<Object> path : getSecretPaths(settings)) {
      maskAt(tree, path);
    }
    maskByName(tree);
    return tree;
  }

  /**
   * @return display path -> JSON value of every secret of the settings, used to detect changes of secrets
   */
  static Map<String, JsonNode> getSecrets(Object settings) {
    JsonNode tree = JsonSettings.objectMapper.valueToTree(settings);
    Map<String, JsonNode> result = new TreeMap<>();
    for (List<Object> path : getSecretPaths(settings)) {
      JsonNode node = navigate(tree, path);
      result.put(toDisplayPath(path), node != null ? node : NullNode.getInstance());
    }
    collectByName(tree, new ArrayList<>(), result);
    return result;
  }

  /**
   * @return the display paths of the secrets that differ between the two settings
   */
  static List<String> getChangedSecrets(Object before, Object after) {
    Map<String, JsonNode> old = getSecrets(before);
    Map<String, JsonNode> updated = getSecrets(after);
    Set<String> paths = new TreeSet<>(old.keySet());
    paths.addAll(updated.keySet());
    List<String> changed = new ArrayList<>();
    for (String path : paths) {
      if (!Objects.equals(normalize(old.get(path)), normalize(updated.get(path)))) {
        changed.add(path);
      }
    }
    return changed;
  }

  static boolean isSecret(Field field) {
    if (field.isAnnotationPresent(Secret.class)) {
      return true;
    }
    Class<?> type = field.getType();
    if (type.isPrimitive() || type == Boolean.class || Number.class.isAssignableFrom(type) || type.isEnum()) {
      return false; //e.g. "passwordSet" or "apiKey1Enabled" are flags, not secrets
    }
    return SECRET_NAME.matcher(field.getName()).matches();
  }

  //-------------------------------- reflection -------------------------------------------------------

  static List<List<Object>> getSecretPaths(Object value) {
    List<List<Object>> result = new ArrayList<>();
    collectPaths(value, new ArrayList<>(), result, 0);
    return result;
  }

  private static void collectPaths(Object value, List<Object> path, List<List<Object>> result, int depth) {
    if (value == null || depth > MAX_DEPTH) {
      return;
    }
    if (value instanceof Collection<?> collection) {
      int index = 0;
      for (Object element : collection) {
        collectPaths(element, append(path, index++), result, depth + 1);
      }
      return;
    }
    if (value instanceof Map<?, ?> map) {
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        collectPaths(entry.getValue(), append(path, String.valueOf(entry.getKey())), result, depth + 1);
      }
      return;
    }
    Class<?> type = value.getClass();
    if (type.isEnum() || !type.getName().startsWith("de.mephisto.vpin.")) {
      return;
    }
    for (Field field : getFields(type)) {
      List<Object> fieldPath = append(path, field.getName());
      if (isSecret(field)) {
        result.add(fieldPath);
      }
      else {
        collectPaths(read(field, value), fieldPath, result, depth + 1);
      }
    }
  }

  private static List<Field> getFields(Class<?> type) {
    List<Field> fields = new ArrayList<>();
    for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
      for (Field field : c.getDeclaredFields()) {
        if (!Modifier.isStatic(field.getModifiers()) && !field.isSynthetic()) {
          fields.add(field);
        }
      }
    }
    return fields;
  }

  private static Object read(Field field, Object target) {
    try {
      field.setAccessible(true);
      return field.get(target);
    }
    catch (Exception e) {
      return null;
    }
  }

  //-------------------------------- JSON -------------------------------------------------------------

  private static void maskAt(JsonNode tree, List<Object> path) {
    JsonNode parent = navigate(tree, path.subList(0, path.size() - 1));
    Object last = path.get(path.size() - 1);
    if (parent instanceof ObjectNode object && last instanceof String name) {
      if (isSet(object.get(name))) {
        object.put(name, MASK);
      }
    }
    else if (parent instanceof ArrayNode array && last instanceof Integer index) {
      if (index < array.size() && isSet(array.get(index))) {
        array.set(index, MASK);
      }
    }
  }

  private static void maskByName(JsonNode node) {
    if (node instanceof ObjectNode object) {
      for (String name : new ArrayList<>(object.propertyNames())) {
        JsonNode child = object.get(name);
        if (SECRET_NAME.matcher(name).matches() && child != null && child.isString() && isSet(child)) {
          object.put(name, MASK);
        }
        else {
          maskByName(child);
        }
      }
    }
    else if (node instanceof ArrayNode array) {
      for (JsonNode child : array) {
        maskByName(child);
      }
    }
  }

  private static void collectByName(JsonNode node, List<Object> path, Map<String, JsonNode> result) {
    if (node instanceof ObjectNode object) {
      for (String name : object.propertyNames()) {
        JsonNode child = object.get(name);
        List<Object> childPath = append(path, name);
        if (SECRET_NAME.matcher(name).matches() && child != null && (child.isString() || child.isNull())) {
          result.putIfAbsent(toDisplayPath(childPath), child);
        }
        else {
          collectByName(child, childPath, result);
        }
      }
    }
    else if (node instanceof ArrayNode array) {
      for (int i = 0; i < array.size(); i++) {
        collectByName(array.get(i), append(path, i), result);
      }
    }
  }

  static JsonNode navigate(JsonNode tree, List<Object> path) {
    JsonNode node = tree;
    for (Object segment : path) {
      if (node == null) {
        return null;
      }
      if (segment instanceof Integer index) {
        node = node.isArray() && index < node.size() ? node.get(index) : null;
      }
      else {
        node = node.isObject() ? node.get((String) segment) : null;
      }
    }
    return node;
  }

  private static boolean isSet(JsonNode node) {
    if (node == null || node.isNull() || node.isMissingNode()) {
      return false;
    }
    if (node.isString()) {
      return !node.stringValue().isBlank();
    }
    if (node.isContainer()) {
      return !node.isEmpty();
    }
    return true;
  }

  private static JsonNode normalize(JsonNode node) {
    return isSet(node) ? node : null;
  }

  static String toDisplayPath(List<Object> path) {
    StringBuilder b = new StringBuilder();
    for (Object segment : path) {
      if (segment instanceof Integer index) {
        b.append('[').append(index).append(']');
      }
      else {
        if (!b.isEmpty()) {
          b.append('.');
        }
        b.append(segment);
      }
    }
    return b.toString();
  }

  private static List<Object> append(List<Object> path, Object segment) {
    List<Object> result = new ArrayList<>(path);
    result.add(segment);
    return result;
  }
}
