package de.mephisto.vpin.connectors.wovp.patcher;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Java port of the "WoVP Challenges Patcher" (wovp.ps1) by World of Virtual Pinball.
 * <p>
 * Works on a VPX table that has been extracted with <code>vpxtool extract</code>:
 * <ul>
 *   <li>adds the two timers "WoVP_Timer" (1000ms) and "WoVP_FastTimer" (50ms) to the game items</li>
 *   <li>appends the WoVP telemetry block (wovp_script.vbs) to the table script</li>
 * </ul>
 * The folder has to be assembled with <code>vpxtool assemble</code> afterwards.
 * Running the vpxtool is not part of this class, since only the server knows where it is installed.
 */
public class WovpTablePatcher {
  private final static Logger LOG = LoggerFactory.getLogger(WovpTablePatcher.class);

  public static final String TIMER_NAME = "WoVP_Timer";
  public static final String FAST_TIMER_NAME = "WoVP_FastTimer";
  public static final int TIMER_INTERVAL = 1000;
  public static final int FAST_TIMER_INTERVAL = 50;

  static final String GAMEITEMS_INDEX = "gameitems.json";
  static final String GAMEITEMS_FOLDER = "gameitems";
  static final String SCRIPT_FILE = "script.vbs";
  static final String SCRIPT_TEMPLATE = "wovp_script.vbs";

  /** Same check as wovp.ps1: the script is considered injected when the timer sub exists. */
  private static final Pattern INJECTED_SCRIPT_PATTERN = Pattern.compile("(?im)^\\s*Sub\\s+WoVP_Timer_Timer\\b");
  /** Second line of the injected block, used to find its beginning when the patch is removed. */
  private static final String SCRIPT_BLOCK_MARKER = "' WoVP Telemetry";

