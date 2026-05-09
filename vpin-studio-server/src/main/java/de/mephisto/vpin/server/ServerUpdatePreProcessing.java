package de.mephisto.vpin.server;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.*;
import java.nio.file.*;
import java.util.zip.CRC32;

import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.restclient.system.NVRamsInfo;
import de.mephisto.vpin.restclient.util.PackageUtil;
import net.sf.sevenzipjbinding.SevenZip;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.*;

import static de.mephisto.vpin.commons.SystemInfo.RESOURCES;

/**
 * Service that synchronizes local files against a remote JSON manifest.
 *
 * <h2>Manifest format</h2>
 * Each entry may carry a {@code length} (bytes), a {@code crc32} (8-char hex),
 * or both. At least one must be present for validation; if both are supplied,
 * both must match before a download is skipped.
 *
 * // Synchronize remote -> local
 * FileSyncService.SyncResult result = svc.sync();
 * System.out.println(result);
 *
 * // After a manual install, refresh the manifest's length/crc values
 * svc.updateManifestFromLocal(Path.of("manifest.json"));
 */
public class ServerUpdatePreProcessing {
    private final static Logger LOG = LoggerFactory.getLogger(ServerUpdatePreProcessing.class);

    private final static String GITHUB_RESOURCES_URL = "https://raw.githubusercontent.com/syd711/vpin-studio/main/resources/";

    public static void execute() {
        ServerUpdatePreProcessing processor = new ServerUpdatePreProcessing();
        processor.doRun();
    }

    public void doRun() {

        init7zip();

        new Thread(() -> {
            try {
                Thread.currentThread().setName("ServerUpdatePreProcessing");
                long start = System.currentTimeMillis();

                runScriptCheck();

                runDownloadableInstallationsCheck();

                synchronizeNVRams(false);

                LOG.info("Finished resource updates check, took {}ms.", System.currentTimeMillis() - start);
            }
            catch (Exception e) {
                LOG.error("Server update failed: {}", e.getMessage(), e);
            }
        }).start();
    }

    private void runDownloadableInstallationsCheck() throws Exception {
         JsonMapper objectMapper =JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
                .disable(EnumFeature.READ_ENUMS_USING_TO_STRING)
                 .build();

        File manifestFile = new File(RESOURCES + "sync.json");

        // refresh manifest from github
        Updater.downloadAndOverwrite(GITHUB_RESOURCES_URL + "sync.json", manifestFile, true);

        if (!manifestFile.exists()) {
            LOG.error("Manifest file not found: {}", manifestFile.getAbsolutePath());
            return;
        }
        // else
        LOG.info("Starting sync from manifest: {}", manifestFile.getAbsolutePath());

        byte[] body = Files.readAllBytes(manifestFile.toPath());
        ServerUpdateFileEntry[] files = objectMapper.readValue(body, ServerUpdateFileEntry[].class);
        LOG.info("Manifest loaded: {} entries", files.length);

        for (ServerUpdateFileEntry entry : files) {
            File localFile = resolveDestination(entry);

            // manage deletions
            if (BooleanUtils.isTrue(entry.isDelete())) {
                if (localFile.exists()) {
                    if (entry.getName().endsWith("/") && localFile.isDirectory()) {
                        FileUtils.deleteDirectory(localFile);
                    }
                    else if (!localFile.delete()) {
                        LOG.error("Failed to clean up file: {}", localFile.getAbsolutePath());
                    }
                }
                continue;
            }

            boolean isZip = FilenameUtils.getExtension(localFile.getName()).equalsIgnoreCase("zip");

            boolean download = false;
            if (!localFile.exists()) {
                LOG.info("[MISSING] {} -> downloading", entry.getName());
                download = true;
            }
            else if (!entry.hasValidation()) {
                // No way to tell if the file is current; always refresh.
                LOG.info("[NO-VALIDATION] {} -> downloading (no checksum/length in manifest)", entry.getName());
                download = true;
            }
            // Validate local file against manifest fields.
            else if (!isUpToDate(entry, localFile)) {
                LOG.info("[OUTDATED] {} -> downloading", entry.getName());
                download = true;
            }

            if (download) {
                if (BooleanUtils.isTrue(entry.isEmptyParentFolder())) {
                    File parentFolder = localFile.getParentFile();
                    if (parentFolder.exists() && parentFolder.isDirectory()) {
                        FileUtils.deleteDirectory(parentFolder);
                    }
                }

                // File-based download: download to RESOURCES/<key>
                if (!localFile.getParentFile().exists()) {
                    localFile.getParentFile().mkdirs();
                }
                LOG.info("Downloading missing resource file {}", localFile.getAbsolutePath());

                String url = resolveUrl(entry);
                ServerUpdatePreProcessorUI.downloadWithProgressDialog(url, localFile, null);

                if (isZip) {
                    PackageUtil.unpackTargetFolder(localFile, localFile.getParentFile(), entry.getArchiveFolder(), Collections.emptyList(), null);
                }
                updateEntry(entry, localFile);
                LOG.info("[OK] {} has been installed/updated", entry.getName());
            }
            else {
                LOG.info("[OK] {} is up-to-date", entry.getName());
            }
        }
        LOG.info("Sync complete.");

        // write updated length and CRC
        objectMapper.writeValue(manifestFile, files);
        LOG.info("Local synced file updated: {}", manifestFile.getAbsolutePath());
    }

