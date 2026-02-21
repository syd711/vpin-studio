package de.mephisto.vpin.connectors.github;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Downloads .vbs script files from a GitHub repository by fetching the master.zip archive.
 * All .vbs files found anywhere in the zip are extracted flat into the target directory,
 * with leading numeric prefixes stripped from their filenames.
 *
 * Designed for https://github.com/jsm174/vpx-standalone-scripts
 */
public class GithubFileDownloader {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final String repoUrl;

  public GithubFileDownloader(String repoUrl) {
    this.repoUrl = repoUrl;
  }

  /**
   * Downloads the repository's master.zip and extracts every .vbs file into {@code targetDir}.
   *
   * @param targetDir directory to extract files into (created if absent)
   * @return list of extracted files
   */
  public List<File> downloadVbsFiles(File targetDir) throws IOException {
    targetDir.mkdirs();

    String zipUrl = buildZipUrl();
    LOG.info("Downloading archive from {}", zipUrl);

    List<File> extracted = new ArrayList<>();

    URL url = new URL(zipUrl);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestProperty("User-Agent", "VPinStudio");
    conn.setConnectTimeout(30_000);
    conn.setReadTimeout(120_000);

    try (ZipInputStream zis = new ZipInputStream(new BufferedInputStream(conn.getInputStream()))) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        if (!entry.isDirectory() && entry.getName().endsWith(".vbs")) {
          String fileName = stripLeadingNumber(new File(entry.getName()).getName());
          File target = new File(targetDir, fileName);

          try (FileOutputStream fos = new FileOutputStream(target)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = zis.read(buf)) != -1) {
              fos.write(buf, 0, n);
            }
          }

          extracted.add(target);
          LOG.info("Extracted {}", target.getAbsolutePath());
        }
        zis.closeEntry();
      }
    }

    LOG.info("Extracted {} .vbs file(s) to {}", extracted.size(), targetDir.getAbsolutePath());
    return extracted;
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private String buildZipUrl() {
    String base = repoUrl.endsWith("/") ? repoUrl.substring(0, repoUrl.length() - 1) : repoUrl;
    return base + "/archive/refs/heads/master.zip";
  }

  /** Removes a leading numeric prefix and its separator from a filename, e.g. {@code 01_foo.vbs} → {@code foo.vbs}. */
  String stripLeadingNumber(String fileName) {
    return fileName; //fileName.replaceFirst("^\\d+[_\\-.]", "");
  }
}
