package de.mephisto.vpin.server.achievements.scoreserver;

import de.mephisto.vpin.restclient.achievements.VpxPluginStatus;
import de.mephisto.vpin.restclient.util.OSUtil;
import de.mephisto.vpin.commons.SystemInfo;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.GameEmulator;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Installs the VPX plugins for the live tracking (VPX 10.8.1+, Windows, 64 bit): superhac's score-server and the
 * PinMAME plugin with the "GetController" change. Both are built by {@code .github/workflows/build-vpx-plugins.yml}
 * and published as pre-releases "vpx-plugins-*" of the VPin Studio repository (port of the Watcher's
 * {@code ensure_vpx_plugins}).
 * <p>
 * The newest build for the installed VPX revision is used. The official PinMAME plugin is backed up and restored when
 * the live tracking is switched off. When a VPX update replaces the PinMAME plugin by a new official version, the
 * live tracking stays off until a new plugin build is released.
 */
@Service
public class VpxPluginInstaller {
  private static final Logger LOG = LoggerFactory.getLogger(VpxPluginInstaller.class);

  private static final String RELEASES_API = "https://api.github.com/repos/syd711/vpin-studio/releases?per_page=30";
  private static final String TAG_PREFIX = "vpx-plugins-";
  private static final String ZIP_ASSET = "vpx-plugins-win64.zip";
  private static final String MANIFEST_ASSET = "vpx-plugins-manifest.json";
  private static final String MIN_VPX_VERSION = "10.8.1";
  /** vpinball release tags: v10.8.1-5436-af26b2d93 */
  private static final Pattern VPX_REF_PATTERN = Pattern.compile("v?(\\d+\\.\\d+\\.\\d+)-(\\d+)-.*");

  public static final String SCORE_SERVER_ID = "ScoreServer";
  public static final String PINMAME_ID = "PinMAME";
  private static final String SCORE_SERVER_FOLDER = "score-server";
  private static final String SCORE_SERVER_DLL = "plugin-score-server64.dll";
  private static final String PINMAME_FOLDER = "pinmame";
  private static final String PINMAME_DLL = "plugin-pinmame64.dll";
  private static final String PINMAME_BACKUP_SUFFIX = ".studio-original";

  @Autowired
  private EmulatorService emulatorService;

  private final ObjectMapper mapper = JsonMapper.builder().build();
  private final HttpClient http = HttpClient.newBuilder()
      .connectTimeout(Duration.ofSeconds(15))
      .followRedirects(HttpClient.Redirect.ALWAYS)
      .build();
  private final ReentrantLock lock = new ReentrantLock();
  private volatile VpxPluginStatus status = new VpxPluginStatus();

  @NonNull
  public VpxPluginStatus getStatus() {
    return status;
  }

  @NonNull
  public File getFolder() {
    return new File(SystemInfo.RESOURCES, "achievements/vpx-plugins");
  }

  /**
   * Installs or repairs the plugins and enables them in VPinballX.ini. Nothing is changed while VPX is running.
   */
  @NonNull
  public VpxPluginStatus ensure() {
    // an installation started in the background (setting changed) is waited for
    lock.lock();
    try {
      status = doEnsure();
      if (status.ready) {
        LOG.info("VPX plugins ready: {} ({})", status.installedTag, status.vpxFolder);
      }
      else {
        LOG.info("VPX plugins not ready: {} {}", status.state, status.detail != null ? status.detail : "");
      }
      return status;
    }
    catch (Exception e) {
      LOG.error("VPX plugin installation failed: {}", e.getMessage(), e);
      status = copy(status, VpxPluginStatus.FAILED, "Plugin installation failed: " + e.getMessage());
      status.ready = false;
      return status;
    }
    finally {
      lock.unlock();
    }
  }

  /**
   * Restores the official PinMAME plugin and disables the score-server plugin (live tracking switched off).
   */
  @NonNull
  public VpxPluginStatus uninstall() {
    return uninstall(false);
  }

