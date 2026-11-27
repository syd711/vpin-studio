package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.mcp.McpToolGroup;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

import java.util.*;

import static de.mephisto.vpin.server.mcp.McpTableTools.prop;
import static de.mephisto.vpin.server.mcp.McpTableTools.schema;

/**
 * MCP tools that read and change the VPin Studio preferences stored as JSON settings.
 * <p>
 * Only the settings listed in {@link #SETTINGS} are offered. The MCP settings themselves and the authentication
 * settings are never offered, so an assistant can not widen its own access. Secret values (see {@link McpSecrets}) are
 * masked and can not be changed. Saving goes through {@link PreferencesService#savePreference(JsonSettings)}, so the
 * preference change listeners apply the change exactly as if it was made in the Studio.
 */
@Service
public class McpSettingsTools implements McpToolProvider {
  private final static Logger LOG = LoggerFactory.getLogger(McpSettingsTools.class);

  record SettingsInfo(String description, String preferencePage) {
  }

  static final Map<String, SettingsInfo> SETTINGS = new LinkedHashMap<>();

  static {
    SETTINGS.put(PreferenceNames.SERVER_SETTINGS, new SettingsInfo("General server behaviour: keeping file and display names on table uploads, backups on overwrite, the frontend fields used for the VPS mapping, the startup delay, the volume and how DMDs and backglass scores are handled.", "Server Settings"));
    SETTINGS.put(PreferenceNames.HIGHSCORE_CARD_SETTINGS, new SettingsInfo("Highscore cards: the frontend screen, resolution and how long and where card notifications are shown.", "Highscore Cards"));
    SETTINGS.put(PreferenceNames.PAUSE_MENU_SETTINGS, new SettingsInfo("The in-game pause menu: key binding, shown sections (scores, tutorials, todos, achievements), tutorial screen, scaling, rotation and screenshots.", "Pause Menu"));
    SETTINGS.put(PreferenceNames.OVERLAY_SETTINGS, new SettingsInfo("The overlay: design, page URL, screen and whether it is shown on startup.", "Overlay"));
    SETTINGS.put(PreferenceNames.NOTIFICATION_SETTINGS, new SettingsInfo("On-screen notifications: which events are shown (highscores, iScored, Discord, competitions, recordings, achievements), duration, screen and rotation.", "Notifications"));
    SETTINGS.put(PreferenceNames.VALIDATION_SETTINGS, new SettingsInfo("The validation profiles of the table validators, e.g. which frontend media are expected.", "Validators"));
    SETTINGS.put(PreferenceNames.IGNORED_VALIDATION_SETTINGS, new SettingsInfo("The validation issues that are ignored for all tables, as map of validation code -> true. Prefer the ignore_validation tool.", "Validators"));
    SETTINGS.put(PreferenceNames.VPS_SETTINGS, new SettingsInfo("Virtual Pinball Spreadsheet: which updates are reported and shown as columns (backglass, PUP pack, ROM, ...) and the author deny list.", "VPS"));
    SETTINGS.put(PreferenceNames.TAGGING_SETTINGS, new SettingsInfo("Automatic tagging of tables, screens and backglasses and the tags used for them.", "Tagging"));
    SETTINGS.put(PreferenceNames.RECORDER_SETTINGS, new SettingsInfo("The media recorder: recording mode, screen options, start delay, custom launcher and NVENC encoding.", "Recorder"));
    SETTINGS.put(PreferenceNames.BACKUP_SETTINGS, new SettingsInfo("Which files are included in table backups and whether existing backups are overwritten.", "Backups"));
    SETTINGS.put(PreferenceNames.MONITORING_SETTINGS, new SettingsInfo("The cabinet monitor view: mode, refresh interval, scaling and disabled screens.", "Monitoring"));
    SETTINGS.put(PreferenceNames.DOF_SETTINGS, new SettingsInfo("DirectOutput Framework: installation folder, synchronization of the DOF config (the API key is secret).", "DOF"));
    SETTINGS.put(PreferenceNames.DOFLINX_SETTINGS, new SettingsInfo("DOFLinx: installation folder and autostart.", "DOFLinx"));
    SETTINGS.put(PreferenceNames.DOF_TESTER_SETTINGS, new SettingsInfo("The DOF tester: test duration.", "DOF"));
    SETTINGS.put(PreferenceNames.ACHIEVEMENTS_SETTINGS, new SettingsInfo("Achievements: tracking, cloud sync, minimum session length and live tracking.", "Achievements"));
    SETTINGS.put(PreferenceNames.MANIA_SETTINGS, new SettingsInfo("VPin Mania: which data is shared (scores, ratings, played tables, online status) and challenge options (the API key is secret).", "VPin Mania"));
    SETTINGS.put(PreferenceNames.ISCORED_SETTINGS, new SettingsInfo("iScored: enabled flag and game rooms. Prefer the add/remove_iscored_game_room tools for the game rooms.", "iScored"));
    SETTINGS.put(PreferenceNames.WOVP_SETTINGS, new SettingsInfo("World of Virtual Pinball weekly challenges: enabled, badges, tagging, highscore reset and score submission (the API keys are secret).", "World of Virtual Pinball"));
    SETTINGS.put(PreferenceNames.WEBHOOK_SETTINGS, new SettingsInfo("Webhook sets and the events they subscribe (the endpoints and parameters are secret).", "Webhooks"));
    SETTINGS.put(PreferenceNames.DISCORD_CLIENT_SETTINGS, new SettingsInfo("The local Discord client integration: launch on startup and status updates.", "Discord"));
    SETTINGS.put(PreferenceNames.ASSET_SOURCES_SETTINGS, new SettingsInfo("The sources searched for frontend media (search_media_sources).", "Media Sources"));
    SETTINGS.put(PreferenceNames.VPXZ_SETTINGS, new SettingsInfo("VPXZ table bundles: included files and the web server that serves them.", "VPXZ"));
    SETTINGS.put(PreferenceNames.VR_SETTINGS, new SettingsInfo("VR mode of the cabinet.", "VR"));
    SETTINGS.put(PreferenceNames.PINBALLX_SETTINGS, new SettingsInfo("PinballX frontend options (the GameEx account is secret). Only relevant with PinballX.", "PinballX"));
    SETTINGS.put(PreferenceNames.PINBALLY_SETTINGS, new SettingsInfo("PinballY frontend options (charset). Only relevant with PinballY.", "PinballY"));
    SETTINGS.put(PreferenceNames.VPU_SETTINGS, new SettingsInfo("VPUniverse account for downloads (the password is secret).", "VPUniverse"));
    SETTINGS.put(PreferenceNames.VPF_SETTINGS, new SettingsInfo("VPForums account for downloads (the password is secret).", "VPForums"));
  }

