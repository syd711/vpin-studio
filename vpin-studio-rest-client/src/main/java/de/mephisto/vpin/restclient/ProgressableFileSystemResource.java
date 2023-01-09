package de.mephisto.vpin.restclient;

import org.springframework.core.io.FileSystemResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressableFileSystemResource extends FileSystemResource {
  private FileUploadProgressListener listener;

  public ProgressableFileSystemResource(File file) {
    super(file);
  }

  public ProgressableFileSystemResource(File file, FileUploadProgressListener listener) {
    super(file);
    this.listener = listener;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    ProcessInputStream processInputStream = new ProcessInputStream(new FileInputStream(getFile()), (int) getFile().length());
    if (listener != null) {
      processInputStream.addListener(listener);
    }
    return processInputStream;
  }
}
