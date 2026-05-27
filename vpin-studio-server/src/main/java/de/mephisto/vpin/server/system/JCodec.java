package de.mephisto.vpin.server.system;

import de.mephisto.vpin.restclient.util.SystemCommandExecutor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class JCodec {
    private final static Logger LOG = LoggerFactory.getLogger(JCodec.class);
    private static final int FRAME_INDEX = 50;

    public static boolean export(@NonNull File file, @NonNull File defaultPicture) {
        try {
            long time = System.currentTimeMillis();
            extractFrame(file, defaultPicture);
            LOG.info("Extraction from {} took {}ms", file.getAbsolutePath(), (System.currentTimeMillis() - time));
            return defaultPicture.exists();
        } catch (Exception e) {
            LOG.warn("Failed to extract video: {}", e.getMessage());
            return false;
        }
    }

    public static byte[] grab(@NonNull File file) {
        File tempFile = null;
        try {
            long time = System.currentTimeMillis();
            tempFile = File.createTempFile("vpin-frame-", ".png");
            extractFrame(file, tempFile);
            byte[] result = Files.readAllBytes(tempFile.toPath());
            LOG.info("Extraction from {} took {}ms", file.getAbsolutePath(), (System.currentTimeMillis() - time));
            return result;
        } catch (Exception e) {
            LOG.warn("Failed to extract video: {}", e.getMessage());
            return null;
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    private static void extractFrame(@NonNull File file, @NonNull File outputFile) throws Exception {
        File resources = new File(SystemService.RESOURCES);
        if (!resources.exists()) {
            resources = new File("../" + SystemService.RESOURCES);
        }

        List<String> commandList = List.of(
                "ffmpeg.exe",
                "-y",
                "-i", file.getAbsolutePath(),
                "-vf", "select=eq(n\\," + FRAME_INDEX + ")",
                "-vframes", "1",
                outputFile.getAbsolutePath()
        );

        LOG.info("Executing: {}", String.join(" ", commandList));
        SystemCommandExecutor executor = new SystemCommandExecutor(commandList);
        executor.enableLogging(true);
        executor.setDir(resources);
        executor.executeCommand();

        LOG.info("Frame extraction output: {}", executor.getStandardOutputFromCommand());
        if (!outputFile.exists() || outputFile.length() == 0) {
            throw new IOException("FFmpeg did not produce output for: " + file.getAbsolutePath());
        }
    }
}