  /**
   * Never offered, whatever the code above says: the AI must not change its own permissions or log in somewhere.
   */
  static final Set<String> FORBIDDEN = Set.of(PreferenceNames.MCP_SETTINGS, PreferenceNames.AUTHENTICATION_SETTINGS);

  @Autowired
  private PreferencesService preferencesService;

  @Override
  public List<McpTool> getTools() {
    List<McpTool> tools = new ArrayList<>();
    Map<String, Object> keyProp = prop("string", "The settings key, see list_settings.");
    keyProp.put("enum", new ArrayList<>(SETTINGS.keySet()));

    tools.add(McpTool.read("list_settings", "List settings",
        "Lists the VPin Studio settings an assistant can read (and, if allowed, change): the key, a description and "
            + "the preference page in VPin Studio where the user finds them.",
        schema(Map.of(), List.of()), args -> listSettings()));

    tools.add(McpTool.read("get_settings", "Get settings",
        "Returns the current values of one settings key as JSON. Secret values (passwords, API keys, tokens, webhook "
            + "URLs) are replaced by \"" + McpSecrets.MASK + "\"; an empty value means it is not set.",
        schema(Map.of("key", keyProp), List.of("key")), this::getSettings));

    Map<String, Object> changes = prop("object", "The fields to change with their new values, nested objects are merged, lists and "
        + "values are replaced. Use the field names returned by get_settings.");
    tools.add(McpTool.write(McpToolGroup.SETTINGS, "update_settings", "Update settings",
        "Changes some fields of one settings key. The other fields are kept. Returns the changed fields with their old "
            + "and new values. Secret values can not be changed. Run it with dryRun=true first and show the user the changes.",
        schema(Map.of(
            "key", keyProp,
            "changes", changes,
            "dryRun", prop("boolean", "Only validate the changes and return them, nothing is saved. Default false.")), List.of("key", "changes")),
        this::updateSettings));
    return tools;
  }

  //-------------------------------- tools ------------------------------------------------------------

  private Object listSettings() {
    List<Map<String, Object>> result = new ArrayList<>();
    SETTINGS.forEach((key, info) -> {
      Map<String, Object> s = new LinkedHashMap<>();
      s.put("key", key);
      s.put("description", info.description());
      s.put("preferencePage", info.preferencePage());
      result.add(s);
    });
    return result;
  }

  private Object getSettings(McpArguments args) {
    String key = requireKey(args);
    JsonSettings settings = load(key);
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("key", key);
    result.put("preferencePage", SETTINGS.get(key).preferencePage());
    result.put("values", McpSecrets.mask(settings));
    return result;
  }

