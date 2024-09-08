package de.mephisto.vpin.restclient.util;

import org.springframework.core.io.FileSystemResource;
import java.io.*;

public class MyResource extends FileSystemResource {
    private final File file;
    private FileUploadProgressListener listener;

    public MyResource(File file, FileUploadProgressListener listener) {
        super(file);
        this.file = file;
        this.listener = listener;
    }

    @Override
    public File getFile() {
        return new FileWithProgress(file, listener);
    }

    public static class FileWithProgress extends File {
        private final FileUploadProgressListener listener;

        public FileWithProgress(File file, FileUploadProgressListener listener) {
            super(file.getAbsolutePath());
            this.listener = listener;
        }

        public OutputStream getOutputStream() throws IOException {
            final long totalBytes = length();
            return new ProgressOutputStream(new FileOutputStream(this), totalBytes, listener);
        }
    }

    public static class ProgressOutputStream extends FilterOutputStream {
        private final FileUploadProgressListener listener;
        private long bytesWritten = 0;
        private final long totalBytes;

        protected ProgressOutputStream(OutputStream out, long totalBytes, FileUploadProgressListener listener) {
            super(out);
            this.totalBytes = totalBytes;
            this.listener = listener;
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            super.write(b, off, len);
            bytesWritten += len;
            listener.process(((double) bytesWritten / totalBytes) * 100);
        }

        @Override
        public void write(int b) throws IOException {
            super.write(b);
            bytesWritten++;
            listener.process(((double) bytesWritten / totalBytes) * 100);
        }
    }
}