  /**
   * Restores the official PinMAME plugin and disables the score-server plugin.
   *
   * @param removeFiles also deletes the score-server plugin folder and the backup of the official PinMAME plugin
   *                    (uninstall button), otherwise the files are kept for a quick re-activation
   */
  @NonNull
  public VpxPluginStatus uninstall(boolean removeFiles) {
    lock.lock();
    try {
      VpxPluginStatus result = new VpxPluginStatus();
      result.state = VpxPluginStatus.DISABLED;
      ObjectNode state = loadState();
      File vpxFolder = resolveVpxFolder(state);
      if (vpxFolder == null) {
        status = result;
        return result;
      }
      result.vpxFolder = vpxFolder.getAbsolutePath();
      if (isVpxRunning()) {
        result.state = VpxPluginStatus.BUSY;
        result.detail = removeFiles
            ? "VPX is running, close VPX to uninstall the plugins."
            : "VPX is running, the official PinMAME plugin is restored after VPX was closed.";
        status = result;
        return result;
      }
      File pmDll = new File(vpxFolder, "plugins/" + PINMAME_FOLDER + "/" + PINMAME_DLL);
      File backup = new File(pmDll.getParentFile(), PINMAME_DLL + PINMAME_BACKUP_SUFFIX);
      if (pmDll.isFile() && backup.isFile() && strings(state.get("patchedShas")).contains(sha256(pmDll))) {
        Files.copy(backup.toPath(), pmDll.toPath(), StandardCopyOption.REPLACE_EXISTING);
        LOG.info("Restored the official PinMAME plugin {}", pmDll.getAbsolutePath());
      }
      File ini = VpxIni.find(appData(), vpxFolder, state.path("vpxVersion").asString(null));
      if (ini != null) {
        VpxIni.setPluginEnabled(ini, SCORE_SERVER_ID, false);
      }
      if (removeFiles) {
        File ssDir = new File(vpxFolder, "plugins/" + SCORE_SERVER_FOLDER);
        if (ssDir.exists()) {
          FileUtils.deleteDirectory(ssDir);
          LOG.info("Deleted the score-server plugin {}", ssDir.getAbsolutePath());
        }
        // the backup is only obsolete once the official plugin is back in place
        if (backup.isFile() && pmDll.isFile() && !strings(state.get("patchedShas")).contains(sha256(pmDll))) {
          Files.delete(backup.toPath());
        }
      }
      state.remove("installedTag");
      saveState(state);
      status = result;
      return result;
    }
    catch (Exception e) {
      LOG.error("Failed to uninstall the VPX plugins: {}", e.getMessage(), e);
      VpxPluginStatus result = new VpxPluginStatus();
      result.state = VpxPluginStatus.FAILED;
      result.detail = e.getMessage();
      status = result;
      return result;
    }
    finally {
      lock.unlock();
    }
  }

  /**
   * @return true if the score-server plugin files are present in the VPX plugins folder (also when disabled)
   */
  public boolean isInstalled() {
    File vpxFolder = resolveVpxFolder(loadState());
    return vpxFolder != null && new File(vpxFolder, "plugins/" + SCORE_SERVER_FOLDER + "/" + SCORE_SERVER_DLL).isFile();
  }

  /**
   * The installation the plugins were written to, the current VPX installation if the state got lost.
   */
  @Nullable
  private File resolveVpxFolder(ObjectNode state) {
    String vpxFolder = state.path("vpxFolder").asString("");
    if (!vpxFolder.isEmpty() && new File(vpxFolder).isDirectory()) {
      return new File(vpxFolder);
    }
    if (!OSUtil.isWindows()) {
      return null;
    }
    GameEmulator emulator = findVpxEmulator();
    return emulator != null && emulator.getInstallationFolder().isDirectory() ? emulator.getInstallationFolder() : null;
  }

  //------------------------------------------- Installation ----------------------------------------------------------