    private void updateEntry(ServerUpdateFileEntry entry, File localFile) {
        if (localFile.exists()) {
            if (entry.getLength() < 0) {
                // Local file not updated, keep force getContentLength
                return;
            }
            FileStats stats = computeStats(localFile);
            entry.setLength(stats.length);
            entry.setCrc32(stats.crc32Hex);

            LOG.info("Updated {} -> length={}, crc32={}", entry.getName(), stats.length, stats.crc32Hex);
        }
    }

    private void runScriptCheck() {
        try {
            File scriptFolder = new File(RESOURCES, "scripts/");
            scriptFolder.mkdirs();

            File emulatorLaunchScript = new File(scriptFolder, "emulator-launch.bat");
            if (!emulatorLaunchScript.exists()) {
                Files.write(emulatorLaunchScript.toPath(), "curl -X POST --data-urlencode \"table=%~1\" http://localhost:8089/service/gameLaunch".getBytes());
            }
            File emulatorExitScript = new File(scriptFolder, "emulator-exit.bat");
            if (!emulatorExitScript.exists()) {
                Files.write(emulatorExitScript.toPath(), "curl -X POST --data-urlencode \"table=%~1\" http://localhost:8089/service/gameExit".getBytes());
            }

            File frontendLaunchScript = new File(scriptFolder, "frontend-launch.bat");
            if (!frontendLaunchScript.exists()) {
                Files.write(frontendLaunchScript.toPath(), "curl -X POST --data-urlencode \"system=\" http://localhost:8089/service/frontendLaunch".getBytes());
            }
        }
        catch (Exception e) {
            LOG.error("Failed to scripting: {}", e.getMessage());
        }
    }

    private void init7zip() {
        try {
            LOG.info("Initializing 7z.");
            File sevenZipTempFolder = new File(System.getProperty("java.io.tmpdir"), "sevenZipServer/");
            sevenZipTempFolder.mkdirs();
            SevenZip.initSevenZipFromPlatformJAR(sevenZipTempFolder);
            LOG.info("7z initialized.");
        }
        catch (Exception e) {
            LOG.error("Failed to initialize sevenzip: {}", e.getMessage());
        }
    }

    public static NVRamsInfo synchronizeNVRams(boolean deleteAll) {
        NVRamsInfo info = new NVRamsInfo();
        try {
            File nvRamIndex = new File(RESOURCES, "index.txt");
            Updater.download("https://raw.githubusercontent.com/syd711/nvrams/main/index.txt", nvRamIndex, true);
            if (!nvRamIndex.exists()) {
                LOG.warn("Skipped nvram sync, download failed.");
                return null;
            }

            FileInputStream in = new FileInputStream(nvRamIndex);
            List<String> nvRams = IOUtils.readLines(in, Charset.defaultCharset());
            in.close();
            nvRamIndex.delete();

            File nvramFolder = new File(RESOURCES, "nvrams/");
            if (!nvramFolder.exists()) {
                nvramFolder.mkdirs();
            }

            for (String nvRam : nvRams) {
                File nvramFile = new File(nvramFolder, nvRam + ".nv");
                if (nvramFile.exists() && deleteAll) {
                    if (nvramFile.delete()) {
                        LOG.info("Deleted {}", nvramFile.getAbsolutePath());
                    }
                }

                if (!nvramFile.exists()) {
                    info.setCount(info.getCount() + 1);
                    Updater.download("https://raw.githubusercontent.com/syd711/nvrams/main/" + nvramFile.getName() + "/" + nvramFile.getName(), nvramFile, true);
                    LOG.info("Downloaded nvram file {}", nvramFile.getAbsolutePath());
                }
            }
            LOG.info("Finished NVRam synchronization, there are currently {} resetted nvrams available.", nvRams.size());
        }
        catch (IOException e) {
            LOG.error("Failed to sync nvrams: {}", e.getMessage(), e);
        }

        return info;
    }

