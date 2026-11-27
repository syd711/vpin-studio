package de.mephisto.vpin.server.mcp;

import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.descriptors.BackupExportDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.BackupRestoreDescriptor;
import de.mephisto.vpin.restclient.mcp.McpToolGroup;
import de.mephisto.vpin.restclient.util.OSUtil;
import de.mephisto.vpin.server.backups.BackupDescriptor;
import de.mephisto.vpin.server.backups.BackupService;
import de.mephisto.vpin.server.backups.BackupSource;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.frontend.FrontendStatusService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemBackupService;
import de.mephisto.vpin.server.system.SystemResource;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static de.mephisto.vpin.server.mcp.McpTableTools.prop;
import static de.mephisto.vpin.server.mcp.McpTableTools.schema;

/**
 * MCP tools for the cabinet itself: system information, frontend control, backups, updates, server restart and
 * shutdown. The changing tools belong to the {@link McpToolGroup#SYSTEM} group, which is off by default.
 */
@Service
public class McpSystemTools implements McpToolProvider {
  private final static Logger LOG = LoggerFactory.getLogger(McpSystemTools.class);

  private static final int MAX_BACKUPS = 100;
  private static final long SHUTDOWN_DELAY_MS = 3000;

  @Autowired
  private SystemService systemService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private FrontendStatusService frontendStatusService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private GameService gameService;

  @Autowired
  private BackupService backupService;

  @Autowired
  private SystemBackupService systemBackupService;

  @Lazy
  @Autowired
  private SystemResource systemResource;

  @Override
  public List<McpTool> getTools() {
    List<McpTool> tools = new ArrayList<>();
    Map<String, Object> none = schema(Map.of(), List.of());
    Map<String, Object> tableIds = prop("array", "The table ids, see search_tables.");
    tableIds.put("items", Map.of("type", "integer"));

    tools.add(McpTool.read("get_system_info", "Get system info",
        "Returns information on the cabinet: system name, VPin Studio version, operating system, frontend and "
            + "whether it is running, server uptime, memory and the free disk space of the table folders.",
        none, args -> getSystemInfo()));

    tools.add(McpTool.read("check_studio_update", "Check for VPin Studio update",
        "Checks GitHub for a newer VPin Studio release. Updates are installed by the user in VPin Studio.",
        none, args -> checkUpdate()));

    tools.add(McpTool.read("list_backups", "List table backups",
        "Lists the backup sources and the table backups stored in them.",
        schema(Map.of(
            "sourceId", prop("integer", "Only backups of this source."),
            "tableId", prop("integer", "Only backups of this table, see search_tables.")), List.of()),
        this::listBackups));

    tools.add(McpTool.write(McpToolGroup.SYSTEM, "restart_frontend", "Restart frontend",
        "Restarts the frontend (e.g. PinUP Popper) on the cabinet. A running game is closed.",
        none, args -> restartFrontend()));

    tools.add(McpTool.write(McpToolGroup.SYSTEM, "kill_frontend", "Stop frontend",
        "Stops the frontend and the emulators on the cabinet, e.g. to work on the tables.",
        none, args -> killFrontend()));

    tools.add(McpTool.write(McpToolGroup.SYSTEM, "create_backup", "Back up tables",
        "Creates a backup of tables (table file, backglass, PUP pack, media, highscores, ... depending on the "
            + "backup settings) in a backup source. Runs as background job, see list_jobs.",
        schema(Map.of(
            "tableIds", tableIds,
            "sourceId", prop("integer", "The backup source, see list_backups. Default the first enabled source.")), List.of("tableIds")),
        this::createBackup));

    tools.add(McpTool.write(McpToolGroup.SYSTEM, "create_settings_backup", "Back up settings",
        "Saves the VPin Studio settings, players and table data (ratings, notes, VPS links, ...) to a backup file on "
            + "the cabinet. It can be restored in VPin Studio (System Manager).",
        none, args -> createSettingsBackup()));

    tools.add(McpTool.delete(McpToolGroup.SYSTEM, "restore_backup", "Restore table backup",
        "Restores a table backup from list_backups. An installed table with the same file is overwritten, so always "
            + "confirm with the user first. Runs as background job, see list_jobs.",
        schema(Map.of(
            "sourceId", prop("integer", "The backup source, see list_backups."),
            "fileName", prop("string", "The backup file name, see list_backups."),
            "emulatorId", prop("integer", "The emulator to restore to, default the first Visual Pinball X emulator.")), List.of("sourceId", "fileName")),
        this::restoreBackup));

    tools.add(McpTool.delete(McpToolGroup.SYSTEM, "restart_server", "Restart VPin Studio Server",
        "Restarts the VPin Studio Server. The MCP connection is interrupted for about half a minute. Confirm with the user first.",
        none, args -> restartServer()));

    tools.add(McpTool.delete(McpToolGroup.SYSTEM, "shutdown_cabinet", "Shut down cabinet",
        "Shuts down the cabinet computer. It has to be switched on again by hand. Always confirm with the user first.",
        none, args -> shutdownCabinet()));
    return tools;
  }

  //-------------------------------- read -------------------------------------------------------------