  private VpxPluginStatus doEnsure() throws Exception {
    VpxPluginStatus res = new VpxPluginStatus();
    if (!OSUtil.isWindows()) {
      res.state = VpxPluginStatus.NOT_SUPPORTED;
      res.detail = "The live tracking is only available on Windows.";
      return res;
    }
    GameEmulator emulator = findVpxEmulator();
    if (emulator == null) {
      res.state = VpxPluginStatus.VPX_NOT_FOUND;
      return res;
    }
    File vpxFolder = emulator.getInstallationFolder();
    String version = emulator.getEmulatorVersion();
    res.vpxFolder = vpxFolder.getAbsolutePath();
    res.vpxVersion = version;
    if (!isSupportedVersion(version)) {
      res.state = VpxPluginStatus.VPX_OLD;
      return res;
    }

    ObjectNode state = loadState();
    PackageLookup lookup = findPackage(state, version);
    Package pkg = lookup.pkg();
    if (pkg == null) {
      rollbackIncompatible(state, version);
      return noPackage(res, version, lookup.newestOther());
    }
    InstallPlan plan = InstallPlan.create(vpxFolder, pkg, state);
    boolean running = isVpxRunning();

    if (plan.needPinmame() && isOfficialPinmameUpdated(plan, state, vpxFolder)) {
      res.state = VpxPluginStatus.REBUILD_NEEDED;
      res.detail = "A VPX update installed a new PinMAME plugin, the live tracking is off until a new plugin build is released.";
      return res;
    }
    if (plan.needsCopy() && running) {
      res.state = VpxPluginStatus.BUSY;
      res.detail = "VPX is running, the plugins are installed after VPX was closed.";
      return res;
    }

    try {
      copyPlugins(plan, pkg, state);
    }
    catch (AccessDeniedException e) {
      res.state = VpxPluginStatus.FAILED;
      res.detail = "No write access to the VPX plugins folder (" + e.getMessage() + ").";
      return res;
    }
    recordInstallation(state, plan, pkg, vpxFolder, version);
    if (plan.needsCopy()) {
      LOG.info("Installed the VPX plugins {} into {}", plan.tag(), vpxFolder.getAbsolutePath());
    }

    if (!enablePlugins(res, vpxFolder, version, running)) {
      return res;
    }
    res.state = VpxPluginStatus.READY;
    res.ready = true;
    res.installedTag = plan.tag();
    return res;
  }

  /**
   * Plugins installed before the build check (or for another VPX build) can stop VPX from starting: the official
   * PinMAME plugin is restored and the score-server disabled.
   */
  private void rollbackIncompatible(ObjectNode state, String version) {
    String installedTag = state.path("installedTag").asString("");
    if (installedTag.isEmpty() || isVpxRunning()) {
      return;
    }
    LOG.warn("The installed VPX plugins {} do not match VPX {}, restoring the official PinMAME plugin.", installedTag, version);
    uninstall(false);
  }

  private static boolean isSupportedVersion(@Nullable String version) {
    return version != null && VpxIni.compareVersion(version, MIN_VPX_VERSION) >= 0;
  }

  /**
   * What has to be copied into one VPX installation for the given package.
   *
   * @param currentSha the hash of the installed PinMAME plugin, empty if there is none
   * @param ownBuilds  the hashes of the PinMAME plugins built by the VPin Studio that were installed before
   */
  private record InstallPlan(String tag, String patchedSha, File pmDir, File pmDll, File ssDir,
                             String currentSha, List<String> ownBuilds, boolean needPinmame, boolean needServer) {

    static InstallPlan create(File vpxFolder, Package pkg, ObjectNode state) throws IOException {
      String tag = pkg.manifest().path("tag").asString("");
      String patchedSha = manifestSha(pkg.manifest(), PINMAME_FOLDER, PINMAME_DLL);
      String serverSha = manifestSha(pkg.manifest(), SCORE_SERVER_FOLDER, SCORE_SERVER_DLL);
      File pmDir = new File(vpxFolder, "plugins/" + PINMAME_FOLDER);
      File pmDll = new File(pmDir, PINMAME_DLL);
      File ssDir = new File(vpxFolder, "plugins/" + SCORE_SERVER_FOLDER);
      File ssDll = new File(ssDir, SCORE_SERVER_DLL);

      String currentSha = pmDll.isFile() ? sha256(pmDll) : "";
      boolean needPinmame = !currentSha.equals(patchedSha);
      boolean needServer = !ssDll.isFile() || !sha256(ssDll).equals(serverSha);
      return new InstallPlan(tag, patchedSha, pmDir, pmDll, ssDir, currentSha, strings(state.get("patchedShas")),
          needPinmame, needServer);
    }

    boolean needsCopy() {
      return needPinmame || needServer;
    }

    /**
     * @return true if an official PinMAME plugin is installed (none of the own builds)
     */
    boolean hasOfficialPinmame() {
      return !currentSha.isEmpty() && !ownBuilds.contains(currentSha);
    }
  }

