package de.mephisto.vpin.connectors.github;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
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
      if (targetFile.isFile()) {
        installLog.setStatus("targetFolder folder must be a file");
        throw new UnsupportedOperationException("targetFile must be a file");
      }
      if (targetFile.exists() && !targetFile.delete()) {
        installLog.setStatus("Failed to delete " + targetFile.getAbsolutePath());
        throw new UnsupportedOperationException("Failed to delete " + targetFile.getAbsolutePath());
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


    URL url = new URL(downloadUrl);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setReadTimeout(5000);
    connection.setDoOutput(true);
    BufferedInputStream in = new BufferedInputStream(url.openStream());

    File tmp = new File(targetFile.getParentFile(), targetFile.getName() + ".bak");
    if (tmp.exists()) {
      tmp.delete();
    }

    FileOutputStream fileOutputStream = null;
    try {
      fileOutputStream = new FileOutputStream(tmp);
      byte dataBuffer[] = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
        fileOutputStream.write(dataBuffer, 0, bytesRead);
      }
      in.close();
      fileOutputStream.close();
      installLog.log("Download finished, read " + readableFileSize(tmp.length()) + ".");
      LOG.info("Download finished, read " + readableFileSize(tmp.length()) + ".");
    } finally {
      if (fileOutputStream != null) {
        fileOutputStream.close();
      }
    }

    if (!tmp.renameTo(targetFile)) {
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
