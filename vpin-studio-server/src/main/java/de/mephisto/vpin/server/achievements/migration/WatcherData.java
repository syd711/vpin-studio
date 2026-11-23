package de.mephisto.vpin.server.achievements.migration;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.io.File;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Achievement data of a VPX Achievement Watcher player, read from Firebase ({@code players/<pid>/achievements})
 * or from a local {@code achievements_state.json}. Both have the same structure.
 */
public class WatcherData {

  public record Entry(@NonNull String title, @Nullable String rom, @Nullable Instant ts, @Nullable String vpsId) {
  }

  public String playerName;
  public String pid;
  public String recoveryKey;
  public final List<Entry> entries = new ArrayList<>();
  public final Set<String> romsPlayed = new LinkedHashSet<>();
  public final Set<String> badges = new LinkedHashSet<>();
  public String selectedBadge;
  public final Map<String, Long> tally = new HashMap<>();
  public boolean cloudUploadDone;
  public boolean signatureValid;
  /** local import only: the Watcher's generated rule files */
  public final List<File> ruleFiles = new ArrayList<>();

  public int uniqueCount() {
    Set<String> titles = new LinkedHashSet<>();
    entries.forEach(e -> titles.add(e.title().strip()));
    return titles.size();
  }

  /**
   * Parses the achievements state (Firebase achievements node or local state file).
   *
   * @param global for Firebase the "global" list, locally the "global" map {"__global__": [...]}
   */
  public void parse(@NonNull JsonNode state) {
    if (state.hasNonNull("name")) {
      playerName = state.get("name").asString();
    }
    JsonNode global = state.get("global");
    if (global != null) {
      if (global.isObject() && global.has("__global__")) {
        addEntries(global.get("__global__"), null);
      }
      else if (global.isObject()) {
        // Firebase may deliver arrays as objects with numeric keys, local state may contain other buckets
        boolean numeric = true;
        for (Map.Entry<String, JsonNode> e : global.properties()) {
          if (!e.getKey().matches("\\d+")) {
            numeric = false;
            break;
          }
        }
        if (numeric) {
          addEntries(global, null);
        }
        else {
          for (Map.Entry<String, JsonNode> e : global.properties()) {
            addEntries(e.getValue(), null);
          }
        }
      }
      else {
        addEntries(global, null);
      }
    }
    JsonNode session = state.get("session");
    if (session != null && session.isObject()) {
      for (Map.Entry<String, JsonNode> e : session.properties()) {
        addEntries(e.getValue(), e.getKey().toLowerCase());
      }
    }
    for (JsonNode rom : values(state.get("roms_played"))) {
      if (rom.isString() && !rom.asString().isBlank()) {
        romsPlayed.add(rom.asString().toLowerCase());
      }
    }
    for (JsonNode badge : values(state.get("badges"))) {
      if (badge.isString()) {
        badges.add(badge.asString());
      }
    }
    if (state.hasNonNull("selected_badge")) {
      selectedBadge = state.get("selected_badge").asString();
    }
    JsonNode tallyNode = state.get("global_tally");
    if (tallyNode != null && tallyNode.isObject()) {
      for (Map.Entry<String, JsonNode> e : tallyNode.properties()) {
        JsonNode progress = e.getValue().get("progress");
        if (progress != null && progress.isNumber()) {
          tally.put(e.getKey(), progress.asLong());
        }
      }
    }
    if (state.has("cloud_upload_done")) {
      cloudUploadDone = state.get("cloud_upload_done").asBoolean(false);
    }
  }

  /**
   * Merges the per ROM session entries delivered separately by Firebase ({@code achievements/session}).
   */
  public void parseSession(@Nullable JsonNode session) {
    if (session == null || !session.isObject()) {
      return;
    }
    Set<String> known = new LinkedHashSet<>();
    entries.forEach(e -> known.add(e.title()));
    for (Map.Entry<String, JsonNode> e : session.properties()) {
      for (JsonNode item : values(e.getValue())) {
        Entry entry = toEntry(item, e.getKey().toLowerCase());
        if (entry != null && known.add(entry.title())) {
          entries.add(entry);
        }
      }
    }
  }

  private void addEntries(@Nullable JsonNode list, @Nullable String rom) {
    for (JsonNode item : values(list)) {
      Entry entry = toEntry(item, rom);
      if (entry != null) {
        entries.add(entry);
      }
    }
  }

  @Nullable
  private static Entry toEntry(JsonNode item, @Nullable String rom) {
    String title;
    String ts = null;
    String vps = null;
    if (item.isString()) {
      title = item.asString();
    }
    else if (item.isObject() && item.hasNonNull("title")) {
      title = item.get("title").asString();
      ts = item.hasNonNull("ts") ? item.get("ts").asString() : null;
      vps = item.hasNonNull("vps_id") ? item.get("vps_id").asString() : null;
    }
    else {
      return null;
    }
    if (title == null || title.isBlank()) {
      return null;
    }
    return new Entry(title.strip(), rom, parseTs(ts), vps != null && !vps.isBlank() ? vps.strip() : null);
  }

  @Nullable
  static Instant parseTs(@Nullable String ts) {
    if (ts == null || ts.isBlank()) {
      return null;
    }
    try {
      return OffsetDateTime.parse(ts.replace("Z", "+00:00")).toInstant();
    }
    catch (Exception e) {
      try {
        return java.time.LocalDateTime.parse(ts).atZone(java.time.ZoneId.systemDefault()).toInstant();
      }
      catch (Exception e2) {
        return null;
      }
    }
  }

  private static List<JsonNode> values(@Nullable JsonNode node) {
    List<JsonNode> result = new ArrayList<>();
    if (node == null || node.isNull()) {
      return result;
    }
    if (node.isArray()) {
      node.forEach(result::add);
    }
    else if (node.isObject()) {
      for (Map.Entry<String, JsonNode> e : node.properties()) {
        result.add(e.getValue());
      }
    }
    return result;
  }
}