  private static String manifestSha(JsonNode manifest, String folder, String dll) {
    return manifest.path("files").path(folder + "/" + dll).asString("").toLowerCase(Locale.ROOT);
  }

  /**
   * Detects a VPX update: the build is the same as before, but another official PinMAME plugin than the backed up
   * one is installed. Only applies to the installation the state was written for, the backup hash of another VPX
   * installation says nothing about this one.
   */
  private static boolean isOfficialPinmameUpdated(InstallPlan plan, ObjectNode state, File vpxFolder) {
    boolean sameInstallation = vpxFolder.getAbsolutePath().equalsIgnoreCase(state.path("vpxFolder").asString(""));
    boolean newBuild = !plan.tag().equals(state.path("installedTag").asString(""));
    String originalSha = state.path("originalSha").asString("");
    return sameInstallation && !newBuild && !originalSha.isEmpty()
        && plan.hasOfficialPinmame() && !plan.currentSha().equals(originalSha);
  }

  private void copyPlugins(InstallPlan plan, Package pkg, ObjectNode state) throws IOException {
    if (plan.needServer()) {
      FileUtils.copyDirectory(new File(pkg.folder(), SCORE_SERVER_FOLDER), plan.ssDir());
    }
    if (plan.needPinmame()) {
      copyPinmame(plan, pkg, state);
    }
  }

  private void copyPinmame(InstallPlan plan, Package pkg, ObjectNode state) throws IOException {
    File pmDir = plan.pmDir();
    if (!pmDir.exists() && !pmDir.mkdirs()) {
      throw new IOException("Failed to create " + pmDir.getAbsolutePath());
    }
    // back up the official plugin, never one of the own builds
    if (plan.hasOfficialPinmame()) {
      Files.copy(plan.pmDll().toPath(), new File(pmDir, PINMAME_DLL + PINMAME_BACKUP_SUFFIX).toPath(), StandardCopyOption.REPLACE_EXISTING);
      state.put("originalSha", plan.currentSha());
    }
    Files.copy(new File(pkg.folder(), PINMAME_FOLDER + "/" + PINMAME_DLL).toPath(), plan.pmDll().toPath(), StandardCopyOption.REPLACE_EXISTING);
    if (!sha256(plan.pmDll()).equals(plan.patchedSha())) {
      throw new IOException("The patched PinMAME plugin did not verify after the copy.");
    }
  }

  private void recordInstallation(ObjectNode state, InstallPlan plan, Package pkg, File vpxFolder, String version) throws IOException {
    List<String> ownBuilds = new ArrayList<>(plan.ownBuilds());
    if (!plan.patchedSha().isEmpty() && !ownBuilds.contains(plan.patchedSha())) {
      ownBuilds.add(plan.patchedSha());
    }
    ArrayNode shas = state.putArray("patchedShas");
    ownBuilds.subList(Math.max(0, ownBuilds.size() - 10), ownBuilds.size()).forEach(shas::add);
    state.put("installedTag", plan.tag());
    state.put("cache", pkg.folder().getAbsolutePath());
    state.put("vpxFolder", vpxFolder.getAbsolutePath());
    state.put("vpxVersion", version);
    saveState(state);
  }

