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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Runs rom2altsound for one ROM into a staging folder and installs the generated pack into the game's ALT sound folder.
 */
public class Rom2AltSoundJob implements Job {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  // "575 command(s) to extract"
  private static final Pattern TOTAL_PATTERN = Pattern.compile("^(\\d+) command\\(s\\) to extract");
  // "  0x0002 Music: Launch Fanfare & Main Theme   44.39 s ..."
  private static final Pattern COMMAND_PATTERN = Pattern.compile("^ {2}0x[0-9A-Fa-f]+ ");
  // "format = altsound" in the [format] section of altsound.ini
  private static final Pattern FORMAT_PATTERN = Pattern.compile("(?m)^([ \\t]*format[ \\t]*=[ \\t]*)[^\\s;]+");
  private static final int ERROR_LINES = 10;

  // the share of the progress bar for the boot and the installation, the rest is the extraction
  private static final double BOOT_PROGRESS = 0.05;
  private static final double EXTRACT_PROGRESS = 0.9;

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

      int exitCode = runExtraction(result, stagingFolder);
      if (result.isCancelled()) {
        LOG.info("rom2altsound job for \"{}\" cancelled.", rom);
        return;
      }
      if (exitCode != 0) {
        return;
      }

      File packFolder = new File(stagingFolder, rom);
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

  private int runExtraction(@NonNull JobDescriptor result, @NonNull File stagingFolder) throws Exception {
    List<String> cmd = buildCommand(stagingFolder);
    LOG.info("Executing {}", String.join(" ", cmd));

    ProcessBuilder pb = new ProcessBuilder(cmd);
    pb.directory(stagingFolder);
    pb.redirectErrorStream(true);
    Process p = pb.start();
    this.process = p;

    // the job may have been cancelled while the process was starting
    if (result.isCancelled()) {
      destroy(p);
    }

    Deque<String> lastLines = new ArrayDeque<>();
    int total = 0;
    int done = 0;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        LOG.debug("[rom2altsound] {}", line);
        if (!line.isBlank()) {
          lastLines.addLast(line);
          if (lastLines.size() > ERROR_LINES) {
            lastLines.removeFirst();
          }
        }
        if (result.isCancelled()) {
          continue;
        }

        Matcher totalMatcher = TOTAL_PATTERN.matcher(line);
        if (totalMatcher.find()) {
          total = Integer.parseInt(totalMatcher.group(1));
          result.setProgress(BOOT_PROGRESS);
          result.setStatus(ServerMessages.get("rom2altsound.status.extracting", locale, 0, total));
        }
        else if (total > 0 && COMMAND_PATTERN.matcher(line).find()) {
          done = Math.min(done + 1, total);
          result.setProgress(BOOT_PROGRESS + EXTRACT_PROGRESS * done / total);
          result.setStatus(ServerMessages.get("rom2altsound.status.extracting", locale, done, total));
        }
      }
    }

    int exitCode = p.waitFor();
    this.process = null;
    if (exitCode != 0 && !result.isCancelled()) {
      String output = String.join("\n", lastLines);
      LOG.error("rom2altsound for \"{}\" failed with exit code {}:\n{}", rom, exitCode, output);
      result.setError("rom2altsound failed with exit code " + exitCode + (lastLines.isEmpty() ? "" : ": " + lastLines.getLast()));
    }
    return exitCode;
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
    FileUtils.copyDirectory(packFolder, altSoundFolder);
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
