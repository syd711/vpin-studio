package de.mephisto.vpin.connectors.github;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.DecimalFormat;

public class Downloader {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final String downloadUrl;
  private final ReleaseArtifactActionLog installLog;

  Downloader(String downloadUrl, ReleaseArtifactActionLog installLog) {
    this.downloadUrl = downloadUrl;
    this.installLog = installLog;
  }

  @NonNull
  public File download(@Nullable File targetFile) throws Exception {
    if (targetFile != null) {
      if (targetFile.isDirectory()) {
        installLog.setStatus("The download target must be a file: " + targetFile.getAbsolutePath());
        throw new UnsupportedOperationException("The download target must be a file: " + targetFile.getAbsolutePath());
      }
      if (targetFile.exists() && !targetFile.delete()) {
        installLog.setStatus("Failed to delete " + targetFile.getAbsolutePath());
        throw new UnsupportedOperationException("Failed to delete " + targetFile.getAbsolutePath());
      }
      File parent = targetFile.getAbsoluteFile().getParentFile();
      if (parent != null && !parent.exists() && !parent.mkdirs()) {
        installLog.setStatus("Failed to create folder " + parent.getAbsolutePath());
        throw new UnsupportedOperationException("Failed to create folder " + parent.getAbsolutePath());
      }
    }
    else {
      String[] split = downloadUrl.split("/");
      String name = split[split.length - 1];
      targetFile = File.createTempFile(FilenameUtils.getBaseName(name), "." + FilenameUtils.getExtension(name));
      //delete the file again, we only need the reference before writing.
      targetFile.delete();
    }

    installLog.log("Downloading " + downloadUrl + " to " + targetFile.getAbsolutePath());
    LOG.info("Downloading " + downloadUrl + " to " + targetFile.getAbsolutePath());
    long start = System.currentTimeMillis();

    // the file is written next to the target and renamed when complete, so the target never holds a partial download
    File tmp = new File(targetFile.getAbsoluteFile().getParentFile(), targetFile.getName() + ".bak");
    if (tmp.exists()) {
      tmp.delete();
    }

    URL url = URI.create(downloadUrl).toURL();
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setConnectTimeout(10000);
    connection.setReadTimeout(30000);
    connection.setInstanceFollowRedirects(true);
    long expectedLength;
    long read = 0;
    try {
      int responseCode = connection.getResponseCode();
      if (responseCode >= 400) {
        throw new UnsupportedOperationException("Download of " + downloadUrl + " failed with HTTP " + responseCode);
      }
      expectedLength = connection.getContentLengthLong();

      try (InputStream in = new BufferedInputStream(connection.getInputStream());
           OutputStream out = new FileOutputStream(tmp)) {
        byte[] dataBuffer = new byte[64 * 1024];
        int bytesRead;
        while ((bytesRead = in.read(dataBuffer)) != -1) {
          out.write(dataBuffer, 0, bytesRead);
          read += bytesRead;
        }
      }
    }
    catch (Exception e) {
      tmp.delete();
      installLog.setStatus("Download of " + downloadUrl + " failed: " + e.getMessage());
      throw e;
    }
    finally {
      connection.disconnect();
    }

    if (expectedLength > 0 && read != expectedLength) {
      tmp.delete();
      installLog.setStatus("Incomplete download of " + downloadUrl + ": read " + read + " of " + expectedLength + " bytes");
      throw new UnsupportedOperationException("Incomplete download of " + downloadUrl + ": read " + read + " of " + expectedLength + " bytes");
    }

    String msg = "Download finished, read " + readableFileSize(read) + " in " + (System.currentTimeMillis() - start) + "ms.";
    installLog.log(msg);
    LOG.info(msg);

    if (!tmp.renameTo(targetFile)) {
      tmp.delete();
      installLog.setStatus("Failed to rename download temp file " + tmp.getAbsolutePath() + " to " + targetFile.getAbsolutePath());
      throw new UnsupportedOperationException("Failed to rename download temp file " + tmp.getAbsolutePath() + " to " + targetFile.getAbsolutePath());
    }

    installLog.log("Downloaded file " + targetFile.getAbsolutePath());
    LOG.info("Downloaded file " + targetFile.getAbsolutePath());
    return targetFile;
  }

  private static String readableFileSize(long size) {
    if (size <= 0) return "0";
    final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
    return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
  }
}
