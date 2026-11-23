package de.mephisto.vpin.server.achievements.migration;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * Reads a local VPX Achievement Watcher installation: {@code Achievements/achievements_state.json} (with the
 * {@code .bak} fallback), {@code cloud_identity.json} (Player ID + recovery key) and the generated rule files.
 * <p>
 * The state file's signature is verified like the Watcher does. The salts are public, so a valid signature only
 * detects casual edits and is not a proof of ownership.
 */
public class LocalWatcherImporter {
  private static final Logger LOG = LoggerFactory.getLogger(LocalWatcherImporter.class);

  private static final String LEGACY_SALT = "VPX_S3cr3t_H4sh_9921!";
  private static final String BASE_SALT = "VpX_W@tcher_2024!";

  private final ObjectMapper mapper = JsonMapper.builder().build();

  @Nullable
  public WatcherData read(@NonNull File base) {
    File achievements = new File(base, "Achievements");
    File stateFile = new File(achievements, "achievements_state.json");
    if (!stateFile.exists()) {
      // installations before the folder migration kept the state in the base folder
      stateFile = new File(base, "achievements_state.json");
    }
    JsonNode state = readJson(stateFile);
    if (state == null) {
      state = readJson(new File(stateFile.getAbsolutePath() + ".bak"));
    }
    if (state == null || !state.isObject()) {
      return null;
    }

    WatcherData data = new WatcherData();
    data.signatureValid = verifySignature((ObjectNode) state.deepCopy());
    data.parse(state);

    JsonNode identity = readJson(new File(base, "cloud_identity.json"));
    if (identity != null) {
      data.pid = identity.hasNonNull("pid") ? identity.get("pid").asString() : null;
      data.recoveryKey = identity.hasNonNull("key") ? identity.get("key").asString() : null;
    }
    if (data.playerName == null) {
      JsonNode config = readJson(new File(base, "config.json"));
      if (config == null && base.getParentFile() != null) {
        config = readJson(new File(base.getParentFile(), "config.json"));
      }
      if (config != null) {
        JsonNode overlay = config.get("OVERLAY");
        if (overlay != null) {
          data.playerName = overlay.hasNonNull("player_name") ? overlay.get("player_name").asString() : null;
          if (data.pid == null && overlay.hasNonNull("player_id")) {
            data.pid = overlay.get("player_id").asString().toLowerCase();
          }
        }
      }
    }

    File ruleFolder = new File(achievements, "rom_specific_achievements");
    File[] ruleFiles = ruleFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".ach.json"));
    if (ruleFiles != null) {
      data.ruleFiles.addAll(List.of(ruleFiles));
    }
    return data;
  }

  @Nullable
  private JsonNode readJson(File file) {
    if (!file.exists()) {
      return null;
    }
    try {
      return mapper.readTree(file);
    }
    catch (Exception e) {
      LOG.warn("Failed to read {}: {}", file.getAbsolutePath(), e.getMessage());
      return null;
    }
  }

  /**
   * The Watcher's {@code _generate_signature} / {@code _generate_legacy_signature}.
   */
  static boolean verifySignature(@NonNull ObjectNode data) {
    JsonNode sigNode = data.remove("_signature");
    if (sigNode == null || !sigNode.isString()) {
      return false;
    }
    String sig = sigNode.asString();
    String canonical = pythonJson(data);
    String score = data.has("score") ? pythonStr(data.get("score")) : "0";
    String duration = data.has("duration") ? pythonStr(data.get("duration")) : "0";
    String session = data.has("session_id") ? pythonStr(data.get("session_id")) : "none";
    String dynamicSalt = score + "_" + duration + "_" + session + "_" + BASE_SALT;
    return sig.equals(sha256(canonical + dynamicSalt)) || sig.equals(sha256(canonical + LEGACY_SALT));
  }

  private static String pythonStr(JsonNode v) {
    if (v.isString()) {
      return v.asString();
    }
    if (v.isNull()) {
      return "None";
    }
    if (v.isBoolean()) {
      return v.asBoolean() ? "True" : "False";
    }
    return pythonJson(v);
  }

  /**
   * {@code json.dumps(obj, sort_keys=True, separators=(',', ':'))} with Python's default {@code ensure_ascii=True}.
   */
  static String pythonJson(JsonNode node) {
    StringBuilder sb = new StringBuilder();
    write(sb, node);
    return sb.toString();
  }

  private static void write(StringBuilder sb, JsonNode node) {
    if (node == null || node.isNull()) {
      sb.append("null");
    }
    else if (node.isObject()) {
      List<String> keys = new ArrayList<>();
      for (Map.Entry<String, JsonNode> e : node.properties()) {
        keys.add(e.getKey());
      }
      keys.sort(null);
      sb.append('{');
      for (int i = 0; i < keys.size(); i++) {
        if (i > 0) sb.append(',');
        writeString(sb, keys.get(i));
        sb.append(':');
        write(sb, node.get(keys.get(i)));
      }
      sb.append('}');
    }
    else if (node.isArray()) {
      sb.append('[');
      for (int i = 0; i < node.size(); i++) {
        if (i > 0) sb.append(',');
        write(sb, node.get(i));
      }
      sb.append(']');
    }
    else if (node.isString()) {
      writeString(sb, node.asString());
    }
    else if (node.isBoolean()) {
      sb.append(node.asBoolean() ? "true" : "false");
    }
    else if (node.isIntegralNumber()) {
      sb.append(node.bigIntegerValue().toString());
    }
    else if (node.isNumber()) {
      sb.append(pythonFloat(node.asDouble()));
    }
    else {
      sb.append(node.toString());
    }
  }

  private static String pythonFloat(double d) {
    if (d == Math.rint(d) && Math.abs(d) < 1e16) {
      return String.format(java.util.Locale.ROOT, "%.1f", d);
    }
    String s = Double.toString(d);
    // Java "1.0E-5" -> Python "1e-05"
    if (s.contains("E")) {
      String[] parts = s.split("E");
      String mantissa = parts[0].endsWith(".0") ? parts[0].substring(0, parts[0].length() - 2) : parts[0];
      int exp = Integer.parseInt(parts[1]);
      return mantissa + "e" + (exp < 0 ? "-" : "+") + String.format("%02d", Math.abs(exp));
    }
    return s;
  }

  private static void writeString(StringBuilder sb, String s) {
    sb.append('"');
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      switch (c) {
        case '"' -> sb.append("\\\"");
        case '\\' -> sb.append("\\\\");
        case '\n' -> sb.append("\\n");
        case '\r' -> sb.append("\\r");
        case '\t' -> sb.append("\\t");
        case '\b' -> sb.append("\\b");
        case '\f' -> sb.append("\\f");
        default -> {
          if (c < 0x20 || c > 0x7e) {
            sb.append(String.format("\\u%04x", (int) c));
          }
          else {
            sb.append(c);
          }
        }
      }
    }
    sb.append('"');
  }

  private static String sha256(String s) {
    try {
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
    }
    catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