  /**
   * Enables both plugins in VPinballX.ini.
   *
   * @return false if this was not possible, the reason is written into the given status
   */
  private boolean enablePlugins(VpxPluginStatus res, File vpxFolder, String version, boolean running) throws IOException {
    File ini = VpxIni.find(appData(), vpxFolder, version);
    if (ini == null) {
      res.state = VpxPluginStatus.FAILED;
      res.detail = "VPinballX.ini not found, start VPX once so that it creates its settings.";
      return false;
    }
    if (VpxIni.isPluginEnabled(ini, SCORE_SERVER_ID) && VpxIni.isPluginEnabled(ini, PINMAME_ID)) {
      return true;
    }
    if (running) {
      res.state = VpxPluginStatus.BUSY;
      res.detail = "VPX is running, the plugins are enabled after VPX was closed.";
      return false;
    }
    VpxIni.setPluginEnabled(ini, SCORE_SERVER_ID, true);
    VpxIni.setPluginEnabled(ini, PINMAME_ID, true);
    LOG.info("Enabled the score-server and PinMAME plugins in {}", ini.getAbsolutePath());
    return true;
  }

  //------------------------------------------- Emulator --------------------------------------------------------------

  /**
   * Several VPX emulators can be configured (e.g. older versions next to a 10.8.1). The 64-bit installation with the
   * newest version is used, so an older emulator listed first does not hide a supported one. If none is supported,
   * the newest one is returned to report its version.
   */
  @Nullable
  private GameEmulator findVpxEmulator() {
    GameEmulator best = null;
    for (GameEmulator emulator : emulatorService.getVpxGameEmulators()) {
      if (!hasVpx64Executable(emulator)) {
        continue;
      }
      if (best == null || VpxIni.compareVersion(emulator.getEmulatorVersion(), best.getEmulatorVersion()) > 0) {
        best = emulator;
      }
    }
    return best;
  }

  private static boolean hasVpx64Executable(GameEmulator emulator) {
    File folder = emulator.getInstallationFolder();
    if (folder == null || !folder.isDirectory()) {
      return false;
    }
    File[] exes = folder.listFiles(f -> f.getName().toLowerCase(Locale.ROOT).matches("vpinballx.*64\\.exe"));
    return exes != null && exes.length > 0;
  }

  public static boolean isVpxRunning() {
    return ProcessHandle.allProcesses().anyMatch(p -> p.info().command()
        .map(c -> new File(c).getName().toLowerCase(Locale.ROOT))
        .filter(n -> n.startsWith("vpinballx") && n.endsWith(".exe"))
        .isPresent());
  }

  //------------------------------------------- Releases --------------------------------------------------------------

  private record Package(JsonNode manifest, File folder) {
  }

  /**
   * @param pkg         the package for the installed VPX revision, null if there is none
   * @param newestOther the VPX version of the newest build for another revision, null if there is none
   */
  private record PackageLookup(@Nullable Package pkg, @Nullable String newestOther) {
  }

  /**
   * Looks up the newest plugin build for the given VPX revision, falls back to the cached package when GitHub is not
   * reachable.
   */
  private PackageLookup findPackage(ObjectNode state, String version) {
    Package pkg = null;
    String newestOther = null;
    try {
      for (JsonNode release : fetchReleases()) {
        JsonNode manifest = fetchManifest(release);
        if (manifest == null) {
          continue;
        }
        String buildVersion = pluginVpxVersion(manifest);
        if (VpxIni.isPluginCompatible(version, buildVersion)) {
          pkg = download(release, manifest);
          break;
        }
        if (newestOther == null) {
          newestOther = buildVersion;
        }
      }
    }
    catch (Exception e) {
      LOG.warn("Failed to fetch the VPX plugin releases: {}", e.getMessage());
    }
    if (pkg == null) {
      pkg = cachedPackage(state, version);
    }
    return new PackageLookup(pkg, newestOther);
  }

  /**
   * @return the VPX version the plugins were built against including the build number ("10.8.1.5436"), taken from
   * "vpx_build" or the "vpx_ref" tag (v10.8.1-5436-af26b2d93), only major.minor.revision if neither is available
   */
  static String pluginVpxVersion(JsonNode manifest) {
    String version = manifest.path("vpx_version").asString("");
    String build = manifest.path("vpx_build").asString("");
    if (build.isEmpty()) {
      Matcher m = VPX_REF_PATTERN.matcher(manifest.path("vpx_ref").asString(""));
      if (m.matches() && VpxIni.sameRevision(version, m.group(1))) {
        build = m.group(2);
      }
    }
    return build.isEmpty() ? version : version + "." + build;
  }

