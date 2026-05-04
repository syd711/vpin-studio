package de.mephisto.vpin.server.highscores.parsing.nvram;

import de.mephisto.vpin.restclient.highscores.logging.SLOG;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.htmlunit.jetty.io.RuntimeIOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;


/**
 * This converter does not ensure a unified output for all text documents that are outputted.
 * Instead, it just converts the output so that unexpected empty lines for missing positions are applied already.
 */
public class NvRamOutputToScoreTextConverter {
    private final static Logger LOG = LoggerFactory.getLogger(NvRamOutputToScoreTextConverter.class);

    private static final List<NvRamOutputToRaw> svcs = new ArrayList<>();

    public static void registerParser(NvRamOutputToRaw converter) {
        svcs.add(converter);
    }

    public static boolean isSupportedRom(String rom) {
        for (NvRamOutputToRaw svc : svcs) {
            if (svc.isSupportedRom(rom)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static String convertNvRamTextToMachineReadable(@NonNull File nvRam) throws Exception {
        boolean nvOffset = false;
        File originalNVRamFile = nvRam;
        File backedUpRamFile = nvRam;

        try {
            String nvRamFileName = nvRam.getCanonicalFile().getName().toLowerCase();
            String nvRamName = FilenameUtils.getBaseName(nvRamFileName).toLowerCase();
            if (nvRamFileName.contains(" ")) {
                LOG.info("Stripping NV offset from nvram file \"{}\" to check if supported.", nvRamFileName);
                SLOG.info("Stripping NV offset from nvram file \"" + nvRamFileName + "\" to check if supported.");
                nvRamName = nvRamFileName.substring(0, nvRamFileName.indexOf(" "));

                //rename the original nvram file so that we can parse with the original name
                originalNVRamFile = new File(nvRam.getParentFile(), nvRamName + ".nv");
                if (originalNVRamFile.exists()) {
                    backedUpRamFile = new File(nvRam.getParentFile(), originalNVRamFile.getName() + ".bak");
                    if (backedUpRamFile.exists()) {
                        backedUpRamFile.delete();
                    }
                    FileUtils.copyFile(originalNVRamFile, backedUpRamFile);
                    LOG.info("Temporary renamed original nvram file {} to {}", originalNVRamFile.getAbsolutePath(), backedUpRamFile.getAbsolutePath());
                    SLOG.info("Temporary renamed original nvram file " + originalNVRamFile.getAbsolutePath() + " to " + backedUpRamFile.getAbsolutePath());
                    FileUtils.copyFile(nvRam, originalNVRamFile);
                    LOG.info("Temporary renamed actual nvram file {} to {}", nvRam.getAbsolutePath(), originalNVRamFile.getAbsolutePath());
                    SLOG.info("Temporary renamed actual nvram file " + nvRam.getAbsolutePath() + " to " + originalNVRamFile.getAbsolutePath());
                }
                nvOffset = true;
            }

            // try with registered service
            Locale locale = Locale.getDefault();
            String rom = romFromNv(originalNVRamFile);
            for (NvRamOutputToRaw svc : svcs) {
                if (svc.isSupportedRom(rom)) {
                    LOG.info("Used NvRam converter {} for {}", svc, nvRamName);
                    return String.join("\n", svc.getRaw(rom, originalNVRamFile, locale));
                }
            }
            LOG.warn("No registered converted to process nv file {}, rom {}", originalNVRamFile.getName(), rom);
            return null;
        }
        catch (RuntimeIOException ioe) {
            String error = ioe.getMessage();
            SLOG.error(error);
            LOG.error(error);
            return null;
        }
        catch (Exception e) {
            LOG.error(e.getMessage());
            throw e;
        }
        finally {
            if (nvOffset && originalNVRamFile.delete()) {
                FileUtils.copyFile(backedUpRamFile, originalNVRamFile);
                LOG.info("Restored original nvram {}", originalNVRamFile.getAbsolutePath());
            }
        }
    }

    private static String romFromNv(File nvFile) throws IOException {
        String rom = nvFile.getName();
        int dotIndex = rom.lastIndexOf('.');
        if (dotIndex > 0) rom = rom.substring(0, dotIndex);
        int hyphenIndex = rom.indexOf('-');
        if (hyphenIndex > 0) rom = rom.substring(0, hyphenIndex);
        return rom;
    }
}
