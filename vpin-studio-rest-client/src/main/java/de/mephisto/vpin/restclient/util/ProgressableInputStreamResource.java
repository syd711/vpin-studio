package de.mephisto.vpin.restclient.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressableInputStreamResource implements InputStreamSource {
    private static final Logger LOG = LoggerFactory.getLogger(ProgressableInputStreamResource.class);

    private final File file;
    private final FileUploadProgressListener listener;
    private final long fileSize;
    private FileInputStream originalInputStream;
    private ProcessInputStream processInputStream;

    public ProgressableInputStreamResource(File file) {
        this(file, null);
    }

    public ProgressableInputStreamResource(File file, FileUploadProgressListener listener) {
        this.file = file;
        this.listener = listener;
        this.fileSize = file.length();
    }

    private FileInputStream createFileInputStream(File file) {
        try {
            return new FileInputStream(file);  // Safely create FileInputStream
        } catch (IOException e) {
            LOG.error("Error creating FileInputStream for file: " + file.getAbsolutePath(), e);
            return null;  // Return null or handle error accordingly
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        // Safely create the FileInputStream
        originalInputStream = createFileInputStream(file);
        if (originalInputStream == null) {
            throw new IOException("Unable to create FileInputStream for file: " + file.getAbsolutePath());
        }

        // Wrap the FileInputStream with ProcessInputStream for progress tracking
        processInputStream = new ProcessInputStream(originalInputStream, (int) fileSize);

        if (listener != null) {
            processInputStream.addListener(listener);  // Attach listener for progress tracking
        }

        return processInputStream;
    }

    public void close() {
        LOG.info("Closing progressable file system resource for " + this.file.getAbsolutePath());

        if (processInputStream != null) {
            try {
                processInputStream.close();
            } catch (IOException e) {
                LOG.error("Error closing ProcessInputStream for file: " + file.getAbsolutePath(), e);
            }
        }
    }
}
