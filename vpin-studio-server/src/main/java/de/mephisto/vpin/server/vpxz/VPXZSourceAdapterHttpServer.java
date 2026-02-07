package de.mephisto.vpin.server.vpxz;

import de.mephisto.vpin.restclient.util.PasswordUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.*;

public class VPXZSourceAdapterHttpServer implements VPXZSourceAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(VPXZSourceAdapterHttpServer.class);

  private final VPXZService VPXZService;
  private final VPXZSource source;
  private final Map<String, VPXZDescriptor> cache = new HashMap<>();

  public VPXZSourceAdapterHttpServer(VPXZService VPXZService, VPXZSource source) {
    this.VPXZService = VPXZService;
    this.source = source;
    disableSslVerification();
  }

  @Override
  public File export(VPXZDescriptor VPXZDescriptor) {
    throw new UnsupportedOperationException("not supported for http");
  }

  public void downloadArchive(VPXZDescriptor descriptor, File target) {
    BufferedInputStream in = null;
    BufferedOutputStream out = null;
    try {
      String location = this.source.getLocation();
      if (!location.endsWith("/")) {
        location += "/";
      }

      String url = location + URLEncoder.encode(descriptor.getFilename(), StandardCharsets.UTF_8).replace("+", "%20");
      LOG.info("Downloading " + url);
      FileOutputStream fout = new FileOutputStream(target);
      HttpURLConnection conn = getConnection(url);
      in = new BufferedInputStream(conn.getInputStream());
      out = new BufferedOutputStream(fout);

      IOUtils.copy(in, out);

      fout.close();
      conn.disconnect();
    } catch (IOException e) {
      LOG.error("Failed to download " + descriptor.getFilename() + ": " + e.getMessage(), e);
      target.delete();
    }
    finally {
      try {
        in.close();

        out.flush();
        out.close();
      } catch (IOException e) {
        //ignore
      }
    }
  }

  public void downloadDescriptor(VPXZDescriptor descriptor, File target) {
    BufferedInputStream in = null;
    BufferedOutputStream out = null;
    try {
      String location = this.source.getLocation();
      if (!location.endsWith("/")) {
        location += "/";
      }

      String url = location + URLEncoder.encode(FilenameUtils.getBaseName(descriptor.getFilename()) + ".json", StandardCharsets.UTF_8).replace("+", "%20");
      LOG.info("Downloading " + url);
      FileOutputStream fout = new FileOutputStream(target);
      HttpURLConnection conn = getConnection(url);
      in = new BufferedInputStream(conn.getInputStream());
      out = new BufferedOutputStream(fout);

      IOUtils.copy(in, out);

      in.close();

      out.flush();
      out.close();

      fout.close();
      conn.disconnect();
    } catch (IOException e) {
      LOG.error("Failed to download descriptor file " + descriptor.getFilename() + ": " + e.getMessage());
    }
  }

  public List<VPXZDescriptor> getVPXZDescriptors() {
//    if (cache.isEmpty()) {
//      String location = this.source.getLocation();
//      if (!location.endsWith("/")) {
//        location += "/";
//      }
//      location += ArchiveUtil.DESCRIPTOR_JSON;
//
//      HttpURLConnection conn = null;
//      try {
//        conn = getConnection(location);
//        LOG.info("Reading " + location);
//        long start = System.currentTimeMillis();
//        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//        StringBuilder jsonBuffer = new StringBuilder();
//        String str;
//        while ((str = in.readLine()) != null) {
//          jsonBuffer.append(str);
//        }
//        in.close();
//
//        String json = jsonBuffer.toString();
//        List<ArchiveDescriptor> archiveDescriptors = ArchiveUtil.readArchiveDescriptors(json, this.getArchiveSource());
//        for (ArchiveDescriptor archiveDescriptor : archiveDescriptors) {
//          if(archiveService.isValidArchiveDescriptor(archiveDescriptor)) {
//            cache.put(archiveDescriptor.getFilename(), archiveDescriptor);
//          }
//        }
//        LOG.info("Reading of " + location + " finshed, took " + (System.currentTimeMillis() - start) + "ms.");
//      } catch (FileNotFoundException e) {
//        LOG.error("No descriptor found for " + location + " (" + e.getMessage() + ")");
//      } catch (Exception e) {
//        LOG.error("Failed to read HTTP URL \"" + location + "\":" + e.getMessage());
//      } finally {
//        if (conn != null) {
//          conn.disconnect();
//        }
//      }
//    }
    return new ArrayList<>(cache.values());
  }

  public VPXZSource getVPXZSource() {
    return source;
  }

  @Override
  public boolean delete(VPXZDescriptor descriptor) {
    throw new UnsupportedOperationException("Delete not supported for HTTP sources.");
  }

  @Override
  public InputStream getVPXMobileInputStream(VPXZDescriptor vpxzDescriptor) throws IOException {
    String location = this.source.getLocation();
    if (!location.endsWith("/")) {
      location += "/";
    }

    String name = vpxzDescriptor.getFilename();
    location += URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");

    HttpURLConnection conn = getConnection(location);
    return new BufferedInputStream(conn.getInputStream());
  }

  private HttpURLConnection getConnection(String location) {
    HttpURLConnection conn = null;
    try {
      String login = getVPXZSource().getLogin();
      String password = PasswordUtil.decrypt(getVPXZSource().getPassword());

      URL url = new URL(location);
      conn = (HttpURLConnection) url.openConnection();

      if (!StringUtils.isEmpty(login) && !StringUtils.isEmpty(password)) {
        String auth = login + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeaderValue = "Basic " + new String(encodedAuth);
        conn.setRequestProperty("Authorization", authHeaderValue);
      }

      conn.setReadTimeout(0);
      conn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
      return conn;
    } catch (IOException e) {
      LOG.error("Failed to read HTTP URL \"" + location + "\":" + e.getMessage());
      try {
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        String str;
        while ((str = in.readLine()) != null) {
          LOG.error("HTTP ERROR: " + str);
        }
        throw e;
      } catch (IOException ex) {
        LOG.error(ex.getMessage(), ex);
      }
    }
    return null;
  }

  /**
   * disable SSL
   */
  private void disableSslVerification() {
    try {
      System.setProperty("javax.net.ssl.trustStore", "clientTrustStore.key");
      System.setProperty("javax.net.ssl.trustStorePassword", "qwerty");

      // Create a trust manager that does not validate certificate chains
      TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
          return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType) {
        }
      }};

      // Install the all-trusting trust manager
      SSLContext sc = SSLContext.getInstance("SSL");
      sc.init(null, trustAllCerts, new java.security.SecureRandom());
      HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

      // Create all-trusting host name verifier
      HostnameVerifier allHostsValid = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
          return true;
        }
      };

      // Install the all-trusting host verifier
      HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    } catch (Exception e) {
      LOG.error("Failed to disable SSL verification: " + e.getMessage(), e);
    }
  }

  @Override
  public void invalidate() {
    cache.clear();
    LOG.info("Invalidated archive source \"" + this.getVPXZSource() + "\"");
  }
}
