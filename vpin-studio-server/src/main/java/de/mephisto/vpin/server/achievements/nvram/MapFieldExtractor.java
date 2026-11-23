package de.mephisto.vpin.server.achievements.nvram;

import tools.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Port of the Achievement Watcher's {@code Watcher.parse_map}: extracts the fields of a tomlogic NVRAM map in
 * exactly the order and with exactly the labels the Watcher uses, because the labels become part of the
 * achievement titles and the titles are the identity of an achievement (migration compatibility).
 * <p>
 * The legacy {@code "fields"} map format of the Watcher is not supported, only the tomlogic format.
 */
public class MapFieldExtractor {

  private static final Set<String> SKIPPED = Set.of("current_player", "player_count");
  private static final Set<String> NESTED_SUFFIX_KEYS = Set.of("score", "initials", "timestamp");

  private MapFieldExtractor() {
  }

  @NonNull
  public static List<MapField> extract(@NonNull JsonNode map) {
    List<MapField> fields = new ArrayList<>();

    JsonNode gs = map.get("game_state");
    if (gs != null && gs.isObject()) {
      JsonNode scores = gs.get("scores");
      if (scores != null && scores.isArray() && !scores.isEmpty()) {
        JsonNode sc = scores.get(0);
        if (sc != null && sc.isObject() && sc.has("start")) {
          fields.add(new MapField("P1 Score", "", sc));
        }
      }
      addGameState(fields, gs, "credits", "Credits");
      addGameState(fields, gs, "current_ball", "current_ball");
      if (gs.has("ball_count")) {
        addGameState(fields, gs, "ball_count", "Balls Played");
      }
      addGameState(fields, gs, "tilted", "Tilted");
      addGameState(fields, gs, "game_over", "Game Over");
      addGameState(fields, gs, "extra_balls", "Extra Balls");
      addGameState(fields, gs, "tilt_warnings", "Tilt Warnings");
    }

    extractNested(fields, map.get("audits"), "", "");
    extractNested(fields, map.get("adjustments"), "", "adjustments");
    extractNested(fields, map.get("high_scores"), "", "high_scores");
    extractNested(fields, map.get("mode_champions"), "", "mode_champions");
    return fields;
  }

  private static void addGameState(List<MapField> fields, JsonNode gs, String nameIn, String labelOut) {
    JsonNode ent = gs.get(nameIn);
    if (ent == null || !ent.isObject() || !ent.has("start")) {
      return;
    }
    String lab = labelOut != null ? labelOut : firstNonEmpty(text(ent, "label"), nameIn);
    if (SKIPPED.contains(lab.trim().toLowerCase())) {
      return;
    }
    fields.add(new MapField(lab, "", ent));
  }

  private static void extractNested(List<MapField> fields, JsonNode node, String parentLabel, String topKey) {
    if (node == null) {
      return;
    }
    if (node.isObject()) {
      if (node.has("start") || node.has("offset")) {
        String label = firstNonEmpty(text(node, "label"), text(node, "name"), parentLabel);
        if (!StringUtils.isEmpty(label) && !SKIPPED.contains(label.toLowerCase())) {
          boolean exists = fields.stream().anyMatch(f -> f.label().equals(label));
          if (!exists) {
            fields.add(new MapField(label, topKey, node));
          }
        }
      }
      else {
        String currentLabel = firstNonEmpty(text(node, "label"), text(node, "name"), parentLabel);
        Iterator<Map.Entry<String, JsonNode>> it = node.properties().iterator();
        while (it.hasNext()) {
          Map.Entry<String, JsonNode> e = it.next();
          JsonNode v = e.getValue();
          if (v.isObject() || v.isArray()) {
            String childLabel = currentLabel;
            if (!StringUtils.isEmpty(currentLabel) && NESTED_SUFFIX_KEYS.contains(e.getKey())) {
              childLabel = currentLabel + " " + pythonTitle(e.getKey());
            }
            extractNested(fields, v, childLabel, !StringUtils.isEmpty(topKey) ? topKey : e.getKey());
          }
        }
      }
    }
    else if (node.isArray()) {
      for (JsonNode item : node) {
        extractNested(fields, item, parentLabel, topKey);
      }
    }
  }

  private static String text(JsonNode node, String key) {
    JsonNode v = node.get(key);
    if (v == null || v.isNull()) {
      return null;
    }
    if (v.isString()) {
      return v.asString();
    }
    // Python: str(value) for non string values (booleans/numbers are falsy only for 0/False)
    if (v.isBoolean()) {
      return v.asBoolean() ? "True" : null;
    }
    if (v.isNumber()) {
      return v.asDouble() == 0 ? null : v.asString();
    }
    return v.toString();
  }

  private static String firstNonEmpty(String... values) {
    for (String value : values) {
      if (!StringUtils.isEmpty(value)) {
        return value;
      }
    }
    return "";
  }

  /**
   * Python's str.title() for single lower case words.
   */
  private static String pythonTitle(String s) {
    return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1).toLowerCase();
  }
}
