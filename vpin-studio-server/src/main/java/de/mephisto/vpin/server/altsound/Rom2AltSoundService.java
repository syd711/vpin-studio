package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.restclient.altsound.Rom2AltSoundInfo;
import de.mephisto.vpin.restclient.altsound.Rom2AltSoundOptions;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobDescriptorFactory;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.util.OSUtil;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.jobs.JobService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.ServerMessages;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Wraps rom2altsound (https://github.com/Le-Syl21/rom2altsound), which runs PinMAME on the ROM's sound board
 * and records every sound command into an ALT sound pack.
 */
@Service
public class Rom2AltSoundService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public static final String FOLDER_NAME = "rom2altsound";
  private static final String EXE_NAME = "rom2altsound.exe";
  private static final long CHECK_TIMEOUT_SECONDS = 60;

  private final ObjectMapper objectMapper = JsonMapper.builder().build();

  @Autowired
  private AltSoundService altSoundService;

  @Autowired
  private FolderLookupService folderLookupService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  @Autowired
  private JobService jobService;

  @NonNull
  public File getExecutable() {
    return new File(SystemService.RESOURCES + FOLDER_NAME, EXE_NAME);
  }

  public boolean isAvailable() {
    return OSUtil.isWindows() && getExecutable().exists();
  }

  /**
   * Checks the ROM zip of the game with "rom2altsound roms": identifies its set and sound board
   * and whether its sound ROMs are good.
   */
  @NonNull
  public Rom2AltSoundInfo getInfo(@NonNull Game game) {
    Rom2AltSoundInfo info = new Rom2AltSoundInfo();
    info.setAvailable(isAvailable());
    info.setRom(game.getRom());

    File altSoundFolder = altSoundService.getAltSoundFolder(game);
    if (altSoundFolder != null) {
      info.setAltSoundFolder(altSoundFolder.getAbsolutePath());
      info.setAltSoundExists(game.isAltSoundAvailable());
    }

    if (!info.isAvailable()) {
      info.setError("rom2altsound is not installed.");
      return info;
    }
    if (StringUtils.isEmpty(game.getRom())) {
      info.setError("The table has no ROM.");
      return info;
    }

    File romFile = folderLookupService.getRomFile(game);
    if (romFile == null) {
      romFile = new File(folderLookupService.getRomFolder(game), game.getRom() + ".zip");
    }
    info.setRomFile(romFile.getAbsolutePath());
    info.setRomFileExists(romFile.exists());
    if (!romFile.exists()) {
      info.setError("ROM file \"" + romFile.getName() + "\" not found.");
      return info;
    }

    try {
      JsonNode report = runRomCheck(romFile);
      applyRomCheck(info, report, game.getRom());
    }
    catch (Exception e) {
      LOG.error("rom2altsound ROM check of {} failed: {}", romFile.getAbsolutePath(), e.getMessage(), e);
      info.setError("ROM check failed: " + e.getMessage());
    }
    return info;
  }

  @NonNull
  private JsonNode runRomCheck(@NonNull File romFile) throws Exception {
    List<String> cmd = List.of(getExecutable().getAbsolutePath(), "roms", "--json", "-", romFile.getAbsolutePath());
    ProcessBuilder pb = new ProcessBuilder(cmd);
    pb.directory(getExecutable().getParentFile());
    pb.redirectError(ProcessBuilder.Redirect.DISCARD);
    Process process = pb.start();
    String json = IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8);
    if (!process.waitFor(CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
      process.destroyForcibly();
      throw new IllegalStateException("rom2altsound did not finish in " + CHECK_TIMEOUT_SECONDS + "s");
    }
    if (StringUtils.isBlank(json)) {
      throw new IllegalStateException("rom2altsound returned no report, exit code " + process.exitValue());
    }
    return objectMapper.readTree(json);
  }

  private void applyRomCheck(@NonNull Rom2AltSoundInfo info, @NonNull JsonNode report, @NonNull String rom) {
    JsonNode units = report.path("units");
    if (!units.isArray() || units.isEmpty()) {
      info.setError("The ROM file could not be read.");
      return;
    }

    JsonNode unit = units.get(0);
    info.setStatus(text(unit.path("status")));
    for (JsonNode issue : unit.path("issues")) {
      info.getIssues().add(issue.asString());
    }

    // a zip may hold several sets (merged zips), prefer the one of the table's ROM
    JsonNode set = null;
    for (JsonNode s : unit.path("sets")) {
      if (set == null || rom.equalsIgnoreCase(text(s.path("set")))) {
        set = s;
      }
    }
    if (set == null) {
      info.setError("The ROM file holds no known PinMAME set.");
      return;
    }

    info.setSet(text(set.path("set")));
    info.setParent(text(set.path("parent")));
    info.setDescription(text(set.path("description")));
    info.setManufacturer(text(set.path("manufacturer")));
    info.setYear(text(set.path("year")));

    JsonNode sound = set.path("sound");
    if (sound.isMissingNode() || sound.isNull()) {
      info.setError("The ROM has no sound board.");
      return;
    }
    info.setSoundBoard(text(sound.path("board")));
    for (JsonNode i : sound.path("interfaces")) {
      info.getSoundInterfaces().add(i.asString());
    }
    info.setSoundRomId(text(sound.path("sound_rom_id")));
    info.setSoundRomsGood(sound.path("sound_roms_good").asBoolean(false));

    if (!info.isSoundRomsGood()) {
      info.setError("The sound ROMs of the ROM file are missing or bad.");
      return;
    }
    info.setSupported(true);
  }

  /**
   * Starts the generation of an ALT sound pack for the game's ROM as a job.
   */
  @NonNull
  public JobDescriptor generate(@NonNull Game game, @NonNull Rom2AltSoundOptions options) {
    if (!isAvailable()) {
      return JobDescriptorFactory.error("rom2altsound is not installed.");
    }
    String rom = game.getRom();
    if (StringUtils.isEmpty(rom)) {
      return JobDescriptorFactory.error("The table has no ROM.");
    }

    File romFolder = folderLookupService.getRomFolder(game);
    File romFile = folderLookupService.getRomFile(game);
    if (romFile == null || !romFile.exists()) {
      return JobDescriptorFactory.error("ROM file \"" + rom + ".zip\" not found in " + romFolder.getAbsolutePath());
    }

    File altSoundFolder = altSoundService.getAltSoundFolder(game);
    if (altSoundFolder == null) {
      return JobDescriptorFactory.error("No ALT sound folder could be determined.");
    }
    if (altSoundFolder.exists() && !options.isOverwrite() && altSoundService.isAltSoundAvailable(game)) {
      return JobDescriptorFactory.error("An ALT sound already exists in " + altSoundFolder.getAbsolutePath());
    }

    Locale locale = resolveLocale();
    Rom2AltSoundJob job = new Rom2AltSoundJob(this, game.getId(), rom, romFolder, altSoundFolder, options, locale);
    JobDescriptor jobDescriptor = new JobDescriptor(JobType.ALTSOUND_GENERATE);
    jobDescriptor.setTitle(ServerMessages.get("rom2altsound.title", locale, rom));
    jobDescriptor.setGameId(game.getId());
    jobDescriptor.setJob(job);
    jobService.offer(jobDescriptor);
    LOG.info("Offered rom2altsound job for ROM \"{}\", target folder {}", rom, altSoundFolder.getAbsolutePath());
    return jobDescriptor;
  }

  /**
   * Called by the job once the pack has been installed.
   */
  void installed(int gameId, @NonNull String rom, boolean enableAltSound) {
    if (enableAltSound) {
      altSoundService.setAltSoundEnabled(rom, true);
    }
    altSoundService.clearCache();
    gameLifecycleService.notifyGameAssetsChanged(gameId, AssetType.ALT_SOUND, rom);
  }

  @Nullable
  private static String text(@NonNull JsonNode node) {
    return node.isMissingNode() || node.isNull() ? null : node.asString();
  }

  private static Locale resolveLocale() {
    try {
      ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attrs != null) {
        HttpServletRequest request = attrs.getRequest();
        return ServerMessages.parseLocale(request.getHeader("Accept-Language"));
      }
    }
    catch (Exception ignored) {
    }
    return Locale.ENGLISH;
  }
}