  private static VpxPluginStatus noPackage(VpxPluginStatus res, String version, @Nullable String newestOther) {
    if (newestOther != null && VpxIni.compareVersion(version, newestOther) < 0) {
      // the installed VPX is older than the plugin build, updating VPX helps
      res.state = VpxPluginStatus.VPX_UPDATE_NEEDED;
      res.requiredVpxVersion = newestOther;
    }
    else if (newestOther != null) {
      res.state = VpxPluginStatus.REBUILD_NEEDED;
      res.detail = "VPX " + version + " is installed, the newest plugin build is for VPX " + newestOther + ".";
    }
    else {
      res.state = VpxPluginStatus.NO_BUILD;
    }
    return res;
  }

  /**
   * @return the vpx-plugins-* releases, newest first
   */
  private List<JsonNode> fetchReleases() throws Exception {
    JsonNode json = mapper.readTree(get(RELEASES_API));
    List<JsonNode> result = new ArrayList<>();
    if (json != null && json.isArray()) {
      for (JsonNode r : json) {
        if (r.path("tag_name").asString("").startsWith(TAG_PREFIX) && !r.path("draft").asBoolean(false)) {
          result.add(r);
        }
      }
    }
    result.sort((a, b) -> b.path("published_at").asString("").compareTo(a.path("published_at").asString("")));
    return result;
  }

  @Nullable
  private JsonNode fetchManifest(JsonNode release) {
    String url = assetUrl(release, MANIFEST_ASSET);
    if (url == null) {
      return null;
    }
    try {
      return mapper.readTree(get(url));
    }
    catch (Exception e) {
      LOG.warn("Failed to read the manifest of {}: {}", release.path("tag_name").asString(""), e.getMessage());
      return null;
    }
  }

  @Nullable
  private static String assetUrl(JsonNode release, String name) {
    for (JsonNode a : release.path("assets")) {
      if (name.equals(a.path("name").asString(""))) {
        return a.path("browser_download_url").asString(null);
      }
    }
    return null;
  }

  /**
   * Downloads and unpacks the plugin package into resources/achievements/vpx-plugins/&lt;tag&gt;, checks the hashes.
   */
  private Package download(JsonNode release, JsonNode releaseManifest) throws Exception {
    String tag = release.path("tag_name").asString("");
    File cache = new File(getFolder(), tag);
    File manifestFile = new File(cache, "manifest.json");
    if (manifestFile.isFile()) {
      JsonNode manifest = mapper.readTree(manifestFile);
      if (verify(cache, manifest)) {
        return new Package(manifest, cache);
      }
    }
    String url = assetUrl(release, ZIP_ASSET);
    if (url == null) {
      throw new IOException("Release " + tag + " has no " + ZIP_ASSET);
    }
    LOG.info("Downloading the VPX plugins {}", tag);
    byte[] zip = get(url);
    File tmp = new File(getFolder(), tag + ".tmp");
    FileUtils.deleteQuietly(tmp);
    unzip(zip, tmp);
    JsonNode manifest = mapper.readTree(new File(tmp, "manifest.json"));
    if (!verify(tmp, manifest) || !manifest.path("files").equals(releaseManifest.path("files"))) {
      FileUtils.deleteQuietly(tmp);
      throw new IOException("The plugin package " + tag + " did not verify.");
    }
    FileUtils.deleteQuietly(cache);
    Files.move(tmp.toPath(), cache.toPath());
    return new Package(manifest, cache);
  }

