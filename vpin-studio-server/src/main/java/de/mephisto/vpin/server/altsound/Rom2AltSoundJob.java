package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.AltSoundFormats;
import de.mephisto.vpin.restclient.altsound.Rom2AltSoundOptions;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.util.SystemUtil;
import de.mephisto.vpin.server.util.ServerMessages;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Runs rom2altsound for one ROM into a staging folder and installs the generated pack into the game's ALT sound folder.
 */
public class Rom2AltSoundJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  // "format = altsound" in the [format] section of altsound.ini
  private static final Pattern FORMAT_PATTERN = Pattern.compile("(?m)^([ \\t]*format[ \\t]*=[ \\t]*)[^\\s;]+");
  private static final int ERROR_LINES = 10;

  // with --progress json, the ROM's log is written into the pack folder, it is not installed
  private static final String PACK_LOG_FILE = "rom2altsound.log";
  // with --progress json, stdout only holds the JSON lines, the other messages go to stderr
  private static final String STDERR_FILE = "rom2altsound-stderr.log";

  private static final String STATUS_OK = "ok";
  private static final String STATUS_NOT_APPLICABLE = "not_applicable";
  private static final String PASS_MAIN = "main";

  // the progress bar: the boot until 5%, the main recording pass until 80%, the check passes after it until 95%
  private static final double BOOT_PROGRESS = 0.05;
  private static final double MAIN_PASS_END = 0.8;
  private static final double CHECK_PASSES_END = 0.95;

  private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

  private final Rom2AltSoundService service;
  private final int gameId;
  private final String rom;
  private final File romFolder;
  private final File altSoundFolder;
  private final Rom2AltSoundOptions options;
  // resolved on the request thread, the job runs on a worker thread without request context
  private final Locale locale;

  @Nullable
  private volatile Process process;

  // the current recording pass and its share of the progress bar
  @Nullable
  private String pass;
  private double passStart;
  private double passEnd;

  public Rom2AltSoundJob(@NonNull Rom2AltSoundService service, int gameId, @NonNull String rom, @NonNull File romFolder,
                         @NonNull File altSoundFolder, @NonNull Rom2AltSoundOptions options, @NonNull Locale locale) {
    this.service = service;
    this.gameId = gameId;
    this.rom = rom;
    this.romFolder = romFolder;
    this.altSoundFolder = altSoundFolder;
    this.options = options;
    this.locale = locale;
  }

  @Override
  public void execute(JobDescriptor result) {
    File stagingFolder = null;
    long start = System.currentTimeMillis();
    service.jobStarted();
    try {
      stagingFolder = Files.createTempDirectory("rom2altsound-").toFile();
      result.setStatus(ServerMessages.get("rom2altsound.status.booting", locale, rom));

      JsonNode done = runExtraction(result, stagingFolder);
      if (result.isCancelled()) {
        LOG.info("rom2altsound job for \"{}\" cancelled.", rom);
        return;
      }
      if (done == null) {
        // the error is set already when rom2altsound failed without its "done" line
        if (result.getError() == null) {
          result.setError("rom2altsound reported no result for \"" + rom + "\".");
        }
        return;
      }

      File packFolder = new File(stagingFolder, rom);
      String status = text(done, "status");
      if (STATUS_NOT_APPLICABLE.equals(status)) {
        LOG.info("rom2altsound cannot make an ALT sound for \"{}\": {}", rom, text(done, "reason"));
        result.setError("rom2altsound cannot make an ALT sound for \"" + rom + "\": " + text(done, "reason"));
        return;
      }
      if (!STATUS_OK.equals(status)) {
        logPackLog(packFolder);
        result.setError("rom2altsound failed for \"" + rom + "\": " + text(done, "error"));
        return;
      }
      if (done.path("written").asInt(0) == 0) {
        logPackLog(packFolder);
        result.setError("rom2altsound wrote no sound files for \"" + rom + "\".");
        return;
      }
      if (!new File(packFolder, "altsound.csv").exists()) {
        result.setError("rom2altsound did not write an ALT sound pack for \"" + rom + "\".");
        return;
      }

      result.setStatus(ServerMessages.get("rom2altsound.status.installing", locale, altSoundFolder.getAbsolutePath()));
      install(packFolder);
      service.installed(gameId, rom, options.isEnableAltSound());

      result.setProgress(1);
      LOG.info("rom2altsound pack for \"{}\" installed to {}, took {}s", rom, altSoundFolder.getAbsolutePath(), (System.currentTimeMillis() - start) / 1000);
    }
    catch (Exception e) {
      LOG.error("rom2altsound job for \"{}\" failed: {}", rom, e.getMessage(), e);
      result.setError("ALT sound generation failed: " + e.getMessage());
    }
    finally {
      service.jobFinished();
      if (stagingFolder != null && !SystemUtil.deleteFileOrFolder(stagingFolder)) {
        LOG.warn("Failed to delete rom2altsound staging folder {}", stagingFolder.getAbsolutePath());
      }
    }
  }

  /**
   * Runs rom2altsound and follows its JSON progress lines, returns the ROM's "done" line, null if there is none.
   */
  @Nullable
  private JsonNode runExtraction(@NonNull JobDescriptor result, @NonNull File stagingFolder) throws Exception {
    List<String> cmd = buildCommand(stagingFolder);
    LOG.info("Executing {}", String.join(" ", cmd));

    File stderrFile = new File(stagingFolder, STDERR_FILE);
    ProcessBuilder pb = new ProcessBuilder(cmd);
    pb.directory(stagingFolder);
    pb.redirectError(stderrFile);
    Process p = pb.start();
    this.process = p;

    // the job may have been cancelled while the process was starting
    if (result.isCancelled()) {
      destroy(p);
    }

    JsonNode done = null;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        LOG.debug("[rom2altsound] {}", line);
        JsonNode event = parse(line);
        if (event == null) {
          continue;
        }
        String stage = text(event, "stage");
        if ("done".equals(stage) && rom.equalsIgnoreCase(text(event, "rom"))) {
          done = event;
        }
        if (!result.isCancelled()) {
          onProgress(result, stage, event);
        }
      }
    }

    int exitCode = p.waitFor();
    this.process = null;
    if (done == null && exitCode != 0 && !result.isCancelled()) {
      List<String> lastLines = tail(stderrFile);
      LOG.error("rom2altsound for \"{}\" failed with exit code {}:\n{}", rom, exitCode, String.join("\n", lastLines));
      result.setError("rom2altsound failed with exit code " + exitCode + (lastLines.isEmpty() ? "" : ": " + lastLines.getLast()));
    }
    return done;
  }

  private void onProgress(@NonNull JobDescriptor result, @Nullable String stage, @NonNull JsonNode event) {
    if (stage == null) {
      return;
    }
    switch (stage) {
      case "cold-boot" -> result.setStatus(ServerMessages.get("rom2altsound.status.factory", locale, rom));
      case "boot" -> result.setStatus(ServerMessages.get("rom2altsound.status.booting", locale, rom));
      case "sam" -> result.setStatus(ServerMessages.get("rom2altsound.status.decoding", locale, rom));
      case "record" -> onRecord(result, event);
      case "pack" -> {
        result.setProgress(CHECK_PASSES_END);
        result.setStatus(ServerMessages.get("rom2altsound.status.packing", locale));
      }
      default -> {
        // "start", "done" and the stages of later versions
      }
    }
  }

  private void onRecord(@NonNull JobDescriptor result, @NonNull JsonNode event) {
    String eventPass = text(event, "pass");
    int total = event.path("total").asInt(0);
    if (eventPass == null || total <= 0) {
      return;
    }

    if (!eventPass.equals(pass)) {
      pass = eventPass;
      if (PASS_MAIN.equals(eventPass)) {
        passStart = BOOT_PROGRESS;
        passEnd = MAIN_PASS_END;
      }
      else {
        // the number of check passes is not known up front, each one takes half of what is left
        passStart = Math.max(result.getProgress(), MAIN_PASS_END);
        passEnd = passStart + (CHECK_PASSES_END - passStart) / 2;
      }
    }

    int done = Math.min(event.path("done").asInt(0), total);
    result.setProgress(passStart + (passEnd - passStart) * done / total);
    String key = switch (eventPass) {
      case PASS_MAIN -> "rom2altsound.status.extracting";
      case "retry" -> "rom2altsound.status.retrying";
      default -> "rom2altsound.status.checking";
    };
    result.setStatus(ServerMessages.get(key, locale, done, total));
  }

  @Nullable
  private static JsonNode parse(@NonNull String line) {
    if (!line.startsWith("{")) {
      return null;
    }
    try {
      return OBJECT_MAPPER.readTree(line);
    }
    catch (Exception e) {
      LOG.warn("Invalid rom2altsound progress line \"{}\": {}", line, e.getMessage());
      return null;
    }
  }

  @Nullable
  private static String text(@NonNull JsonNode node, @NonNull String field) {
    JsonNode value = node.path(field);
    return value.isMissingNode() || value.isNull() ? null : value.asString();
  }

  /**
   * The last non-blank lines of the file, empty if it cannot be read.
   */
  @NonNull
  private static List<String> tail(@NonNull File file) {
    if (!file.exists()) {
      return List.of();
    }
    try {
      List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8).stream().filter(l -> !l.isBlank()).toList();
      return lines.subList(Math.max(0, lines.size() - ERROR_LINES), lines.size());
    }
    catch (Exception e) {
      LOG.warn("Failed to read {}: {}", file.getAbsolutePath(), e.getMessage());
      return List.of();
    }
  }

  private void logPackLog(@NonNull File packFolder) {
    List<String> lastLines = tail(new File(packFolder, PACK_LOG_FILE));
    if (!lastLines.isEmpty()) {
      LOG.error("rom2altsound log of \"{}\":\n{}", rom, String.join("\n", lastLines));
    }
  }

  @NonNull
  private List<String> buildCommand(@NonNull File stagingFolder) {
    List<String> cmd = new ArrayList<>();
    cmd.add(service.getExecutable().getAbsolutePath());
    // the set name is looked up in --roms, so that the parent zip of a clone is found next to it
    cmd.add(rom);
    cmd.add("--roms");
    cmd.add(romFolder.getAbsolutePath());
    cmd.add("--out");
    cmd.add(stagingFolder.getAbsolutePath());
    cmd.add("--jobs");
    cmd.add("1");
    cmd.add("--no-html");
    cmd.add("--progress");
    cmd.add("json");
    cmd.add("--volume");
    cmd.add(Rom2AltSoundOptions.VOLUME_REFERENCE.equalsIgnoreCase(options.getVolume()) ? Rom2AltSoundOptions.VOLUME_REFERENCE : Rom2AltSoundOptions.VOLUME_FACTORY);
    if (options.getMaxSecs() > 0) {
      cmd.add("--max-secs");
      cmd.add(String.valueOf(options.getMaxSecs()));
    }
    // 0 is a valid value for both: no loop search / no extended intro file
    if (options.getLoopMaxSecs() >= 0) {
      cmd.add("--loop-max-secs");
      cmd.add(String.valueOf(options.getLoopMaxSecs()));
    }
    if (options.getIntroLoopSecs() >= 0) {
      cmd.add("--intro-loop-secs");
      cmd.add(String.valueOf(options.getIntroLoopSecs()));
    }
    if (options.isMergeTwins()) {
      cmd.add("--merge-twins");
    }
    if (options.isDcBlock()) {
      cmd.add("--dc-block");
    }
    if (options.isNoFactory()) {
      cmd.add("--no-factory");
    }
    if (options.isCheckDucking()) {
      cmd.add("--check-ducking");
    }
    if (options.isNoChipCheck()) {
      cmd.add("--no-chip-check");
    }
    if (options.isBsmtHle()) {
      cmd.add("--bsmt-hle");
    }
    if (options.getLimit() > 0) {
      cmd.add("--limit");
      cmd.add(String.valueOf(options.getLimit()));
    }
    return cmd;
  }

  private void install(@NonNull File packFolder) throws Exception {
    if (altSoundFolder.exists() && options.isOverwrite()) {
      LOG.info("Replacing existing ALT sound folder {}", altSoundFolder.getAbsolutePath());
      if (!SystemUtil.deleteFileOrFolder(altSoundFolder)) {
        throw new IllegalStateException("Failed to delete the existing ALT sound folder " + altSoundFolder.getAbsolutePath());
      }
    }
    if (!altSoundFolder.exists() && !altSoundFolder.mkdirs()) {
      throw new IllegalStateException("Failed to create ALT sound folder " + altSoundFolder.getAbsolutePath());
    }
    // the staging folder is a temp folder, likely on another drive, so copy instead of moving
    FileUtils.copyDirectory(packFolder, altSoundFolder, f -> !(f.isFile() && PACK_LOG_FILE.equals(f.getName())));
    switchToGSound(new File(altSoundFolder, "altsound.ini"));
  }

  /**
   * rom2altsound writes altsound.csv and g-sound.csv and selects the former in altsound.ini; we always use G-Sound.
   */
  private void switchToGSound(@NonNull File ini) throws Exception {
    if (!ini.exists()) {
      throw new IllegalStateException("rom2altsound did not write " + ini.getAbsolutePath());
    }
    String content = FileUtils.readFileToString(ini, StandardCharsets.UTF_8);
    String updated = FORMAT_PATTERN.matcher(content).replaceFirst("$1" + AltSoundFormats.gsound);
    if (updated.equals(content)) {
      LOG.warn("No [format] entry found in {}, the pack keeps its format.", ini.getAbsolutePath());
      return;
    }
    FileUtils.writeStringToFile(ini, updated, StandardCharsets.UTF_8);
  }

  @Override
  public void cancel(JobDescriptor jobDescriptor) {
    Job.super.cancel(jobDescriptor);
    Process p = this.process;
    if (p != null) {
      destroy(p);
    }
    LOG.info("Cancelled rom2altsound job for \"{}\"", rom);
  }

  private static void destroy(@NonNull Process p) {
    p.descendants().forEach(ProcessHandle::destroyForcibly);
    p.destroyForcibly();
  }
}