  private Object getSystemInfo() {
    Map<String, Object> result = new LinkedHashMap<>();
    Object name = preferencesService.getPreferenceValue(PreferenceNames.SYSTEM_NAME);
    result.put("systemName", name != null ? String.valueOf(name) : null);
    result.put("version", systemService.getVersion());
    result.put("operatingSystem", systemService.getOperatingSystem() != null ? systemService.getOperatingSystem().name() : null);
    result.put("frontend", frontendService.getFrontendType() != null ? frontendService.getFrontendType().name() : null);
    result.put("frontendRunning", safe(() -> frontendService.isFrontendRunning()));
    result.put("uptime", Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime()).withNanos(0).toString());
    Runtime runtime = Runtime.getRuntime();
    result.put("memoryUsedMb", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
    result.put("memoryMaxMb", runtime.maxMemory() / (1024 * 1024));

    List<Map<String, Object>> folders = new ArrayList<>();
    for (GameEmulator emulator : emulatorService.getValidGameEmulators()) {
      if (StringUtils.isEmpty(emulator.getGamesDirectory())) {
        continue;
      }
      File folder = new File(emulator.getGamesDirectory());
      Map<String, Object> f = new LinkedHashMap<>();
      f.put("emulator", emulator.getName());
      f.put("folder", folder.getAbsolutePath());
      f.put("freeGb", Math.round(folder.getUsableSpace() / (1024d * 1024 * 1024) * 10) / 10d);
      f.put("totalGb", Math.round(folder.getTotalSpace() / (1024d * 1024 * 1024) * 10) / 10d);
      folders.add(f);
    }
    result.put("tableFolders", folders);
    return result;
  }

  private Object checkUpdate() {
    String current = systemService.getVersion();
    String latest = Updater.checkForUpdate();
    if (latest == null) {
      throw new McpToolException("GitHub could not be reached to check for updates.");
    }
    Map<String, Object> result = new LinkedHashMap<>();
    result.put("installedVersion", current);
    result.put("latestVersion", latest);
    result.put("updateAvailable", Updater.isLargerVersionThan(latest, current));
    result.put("releaseNotes", "https://github.com/syd711/vpin-studio/releases/tag/" + latest);
    return result;
  }

  private Object listBackups(McpArguments args) {
    Integer sourceId = args.getInt("sourceId");
    Integer tableId = args.getInt("tableId");

    List<Map<String, Object>> sources = new ArrayList<>();
    for (BackupSource source : backupService.getBackupSources()) {
      Map<String, Object> s = new LinkedHashMap<>();
      s.put("id", source.getId());
      s.put("name", source.getName());
      s.put("type", source.getType());
      s.put("enabled", source.isEnabled());
      sources.add(s);
    }

    List<BackupDescriptor> descriptors;
    if (tableId != null) {
      if (gameService.getGame(tableId) == null) {
        throw new McpToolException("No table found for id " + tableId + ", use search_tables to look up the id.");
      }
      descriptors = backupService.getBackupDescriptorForGame(tableId);
    }
    else if (sourceId != null) {
      descriptors = backupService.getBackupDescriptors(sourceId);
    }
    else {
      descriptors = backupService.getBackupDescriptors();
    }

    List<Map<String, Object>> backups = new ArrayList<>();
    for (BackupDescriptor d : descriptors) {
      if (sourceId != null && (d.getSource() == null || !sourceId.equals(d.getSource().getId().intValue()))) {
        continue;
      }
      if (backups.size() >= MAX_BACKUPS) {
        break;
      }
      Map<String, Object> b = new LinkedHashMap<>();
      b.put("fileName", d.getFilename());
      b.put("sourceId", d.getSource() != null ? d.getSource().getId() : null);
      b.put("tableName", d.getTableDetails() != null ? d.getTableDetails().getGameDisplayName() : null);
      b.put("createdAt", d.getCreatedAt() != null ? d.getCreatedAt().toString() : null);
      b.put("sizeMb", Math.round(d.getSize() / (1024d * 1024) * 10) / 10d);
      backups.add(b);
    }

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("sources", sources);
    result.put("total", descriptors.size());
    result.put("backups", backups);
    return result;
  }

  //-------------------------------- write ------------------------------------------------------------

  private Object restartFrontend() {
    LOG.info("MCP: restarting the frontend");
    frontendStatusService.restart();
    return Map.of("restarted", true);
  }

  private Object killFrontend() {
    LOG.info("MCP: stopping the frontend");
    return Map.of("stopped", frontendStatusService.terminate());
  }

