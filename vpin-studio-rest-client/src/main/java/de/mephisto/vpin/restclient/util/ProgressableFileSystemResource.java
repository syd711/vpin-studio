package de.mephisto.vpin.restclient.util;

import de.mephisto.vpin.restclient.puppacks.PupPackServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;

public class ProgressableFileSystemResource extends FileSystemResource {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private FileUploadProgressListener listener;
  private FileInputStream fileInputStream;
  private ProcessInputStream processInputStream;

  public ProgressableFileSystemResource(File file) {
    super(file);
  }

  public ProgressableFileSystemResource(File file, FileUploadProgressListener listener) {
    super(file);
    this.listener = listener;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    fileInputStream = new FileInputStream(getFile());
    processInputStream = new ProcessInputStream(fileInputStream, (int) getFile().length());
    if (listener != null) {
      processInputStream.addListener(listener);
    }
    return processInputStream;
  }

  public void close() {
    LOG.info("Closing progressable file system resource for " + getFile().getAbsolutePath());
    if (this.fileInputStream != null) {
      try {
        this.fileInputStream.close();
      }
      catch (IOException e) {
        //ignore
      }
    }

    if (processInputStream != null) {
      try {
        processInputStream.close();
      }
      catch (IOException e) {
        //ignore
      }
    }
  }
}