    //------------------------------------------------------------------------------

    /**
     * Updates the {@code length} and {@code crc32} fields of every entry in the
     * given manifest file from the locally installed files, then writes the
     * updated manifest back to the same path.
     *
     * <p>This is useful after a manual deployment: run this method to regenerate
     * the manifest so that future sync runs reflect the current installation.
     *
     * <p>Entries whose destination file does not exist locally are left unchanged
     * (their old values, if any, are preserved) and a warning is logged.
     *
     * @param manifestFile path to the local manifest JSON to read and rewrite
     * @throws IOException if the manifest cannot be read or written
     */
    public void updateManifestFromLocal(File manifestFile) throws IOException {
        LOG.info("Updating manifest checksums from local files: " + manifestFile);

        JsonMapper objectMapper =JsonMapper.builder()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();

        ServerUpdateFileEntry[] files = objectMapper.readValue(manifestFile, ServerUpdateFileEntry[].class);

        int updated = 0;
        for (ServerUpdateFileEntry entry : files) {
            File localFile = resolveDestination(entry);
            updateEntry(entry, localFile);
            updated++;
        }

        objectMapper.writeValue(manifestFile, files);
        LOG.info("Manifest updated (" + updated + " entries) and written to: " + manifestFile);
    }

    /**
     * Returns {@code true} when the local file matches all validation criteria
     * present in the manifest entry.
     */
    private boolean isUpToDate(ServerUpdateFileEntry entry, File localFile) throws IOException {
        if (entry.getLength() != null || entry.getCrc32() != null) {
            FileStats stats = computeStats(localFile);

            if (entry.getLength() != null && stats.length != entry.getLength()) {
                LOG.info("Length mismatch for {} : local={}, manifest={}", entry.getName(), stats.length,  entry.getLength());
                return false;
            }

            if (entry.getCrc32() != null && !Strings.CI.equals(stats.crc32Hex, entry.getCrc32())) {
                LOG.info("CRC32 mismatch for {}: local={}, manifest={}", entry.getName(), stats.crc32Hex, entry.getCrc32());
                return false;
            }
        }

        return true;
    }

    /**
     * Resolves a destination path from the manifest relative to RESOURCES
     */
    private File resolveDestination(ServerUpdateFileEntry entry) throws IOException {
        String dest = StringUtils.defaultIfEmpty(entry.getDestination(), RESOURCES + entry.getName());
        return new File(dest);
    }

    private String resolveUrl(ServerUpdateFileEntry entry) {
        return StringUtils.defaultIfEmpty(entry.getUrl(), GITHUB_RESOURCES_URL + entry.getName());
    }

    /**
     * Computes the byte length and CRC-32 checksum of a local file in a
     * single streaming pass.
     */
    private FileStats computeStats(File file) {
        CRC32 crc = new CRC32();
        long length = 0;

        // Size of the buffer used for CRC computation.
        int BUFFER_SIZE = 8 * 1024; // 8 KB
        byte[] buffer = new byte[BUFFER_SIZE];

        String hex = null;
        try (InputStream in = new BufferedInputStream(new FileInputStream(file), BUFFER_SIZE)) {
            int read;
            while ((read = in.read(buffer)) != -1) {
                crc.update(buffer, 0, read);
                length += read;
            }
            // Format CRC as 12 uppercase hex characters, zero-padded
            hex = "#" + String.format("%08X", crc.getValue());
        }
        catch (IOException ioe) {
            LOG.error("cannot calculate CRC for {}", file.getAbsolutePath());
        }

        return new FileStats(length, hex);
    }

    /** Immutable holder for a file's computed length and CRC-32. */
    static class FileStats {
        long length;
        String crc32Hex;
        public FileStats(long length, String hex) {
            this.length = length;
            this.crc32Hex = hex;
        }
    }

    //-----------------------------------------------------

    public static void main(String[] args) throws IOException {
        ServerUpdatePreProcessing p = new ServerUpdatePreProcessing();
        File manifest = new File(RESOURCES, "sync.json");
        p.updateManifestFromLocal(manifest);
    }

}