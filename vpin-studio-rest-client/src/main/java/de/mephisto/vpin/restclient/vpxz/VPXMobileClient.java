package de.mephisto.vpin.restclient.vpxz;

import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.vpxz.models.Tables;
import de.mephisto.vpin.restclient.vpxz.models.Version;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class VPXMobileClient {

  private static final Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final int CHUNK_SIZE = 512 * 1024; // 512 KB per chunk

  private RestClient restClient;

  public VPXMobileClient(@NonNull String host, int port) {
    restClient = RestClient.createInstance(host, port);
    restClient.initRestClientWithTimeoutMs(2000);
  }

  public Version getInfo() {
    return restClient.get("info", Version.class);
  }

  public Tables getTables() {
    return restClient.get("download?q=10.8%2Ftables.json", Tables.class);
  }

  public void createFolder(@NonNull String name) {
    String folder = URLEncoder.encode(name, StandardCharsets.UTF_8);
    restClient.post("folder?q=" + folder, Object.class, Object.class);
  }

  /**
   * Uploads a file to the mobile device in fixed-size chunks using the endpoint:
   * <pre>POST upload?offset={offset}&amp;q={folderName}&amp;file={fileName}&amp;length={totalFileSize}</pre>
   * The raw chunk bytes are sent as the request body.
   *
   * @param file             local file to upload
   * @param folderName       destination folder on the device (e.g. {@code "Attack From Mars"})
   * @param progressListener called after each successful chunk; may be {@code null}
   * @throws IOException if any chunk fails or the file cannot be read
   */
  public void uploadFile(@NonNull File file, @NonNull String folderName, UploadProgressListener progressListener) throws IOException {
    long fileSize = file.length();
    String encodedFolder = URLEncoder.encode(folderName, StandardCharsets.UTF_8);
    String encodedFile = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8);

    LOG.info("Uploading '{}' ({} bytes) to folder '{}'", file.getName(), fileSize, folderName);

    try (FileInputStream fis = new FileInputStream(file)) {
      byte[] buffer = new byte[CHUNK_SIZE];
      long offset = 0;
      int bytesRead;

      while ((bytesRead = fis.read(buffer)) != -1) {
        String path = "upload?offset=" + offset
            + "&q=" + encodedFolder
            + "&file=" + encodedFile
            + "&length=" + fileSize;

        URL url = new URL(restClient.getBaseUrl() + path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setRequestProperty("Content-Length", String.valueOf(bytesRead));
        conn.setConnectTimeout(10_000);
        conn.setReadTimeout(60_000);

        try (OutputStream os = conn.getOutputStream()) {
          os.write(buffer, 0, bytesRead);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode < 200 || responseCode >= 300) {
          throw new IOException("Upload failed at offset " + offset + " with HTTP " + responseCode);
        }

        offset += bytesRead;
        LOG.info("Uploaded {} / {} bytes", offset, fileSize);

        if (progressListener != null) {
          progressListener.onProgress(offset, fileSize);
        }
      }
    }

    LOG.info("Upload complete: '{}'", file.getName());
  }

  @FunctionalInterface
  public interface UploadProgressListener {
    /**
     * @param bytesUploaded cumulative bytes uploaded so far
     * @param totalBytes    total file size in bytes
     */
    void onProgress(long bytesUploaded, long totalBytes);
  }
}