  private static final String FILE_NAME = "file_name";
  private static final byte[] UTF8_BOM = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};

  private static final JsonMapper mapper = JsonMapper.builder()
      .enable(SerializationFeature.INDENT_OUTPUT)
      .build();

  private WovpTablePatcher() {
  }

  /**
   * Adds the WoVP timers and the telemetry script to an extracted table.
   *
   * @param tableDir the folder created by "vpxtool extract", containing gameitems.json
   * @return PATCHED or ALREADY_PATCHED
   */
  @NonNull
  public static WovpPatchStatus patch(@NonNull File tableDir) throws IOException {
    File indexFile = getIndexFile(tableDir);
    ArrayNode index = readIndex(indexFile);

    if (!findWovpEntries(index).isEmpty()) {
      LOG.info("WoVP timers already exist in {}, nothing to do.", tableDir.getAbsolutePath());
      return WovpPatchStatus.ALREADY_PATCHED;
    }

    // wovp.ps1 clones the first existing timer, so that all fields of the vpxtool version in use are kept.
    // Unlike the original, a timer is created from scratch when the table has none.
    ObjectNode templateEntry = null;
    ObjectNode templateItem = null;
    for (JsonNode entry : index) {
      String fileName = getFileName(entry);
      if (fileName != null && isTimerFile(fileName)) {
        File itemFile = resolveGameItem(tableDir, fileName);
        if (itemFile != null) {
          templateEntry = (ObjectNode) entry;
          templateItem = (ObjectNode) mapper.readTree(itemFile);
          LOG.info("Using timer \"{}\" as template for the WoVP timers.", fileName);
          break;
        }
      }
    }

    File gameItemsDir = new File(tableDir, GAMEITEMS_FOLDER);
    if (!gameItemsDir.exists() && !gameItemsDir.mkdirs()) {
      throw new IOException("Failed to create folder " + gameItemsDir.getAbsolutePath());
    }

    addTimer(index, gameItemsDir, templateEntry, templateItem, TIMER_NAME, TIMER_INTERVAL);
    addTimer(index, gameItemsDir, templateEntry, templateItem, FAST_TIMER_NAME, FAST_TIMER_INTERVAL);
    writeJson(indexFile, index);

    File scriptFile = getScriptFile(tableDir);
    byte[] script = Files.readAllBytes(scriptFile.toPath());
    if (isScriptInjected(script)) {
      LOG.info("WoVP script is already part of {}, skipped injection.", scriptFile.getAbsolutePath());
    }
    else {
      Files.write(scriptFile.toPath(), injectScript(script, loadScriptTemplate()));
      LOG.info("Injected WoVP script into {}", scriptFile.getAbsolutePath());
    }
    return WovpPatchStatus.PATCHED;
  }

  /**
   * Removes the WoVP timers and the telemetry script from an extracted table.
   * This is not supported by the original patcher, which requires the original table to be re-downloaded.
   *
   * @return UNPATCHED or NOT_PATCHED
   */
  @NonNull
  public static WovpPatchStatus unpatch(@NonNull File tableDir) throws IOException {
    File indexFile = getIndexFile(tableDir);
    ArrayNode index = readIndex(indexFile);
    boolean changed = false;

    List<Integer> wovpEntries = findWovpEntries(index);
    for (int i = wovpEntries.size() - 1; i >= 0; i--) {
      int pos = wovpEntries.get(i);
      String fileName = getFileName(index.get(pos));
      File itemFile = fileName != null ? resolveGameItem(tableDir, fileName) : null;
      if (itemFile != null) {
        Files.delete(itemFile.toPath());
      }
      index.remove(pos);
      LOG.info("Removed WoVP game item {}", fileName);
      changed = true;
    }
    if (changed) {
      writeJson(indexFile, index);
    }

    File scriptFile = getScriptFile(tableDir);
    byte[] script = Files.readAllBytes(scriptFile.toPath());
    byte[] cleaned = removeScript(script);
    if (cleaned != null) {
      Files.write(scriptFile.toPath(), cleaned);
      LOG.info("Removed WoVP script from {}", scriptFile.getAbsolutePath());
      changed = true;
    }

    return changed ? WovpPatchStatus.UNPATCHED : WovpPatchStatus.NOT_PATCHED;
  }

  /**
   * @return true if the given table script contains the WoVP telemetry block.
   */
  public static boolean isPatched(@Nullable String script) {
    return script != null && INJECTED_SCRIPT_PATTERN.matcher(script).find();
  }

  @NonNull
  public static byte[] loadScriptTemplate() throws IOException {
    try (InputStream in = WovpTablePatcher.class.getResourceAsStream(SCRIPT_TEMPLATE)) {
      if (in == null) {
        throw new IOException("WoVP script template " + SCRIPT_TEMPLATE + " not found.");
      }
      return stripBom(in.readAllBytes());
    }
  }

  //------------------------------------- game items -------------------------------------------------------------------

  private static void addTimer(@NonNull ArrayNode index, @NonNull File gameItemsDir,
                               @Nullable ObjectNode templateEntry, @Nullable ObjectNode templateItem,
                               @NonNull String name, int interval) throws IOException {
    ObjectNode item = templateItem != null ? templateItem.deepCopy() : createTimerItem();
    ObjectNode timer = getItemData(item);
    timer.put("name", name);
    timer.put("timer_interval", interval);
    timer.put("is_timer_enabled", true);
    JsonNode center = timer.get("center");
    if (center instanceof ObjectNode c) {
      // same offset as wovp.ps1, so that the timers do not overlap with their template in the editor
      if (c.has("x")) {
        c.put("x", c.get("x").asDouble() + 40);
      }
      if (c.has("y")) {
        c.put("y", c.get("y").asDouble() + 40);
      }
    }

    String fileName = "Timer." + name + ".json";
    File itemFile = new File(gameItemsDir, fileName);
    if (itemFile.exists()) {
      throw new IOException("Game item " + itemFile.getAbsolutePath() + " already exists.");
    }
    writeJson(itemFile, item);

    ObjectNode entry = templateEntry != null ? templateEntry.deepCopy() : createIndexEntry();
    entry.put(FILE_NAME, fileName);
    index.add(entry);
    LOG.info("Added timer {} ({}ms) as {}", name, interval, fileName);
  }

  /**
   * vpxtool wraps the item fields into an object named after the item type, e.g. {"Timer": {...}}.
   */
  @NonNull
  private static ObjectNode getItemData(@NonNull ObjectNode item) throws IOException {
    JsonNode timer = item.get("Timer");
    if (timer instanceof ObjectNode t) {
      return t;
    }
    if (item.has("name")) {
      return item;
    }
    throw new IOException("Unsupported timer format: " + item);
  }

  @NonNull
  private static ObjectNode createTimerItem() {
    ObjectNode item = mapper.createObjectNode();
    ObjectNode timer = item.putObject("Timer");
    ObjectNode center = timer.putObject("center");
    center.put("x", 0d);
    center.put("y", 0d);
    timer.put("is_timer_enabled", true);
    timer.put("timer_interval", TIMER_INTERVAL);
    timer.put("name", TIMER_NAME);
    timer.put("backglass", false);
    return item;
  }

  @NonNull
  private static ObjectNode createIndexEntry() {
    ObjectNode entry = mapper.createObjectNode();
    entry.put(FILE_NAME, "");
    entry.put("is_locked", false);
    entry.put("editor_layer", 0);
    return entry;
  }

  @NonNull
  private static List<Integer> findWovpEntries(@NonNull ArrayNode index) {
    List<Integer> result = new ArrayList<>();
    for (int i = 0; i < index.size(); i++) {
      String fileName = getFileName(index.get(i));
      if (fileName != null) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        if (lower.contains(TIMER_NAME.toLowerCase(Locale.ROOT)) || lower.contains(FAST_TIMER_NAME.toLowerCase(Locale.ROOT))) {
          result.add(i);
        }
      }
    }
    return result;
  }

  private static boolean isTimerFile(@NonNull String fileName) {
    String leaf = new File(fileName.replace('\\', '/')).getName();
    return leaf.toLowerCase(Locale.ROOT).startsWith("timer.");
  }

  @Nullable
  private static String getFileName(@Nullable JsonNode entry) {
    if (entry == null || !entry.isObject()) {
      return null;
    }
    for (Map.Entry<String, JsonNode> property : entry.properties()) {
      if (property.getKey().equalsIgnoreCase(FILE_NAME) && property.getValue().isString()) {
        return property.getValue().stringValue();
      }
    }
    return null;
  }

  @Nullable
  private static File resolveGameItem(@NonNull File tableDir, @NonNull String fileName) {
    File direct = new File(tableDir, fileName);
    if (direct.isFile()) {
      return direct;
    }
    File inFolder = new File(new File(tableDir, GAMEITEMS_FOLDER), new File(fileName.replace('\\', '/')).getName());
    if (inFolder.isFile()) {
      return inFolder;
    }
    return null;
  }

  //------------------------------------- script -----------------------------------------------------------------------

  /*
   * The script is handled as bytes: vpxtool writes it with the encoding of the table and the WoVP block is pure ASCII,
   * so decoding and encoding it again could break non-ASCII characters of the original script.
   */

  static boolean isScriptInjected(byte[] script) {
    return isPatched(new String(script, StandardCharsets.ISO_8859_1));
  }

  static byte[] injectScript(byte[] script, byte[] block) {
    int end = trimmedLength(script, script.length);
    ByteArrayOutputStream out = new ByteArrayOutputStream(end + block.length + 8);
    out.write(script, 0, end);
    out.writeBytes("\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
    out.writeBytes(block);
    out.writeBytes("\r\n".getBytes(StandardCharsets.US_ASCII));
    return out.toByteArray();
  }

  /**
   * @return the script without the WoVP block or null if the block was not found.
   */
  @Nullable
  static byte[] removeScript(byte[] script) {
    String text = new String(script, StandardCharsets.ISO_8859_1);
    if (!isPatched(text)) {
      return null;
    }
    int marker = text.lastIndexOf(SCRIPT_BLOCK_MARKER);
    if (marker < 0) {
      return null;
    }
    // the block starts with the "' =====" line in front of the marker
    int lineStart = text.lastIndexOf('\n', marker - 1);
    int blockStart = lineStart >= 0 ? text.lastIndexOf('\n', lineStart - 1) + 1 : 0;
    if (!text.startsWith("'", blockStart)) {
      blockStart = lineStart + 1;
    }
    int end = trimmedLength(script, blockStart);
    ByteArrayOutputStream out = new ByteArrayOutputStream(end + 2);
    out.write(script, 0, end);
    out.writeBytes("\r\n".getBytes(StandardCharsets.US_ASCII));
    return out.toByteArray();
  }

  private static int trimmedLength(byte[] data, int length) {
    int end = length;
    while (end > 0 && (data[end - 1] == ' ' || data[end - 1] == '\t' || data[end - 1] == '\r' || data[end - 1] == '\n')) {
      end--;
    }
    // keep a BOM of an otherwise empty script
    return Math.max(end, startsWithBom(data) ? Math.min(3, length) : 0);
  }

  private static boolean startsWithBom(byte[] data) {
    return data.length >= 3 && data[0] == UTF8_BOM[0] && data[1] == UTF8_BOM[1] && data[2] == UTF8_BOM[2];
  }

  private static byte[] stripBom(byte[] data) {
    if (startsWithBom(data)) {
      byte[] result = new byte[data.length - 3];
      System.arraycopy(data, 3, result, 0, result.length);
      return result;
    }
    return data;
  }

  @NonNull
  private static File getScriptFile(@NonNull File tableDir) throws IOException {
    File scriptFile = new File(tableDir, SCRIPT_FILE);
    if (scriptFile.isFile()) {
      return scriptFile;
    }
    File[] scripts = tableDir.listFiles((dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".vbs"));
    if (scripts == null || scripts.length == 0) {
      throw new IOException("No .vbs file found in " + tableDir.getAbsolutePath());
    }
    if (scripts.length > 1) {
      throw new IOException("Multiple .vbs files found in " + tableDir.getAbsolutePath());
    }
    return scripts[0];
  }

  //------------------------------------- json -------------------------------------------------------------------------

  @NonNull
  private static File getIndexFile(@NonNull File tableDir) throws IOException {
    File indexFile = new File(tableDir, GAMEITEMS_INDEX);
    if (!indexFile.isFile()) {
      throw new IOException(GAMEITEMS_INDEX + " not found in " + tableDir.getAbsolutePath());
    }
    return indexFile;
  }

  @NonNull
  private static ArrayNode readIndex(@NonNull File indexFile) throws IOException {
    JsonNode root = mapper.readTree(stripBom(Files.readAllBytes(indexFile.toPath())));
    if (root instanceof ArrayNode array) {
      return array;
    }
    throw new IOException("Unexpected format of " + indexFile.getAbsolutePath() + ", an array of game items was expected.");
  }

  private static void writeJson(@NonNull File file, @NonNull JsonNode node) throws IOException {
    Files.writeString(file.toPath(), mapper.writeValueAsString(node), StandardCharsets.UTF_8);
  }
}
