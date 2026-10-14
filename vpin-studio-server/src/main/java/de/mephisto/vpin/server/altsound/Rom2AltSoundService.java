package de.mephisto.vpin.server.altsound;

import de.mephisto.vpin.connectors.github.GithubRelease;
import de.mephisto.vpin.connectors.github.GithubReleaseFactory;
import de.mephisto.vpin.connectors.github.ReleaseArtifact;
import de.mephisto.vpin.restclient.altsound.Rom2AltSoundInfo;
import de.mephisto.vpin.restclient.altsound.Rom2AltSoundOptions;
import de.mephisto.vpin.restclient.altsound.Rom2AltSoundVersion;
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
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
  private static final long LATEST_RELEASE_CACHE_MILLIS = TimeUnit.HOURS.toMillis(1);
  // the release asset of the command line program, the "gui" assets hold the original UI
  private static final String RELEASE_ASSET = "windows-x86_64";
  // the files of the release zip that are installed, the rest is ignored
  private static final List<String> RELEASE_FILES = List.of(EXE_NAME, "README.md", "LICENSE");

  private final ObjectMapper objectMapper = JsonMapper.builder().build();

  /**
   * "rom2altsound roms" reports by ROM zip, the key is path, size and modification date of the zip
   */
  private final Map<String, JsonNode> romCheckCache = new ConcurrentHashMap<>();
  private final AtomicInteger runningJobs = new AtomicInteger();

  @Nullable
  private volatile String installedVersion;
  private volatile long installedVersionExeModified;
  @Nullable
  private volatile GithubRelease latestRelease;
  private volatile long latestReleaseLoaded;

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

    if (!OSUtil.isWindows()) {
      info.setError("rom2altsound is only supported on Windows.");
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

    // checked after the ROM file, so that the file info is there without rom2altsound
    if (!info.isAvailable()) {
      info.setError("rom2altsound is not installed.");
      return info;
    }

    try {
      String cacheKey = romFile.getAbsolutePath().toLowerCase() + "|" + romFile.length() + "|" + romFile.lastModified();
      JsonNode report = romCheckCache.get(cacheKey);
      if (report == null) {
        report = runRomCheck(romFile);
        romCheckCache.put(cacheKey, report);
      }
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

  //------------------------------------------------- versions and installation

  @NonNull
  public Rom2AltSoundVersion getVersion(boolean refreshLatest) {
    Rom2AltSoundVersion version = new Rom2AltSoundVersion();
    version.setSupported(OSUtil.isWindows());
    if (!version.isSupported()) {
      version.setError("rom2altsound is only supported on Windows.");
      return version;
    }

    version.setInstalled(isAvailable());
    version.setInstalledVersion(getInstalledVersion());

    try {
      GithubRelease release = getLatestRelease(refreshLatest);
      if (release != null) {
        version.setLatestVersion(StringUtils.removeStart(release.getTag(), "v"));
        version.setLatestReleaseUrl(release.getUrl());
      }
    }
    catch (Exception e) {
      LOG.error("Failed to read the latest rom2altsound release: {}", e.getMessage(), e);
      version.setError("The latest release could not be read: " + e.getMessage());
    }

    version.setUpdateAvailable(version.getLatestVersion() != null
        && (version.getInstalledVersion() == null || isNewer(version.getLatestVersion(), version.getInstalledVersion())));
    return version;
  }

  /**
   * Downloads the latest release from GitHub into the resources folder, for the first installation and for updates.
   */
  @NonNull
  public synchronized Rom2AltSoundVersion install() throws Exception {
    if (!OSUtil.isWindows()) {
      throw new UnsupportedOperationException("rom2altsound is only supported on Windows.");
    }
    if (runningJobs.get() > 0) {
      throw new IllegalStateException("rom2altsound is running, try again when the ALT sound generation has finished.");
    }

    GithubRelease release = getLatestRelease(true);
    if (release == null) {
      throw new IllegalStateException("No rom2altsound release found on " + Rom2AltSoundVersion.RELEASES_URL);
    }
    ReleaseArtifact artifact = release.getLatestArtifact();
    if (artifact == null) {
      throw new IllegalStateException("The rom2altsound release " + release.getTag() + " has no Windows download.");
    }

    File targetFolder = getExecutable().getParentFile();
    if (!targetFolder.exists() && !targetFolder.mkdirs()) {
      throw new IllegalStateException("Failed to create " + targetFolder.getAbsolutePath());
    }

    File zip = File.createTempFile("rom2altsound-", ".zip");
    try {
      LOG.info("Downloading rom2altsound {} from {}", release.getTag(), artifact.getUrl());
      download(artifact.getUrl(), zip);
      if (extract(zip, targetFolder) == 0 || !getExecutable().exists()) {
        throw new IllegalStateException("The download did not contain " + EXE_NAME);
      }
      LOG.info("Installed rom2altsound {} into {}", release.getTag(), targetFolder.getAbsolutePath());
    }
    finally {
      if (!zip.delete()) {
        zip.deleteOnExit();
      }
    }

    // a newer PinMAME may know more sets
    romCheckCache.clear();
    installedVersion = null;
    return getVersion(false);
  }

  private void download(@NonNull String url, @NonNull File target) throws Exception {
    HttpClient client = HttpClient.newBuilder()
        .followRedirects(HttpClient.Redirect.NORMAL)
        .connectTimeout(Duration.ofSeconds(20))
        .build();
    HttpRequest request = HttpRequest.newBuilder(URI.create(url)).header("User-Agent", "VPin-Studio").GET().build();
    HttpResponse<Path> response = client.send(request, HttpResponse.BodyHandlers.ofFile(target.toPath()));
    if (response.statusCode() != 200) {
      throw new IllegalStateException("Download of " + url + " failed with HTTP " + response.statusCode());
    }
  }

  private int extract(@NonNull File zip, @NonNull File targetFolder) throws Exception {
    int count = 0;
    try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip.toPath()))) {
      ZipEntry entry;
      while ((entry = in.getNextEntry()) != null) {
        if (entry.isDirectory()) {
          continue;
        }
        // only the known files, by their name, wherever they are in the zip
        String name = new File(entry.getName()).getName();
        if (!RELEASE_FILES.contains(name)) {
          continue;
        }
        File tmp = new File(targetFolder, name + ".download");
        Files.copy(in, tmp.toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.move(tmp.toPath(), new File(targetFolder, name).toPath(), StandardCopyOption.REPLACE_EXISTING);
        count++;
      }
    }
    return count;
  }

  @Nullable
  private GithubRelease getLatestRelease(boolean refresh) throws Exception {
    if (refresh || latestRelease == null || System.currentTimeMillis() - latestReleaseLoaded > LATEST_RELEASE_CACHE_MILLIS) {
      latestRelease = GithubReleaseFactory.loadRelease(Rom2AltSoundVersion.RELEASES_URL, List.of(RELEASE_ASSET), List.of("gui"));
      latestReleaseLoaded = System.currentTimeMillis();
    }
    return latestRelease;
  }

  /**
   * "0.2.6" from "rom2altsound 0.2.6" (--version), cached until the executable changes.
   */
  @Nullable
  private String getInstalledVersion() {
    if (!isAvailable()) {
      return null;
    }
    File exe = getExecutable();
    String version = installedVersion;
    if (version != null && installedVersionExeModified == exe.lastModified()) {
      return version;
    }
    try {
      Process process = new ProcessBuilder(exe.getAbsolutePath(), "--version").redirectErrorStream(true).start();
      String output;
      try (InputStream in = process.getInputStream()) {
        output = IOUtils.toString(in, StandardCharsets.UTF_8).trim();
      }
      if (!process.waitFor(CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
        process.destroyForcibly();
        return null;
      }
      version = StringUtils.removeStart(StringUtils.removeStart(output, "rom2altsound").trim(), "v");
      installedVersion = version;
      installedVersionExeModified = exe.lastModified();
      return version;
    }
    catch (Exception e) {
      LOG.error("Failed to read the rom2altsound version: {}", e.getMessage(), e);
      return null;
    }
  }

  /**
   * Compares dotted version numbers, e.g. 0.2.10 is newer than 0.2.6.
   */
  static boolean isNewer(@NonNull String latest, @NonNull String installed) {
    String[] l = latest.split("[.-]");
    String[] i = installed.split("[.-]");
    for (int n = 0; n < Math.max(l.length, i.length); n++) {
      int a = n < l.length ? parseVersionPart(l[n]) : 0;
      int b = n < i.length ? parseVersionPart(i[n]) : 0;
      if (a != b) {
        return a > b;
      }
    }
    return false;
  }

  private static int parseVersionPart(@NonNull String s) {
    try {
      return Integer.parseInt(s.replaceAll("\\D", ""));
    }
    catch (NumberFormatException e) {
      return 0;
    }
  }

  void jobStarted() {
    runningJobs.incrementAndGet();
  }

  void jobFinished() {
    runningJobs.decrementAndGet();
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