  private Object updateSettings(McpArguments args) {
    String key = requireKey(args);
    Map<String, Object> changes = args.getMap("changes");
    if (changes == null || changes.isEmpty()) {
      throw new McpToolException("Nothing to change: pass the fields to change in \"changes\".");
    }
    boolean dryRun = args.getBoolean("dryRun", false);

    JsonSettings current = load(key);
    ObjectNode currentTree = JsonSettings.objectMapper.valueToTree(current);
    JsonNode changesTree = JsonSettings.objectMapper.valueToTree(changes);

    List<String> unknown = new ArrayList<>();
    for (String name : changesTree.propertyNames()) {
      if (!currentTree.has(name)) {
        unknown.add(name);
      }
    }
    if (!unknown.isEmpty()) {
      throw new McpToolException("Unknown fields " + unknown + " for \"" + key + "\". Known fields: " + new TreeSet<>(currentTree.propertyNames()));
    }

    ObjectNode merged = currentTree.deepCopy();
    merge(merged, changesTree);
    JsonSettings updated;
    try {
      updated = JsonSettings.objectMapper.treeToValue(merged, current.getClass());
    }
    catch (Exception e) {
      throw new McpToolException("The changes do not match the settings \"" + key + "\": " + e.getMessage());
    }

    List<String> changedSecrets = McpSecrets.getChangedSecrets(current, updated);
    if (!changedSecrets.isEmpty()) {
      throw new McpToolException("The secret values " + changedSecrets + " can not be changed by an assistant. The user can "
          + "change them in VPin Studio (" + SETTINGS.get(key).preferencePage() + "). Leave masked values out of \"changes\".");
    }

    List<Map<String, Object>> diff = new ArrayList<>();
    diff(McpSecrets.mask(current), McpSecrets.mask(updated), "", diff);

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("key", key);
    result.put("dryRun", dryRun);
    result.put("changes", diff);
    if (diff.isEmpty()) {
      result.put("message", "The values are already set, nothing changed.");
      return result;
    }
    if (dryRun) {
      result.put("message", "Nothing saved yet. Call update_settings again without dryRun to save the changes.");
      return result;
    }
    try {
      preferencesService.savePreference(updated);
    }
    catch (Exception e) {
      throw new McpToolException("Saving the settings failed: " + e.getMessage());
    }
    LOG.info("MCP: updated settings \"{}\": {}", key, diff.stream().map(d -> d.get("field")).toList());
    result.put("saved", true);
    return result;
  }

  //-------------------------------- helpers ----------------------------------------------------------

  private static String requireKey(McpArguments args) {
    String key = StringUtils.trimToNull(args.getString("key"));
    if (key == null) {
      throw new McpToolException("Missing argument \"key\".");
    }
    if (FORBIDDEN.contains(key)) {
      throw new McpToolException("The settings \"" + key + "\" are not available to assistants.");
    }
    if (!SETTINGS.containsKey(key)) {
      throw new McpToolException("Unknown settings key \"" + key + "\", see list_settings.");
    }
    return key;
  }

  private JsonSettings load(String key) {
    Class<? extends JsonSettings> clazz = PreferenceNames.getClassFromKey(key);
    JsonSettings settings = preferencesService.getJsonPreference(key, clazz);
    if (settings == null) {
      throw new McpToolException("The settings \"" + key + "\" could not be read, see the VPin Studio Server log.");
    }
    return settings;
  }

  /**
   * Objects are merged recursively, everything else replaces the current value.
   */
  static void merge(ObjectNode target, JsonNode changes) {
    for (Map.Entry<String, JsonNode> entry : changes.properties()) {
      JsonNode existing = target.get(entry.getKey());
      if (existing instanceof ObjectNode existingObject && entry.getValue().isObject()) {
        merge(existingObject, entry.getValue());
      }
      else {
        target.set(entry.getKey(), entry.getValue());
      }
    }
  }

  /**
   * Collects the changed values; objects are compared field by field, lists as a whole.
   */
  static void diff(JsonNode before, JsonNode after, String path, List<Map<String, Object>> result) {
    if (Objects.equals(before, after)) {
      return;
    }
    if (before != null && after != null && before.isObject() && after.isObject()) {
      Set<String> names = new TreeSet<>(before.propertyNames());
      names.addAll(after.propertyNames());
      for (String name : names) {
        diff(before.get(name), after.get(name), path.isEmpty() ? name : path + "." + name, result);
      }
      return;
    }
    Map<String, Object> change = new LinkedHashMap<>();
    change.put("field", path);
    change.put("old", before);
    change.put("new", after);
    result.add(change);
  }
}