  @Nullable
  private Package cachedPackage(ObjectNode state, String version) {
    String cache = state.path("cache").asString("");
    if (cache.isEmpty()) {
      return null;
    }
    try {
      File folder = new File(cache);
      JsonNode manifest = mapper.readTree(new File(folder, "manifest.json"));
      if (VpxIni.isPluginCompatible(version, pluginVpxVersion(manifest)) && verify(folder, manifest)) {
        return new Package(manifest, folder);
      }
    }
    catch (Exception e) {
      // no usable cache
    }
    return null;
  }

  private static boolean verify(File folder, JsonNode manifest) throws IOException {
    JsonNode files = manifest.path("files");
    if (!files.isObject() || files.isEmpty()) {
      return false;
    }
    for (Map.Entry<String, JsonNode> e : files.properties()) {
      File f = new File(folder, e.getKey());
      if (!f.isFile() || !sha256(f).equalsIgnoreCase(e.getValue().asString(""))) {
        return false;
      }
    }
    return true;
  }

  static void unzip(byte[] zip, File target) throws IOException {
    String root = target.getCanonicalPath() + File.separator;
    try (ZipInputStream in = new ZipInputStream(new ByteArrayInputStream(zip))) {
      ZipEntry entry;
      while ((entry = in.getNextEntry()) != null) {
        File dest = new File(target, entry.getName().replace('\\', '/'));
        if (!dest.getCanonicalPath().startsWith(root)) {
          continue; // path traversal
        }
        if (entry.isDirectory()) {
          dest.mkdirs();
          continue;
        }
        dest.getParentFile().mkdirs();
        Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }
    }
  }

  private byte[] get(String url) throws Exception {
    HttpRequest request = HttpRequest.newBuilder(URI.create(url))
        .timeout(Duration.ofSeconds(120))
        .header("User-Agent", "VPin-Studio")
        .header("Accept", url.startsWith("https://api.github.com") ? "application/vnd.github+json" : "application/octet-stream")
        .GET().build();
    HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
    if (response.statusCode() != 200) {
      throw new IOException("HTTP " + response.statusCode() + " for " + url);
    }
    return response.body();
  }

  //------------------------------------------- Helpers ---------------------------------------------------------------

  private File stateFile() {
    return new File(getFolder(), "state.json");
  }

  private ObjectNode loadState() {
    try {
      File f = stateFile();
      if (f.isFile()) {
        JsonNode json = mapper.readTree(f);
        if (json instanceof ObjectNode o) {
          return o;
        }
      }
    }
    catch (Exception e) {
      LOG.warn("Failed to read the VPX plugin state: {}", e.getMessage());
    }
    return mapper.createObjectNode();
  }

  private void saveState(ObjectNode state) throws IOException {
    File f = stateFile();
    f.getParentFile().mkdirs();
    Files.writeString(f.toPath(), mapper.writerWithDefaultPrettyPrinter().writeValueAsString(state));
  }

  private static List<String> strings(@Nullable JsonNode array) {
    List<String> result = new ArrayList<>();
    if (array != null && array.isArray()) {
      for (JsonNode n : array) {
        result.add(n.asString("").toLowerCase(Locale.ROOT));
      }
    }
    return result;
  }

  static String sha256(File file) throws IOException {
    try (InputStream in = Files.newInputStream(file.toPath())) {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] buffer = new byte[1 << 16];
      int n;
      while ((n = in.read(buffer)) > 0) {
        digest.update(buffer, 0, n);
      }
      return HexFormat.of().formatHex(digest.digest());
    }
    catch (java.security.NoSuchAlgorithmException e) {
      throw new IOException(e);
    }
  }

  private static File appData() {
    String appData = System.getenv("APPDATA");
    return appData != null ? new File(appData) : new File(System.getProperty("user.home"), "AppData/Roaming");
  }

  private static VpxPluginStatus copy(VpxPluginStatus s, String state, String detail) {
    VpxPluginStatus c = new VpxPluginStatus();
    c.state = state;
    c.detail = detail;
    c.ready = s.ready;
    c.connected = s.connected;
    c.vpxVersion = s.vpxVersion;
    c.vpxFolder = s.vpxFolder;
    c.installedTag = s.installedTag;
    c.requiredVpxVersion = s.requiredVpxVersion;
    return c;
  }
}