  private Object createBackup(McpArguments args) {
    List<Integer> ids = args.getIntList("tableIds");
    if (ids.isEmpty()) {
      throw new McpToolException("Missing argument \"tableIds\".");
    }
    List<String> names = new ArrayList<>();
    for (Integer id : ids) {
      Game game = gameService.getGame(id);
      if (game == null) {
        throw new McpToolException("No table found for id " + id + ", use search_tables to look up the id.");
      }
      names.add(game.getGameDisplayName());
    }
    BackupSource source = resolveSource(args.getInt("sourceId"));

    BackupExportDescriptor descriptor = new BackupExportDescriptor();
    descriptor.setGameIds(new ArrayList<>(new LinkedHashSet<>(ids)));
    descriptor.setBackupSourceId(source.getId());
    //the jobs are created per table, so this runs async like the REST resource does
    new Thread(() -> {
      Thread.currentThread().setName("MCP Backup Runner");
      backupService.backupTable(descriptor);
    }).start();
    LOG.info("MCP: backing up {} tables to \"{}\"", ids.size(), source.getName());

    Map<String, Object> result = new LinkedHashMap<>();
    result.put("source", source.getName());
    result.put("tables", names);
    result.put("message", "The backups run as background jobs, see list_jobs.");
    return result;
  }

  private Object createSettingsBackup() {
    try {
      String json = systemBackupService.create();
      File folder = new File("./resources/backups");
      if (!folder.exists() && !folder.mkdirs()) {
        throw new McpToolException("The backup folder " + folder.getAbsolutePath() + " could not be created.");
      }
      String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
      File file = new File(folder, "VPin-Studio-Backup-" + timestamp + ".json");
      FileUtils.writeStringToFile(file, json, StandardCharsets.UTF_8);
      LOG.info("MCP: written settings backup {}", file.getAbsolutePath());
      return Map.of("file", file.getCanonicalPath(), "sizeKb", file.length() / 1024);
    }
    catch (McpToolException e) {
      throw e;
    }
    catch (Exception e) {
      throw new McpToolException("The backup failed: " + e.getMessage());
    }
  }

  private Object restoreBackup(McpArguments args) {
    int sourceId = args.requireInt("sourceId");
    String fileName = StringUtils.trimToNull(args.getString("fileName"));
    if (fileName == null) {
      throw new McpToolException("Missing argument \"fileName\".");
    }
    BackupDescriptor backup = backupService.getBackupDescriptors(sourceId, fileName);
    if (backup == null) {
      throw new McpToolException("No backup \"" + fileName + "\" found in source " + sourceId + ", see list_backups.");
    }
    GameEmulator emulator = resolveEmulator(args.getInt("emulatorId"));

    BackupRestoreDescriptor descriptor = new BackupRestoreDescriptor();
    descriptor.setArchiveSourceId(sourceId);
    descriptor.setFilename(fileName);
    descriptor.setEmulatorId(emulator.getId());
    LOG.info("MCP: restoring backup \"{}\"", fileName);
    boolean offered = backupService.restoreBackup(descriptor);
    if (!offered) {
      throw new McpToolException("The restore could not be started, see the VPin Studio Server log.");
    }
    return Map.of("fileName", fileName, "emulator", emulator.getName(), "message", "The restore runs as background job, see list_jobs.");
  }

  private Object restartServer() {
    if (!OSUtil.isWindows()) {
      throw new McpToolException("The server can only be restarted this way on Windows.");
    }
    LOG.info("MCP: restarting the server");
    try {
      systemResource.restart();
    }
    catch (Exception e) {
      throw new McpToolException("The restart failed: " + e.getMessage());
    }
    return Map.of("restarting", true, "message", "The server restarts now, reconnect in about half a minute.");
  }

  private Object shutdownCabinet() {
    if (!OSUtil.isWindows()) {
      throw new McpToolException("The cabinet can only be shut down this way on Windows.");
    }
    LOG.info("MCP: shutting down the cabinet");
    //give the response a moment to reach the assistant
    new Thread(() -> {
      try {
        Thread.sleep(SHUTDOWN_DELAY_MS);
        systemService.systemShutdown();
      }
      catch (InterruptedException e) {
        //ignore
      }
    }, "MCP Shutdown").start();
    return Map.of("shuttingDown", true);
  }

  //-------------------------------- helpers ----------------------------------------------------------

  private BackupSource resolveSource(Integer sourceId) {
    List<BackupSource> sources = backupService.getBackupSources();
    if (sourceId != null) {
      return sources.stream().filter(s -> s.getId() != null && s.getId().intValue() == sourceId).findFirst()
          .orElseThrow(() -> new McpToolException("No backup source found for id " + sourceId + ", see list_backups."));
    }
    return sources.stream().filter(BackupSource::isEnabled).findFirst()
        .orElseThrow(() -> new McpToolException("No backup source is enabled. The user can add one in VPin Studio (Backups)."));
  }

  private GameEmulator resolveEmulator(Integer emulatorId) {
    if (emulatorId != null) {
      GameEmulator emulator = emulatorService.getGameEmulator(emulatorId);
      if (emulator == null) {
        throw new McpToolException("No emulator found for id " + emulatorId + ", see list_emulators.");
      }
      return emulator;
    }
    List<GameEmulator> vpx = emulatorService.getVpxGameEmulators();
    if (vpx.isEmpty()) {
      throw new McpToolException("No Visual Pinball X emulator found, pass the emulatorId.");
    }
    return vpx.get(0);
  }

  private static Object safe(java.util.function.Supplier<Object> supplier) {
    try {
      return supplier.get();
    }
    catch (Exception e) {
      return null;
    }
  }
}
