package de.mephisto.vpin.server.vpa;

import de.mephisto.vpin.restclient.VpaManifest;
import de.mephisto.vpin.restclient.util.PasswordUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VpaSourceAdapterHttpServer implements VpaSourceAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(VpaSourceAdapterHttpServer.class);

  private final VpaSource source;
  private final Map<String, VpaDescriptor> cache = new HashMap<>();

  public VpaSourceAdapterHttpServer(VpaSource source) {
    this.source = source;
  }

  public List<VpaDescriptor> getVpaDescriptors() {
    if (cache.isEmpty()) {
      String location = this.source.getLocation();
      if (!location.endsWith("/")) {
        location += "/";
      }
      location += "descriptor.json";

      HttpURLConnection conn = null;
      try {
        conn = getConnection(location);
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder jsonBuffer = new StringBuilder();
        String str;
        while ((str = in.readLine()) != null) {
          jsonBuffer.append(str);
        }

        String json = jsonBuffer.toString();
        List<VpaManifest> vpaManifests = VpaUtil.readManifests(json);
        for (VpaManifest manifest : vpaManifests) {
          VpaDescriptor descriptor = new VpaDescriptor(source, manifest, new Date(), manifest.getVpaFilename(), 0);
          cache.put(manifest.getUuid(), descriptor);
        }
      } catch (FileNotFoundException e) {
        LOG.error("No descriptor found for " + location + " (" + e.getMessage() + ")");
      } catch (Exception e) {
        LOG.error("Failed to read HTTP URL \"" + location + "\":" + e.getMessage());
      }
    }
    return new ArrayList<>(cache.values());
  }

  public VpaSource getVpaSource() {
    return source;
  }

  @Override
  public boolean delete(VpaDescriptor descriptor) {
    throw new UnsupportedOperationException("Delete not supported for HTTP sources.");
  }

  @Override
  public InputStream getDescriptorInputStream(VpaDescriptor descriptor) throws IOException {
    String location = this.source.getLocation();
    if (!location.endsWith("/")) {
      location += "/";
    }

    String name = descriptor.getManifest().getVpaFilename();
    location += URLEncoder.encode(name, StandardCharsets.UTF_8).replace("+", "%20");

    HttpURLConnection conn = getConnection(location);
    return new BufferedInputStream(conn.getInputStream());
  }

  private HttpURLConnection getConnection(String location) {
    HttpURLConnection conn = null;
    try {
      String login = getVpaSource().getLogin();
      String password = PasswordUtil.decrypt(getVpaSource().getPassword());

      URL url = new URL(location);
      conn = (HttpURLConnection) url.openConnection();

      if (!StringUtils.isEmpty(login) && !StringUtils.isEmpty(password)) {
        String auth = login + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
        String authHeaderValue = "Basic " + new String(encodedAuth);
        conn.setRequestProperty("Authorization", authHeaderValue);
      }

      conn.setReadTimeout(5000);
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

  @Override
  public void invalidate() {
    cache.clear();
    LOG.info("Invalidated VPA source \"" + this.getVpaSource() + "\"");
  }
}
