package de.mephisto.vpin.server.vpauthenticators;

import de.mephisto.vpin.restclient.vpu.VPUSettings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VpuVPAuthenticator implements VPAuthenticator {
  private final static Logger LOG = LoggerFactory.getLogger(VpuVPAuthenticator.class);

  private static final String LOGIN_PAGE_URL = "https://vpuniverse.com/login/";
  private static final String USER_AGENT     = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36";

  private final VPUSettings settings;

  public VpuVPAuthenticator(VPUSettings settings) {
    this.settings = settings;
  }

  @Override
  public String login() {
    if (StringUtils.isBlank(settings.getLogin())) {
      return "Login cannot be empty";
    }
    try {
      return doLogin();
    }
    catch (Exception e) {
      LOG.error("VPU login failed: {}", e.getMessage(), e);
      return "Login failed: " + e.getMessage();
    }
  }

  private String doLogin() throws Exception {
    CookieManager cookieManager = new CookieManager();
    cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

    HttpClient client = HttpClient.newBuilder()
        .cookieHandler(cookieManager)
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    // Step 1: GET login page to establish session cookie and extract csrfKey
    HttpResponse<String> getResponse = client.send(
        HttpRequest.newBuilder()
            .uri(URI.create(LOGIN_PAGE_URL))
            .header("User-Agent", USER_AGENT)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .GET()
            .build(),
        HttpResponse.BodyHandlers.ofString());

    String csrfKey = extractCsrfKey(getResponse.body());
    if (csrfKey == null) {
      throw new RuntimeException("csrfKey not found on VPU login page");
    }

    // Step 2: POST credentials with the extracted csrfKey
    String formData = "csrfKey=" + URLEncoder.encode(csrfKey, StandardCharsets.UTF_8)
        + "&ref=" + URLEncoder.encode(LOGIN_PAGE_URL, StandardCharsets.UTF_8)
        + "&auth=" + URLEncoder.encode(settings.getLogin(), StandardCharsets.UTF_8)
        + "&password=" + URLEncoder.encode(settings.getPassword(), StandardCharsets.UTF_8)
        + "&remember_me=1"
        + "&_processLogin=usernamepassword";

    HttpResponse<String> postResponse = client.send(
        HttpRequest.newBuilder()
            .uri(URI.create(LOGIN_PAGE_URL))
            .header("User-Agent", USER_AGENT)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Origin", "https://vpuniverse.com")
            .header("Referer", LOGIN_PAGE_URL)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .POST(HttpRequest.BodyPublishers.ofString(formData))
            .build(),
        HttpResponse.BodyHandlers.ofString());

    // Successful login: IPS4 sets ips4_member_id to the user's numeric ID (non-zero)
    boolean loggedIn = cookieManager.getCookieStore().getCookies().stream()
        .anyMatch(c -> "ips4_member_id".equals(c.getName()) && !"0".equals(c.getValue()));

    if (loggedIn) {
      LOG.info("VPU login successful for '{}'", settings.getLogin());
      return null;
    }

    // Extract error message from IPS4 error element
    Pattern errorPattern = Pattern.compile("<div[^>]+class=[\"'][^\"']*ipsMessage_error[^\"']*[\"'][^>]*>(.*?)</div>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    Matcher m = errorPattern.matcher(postResponse.body());
    if (m.find()) {
      String msg = m.group(1).replaceAll("<[^>]+>", "").trim();
      if (StringUtils.isNotBlank(msg)) {
        return msg;
      }
    }
    return "Cannot login";
  }

  private String extractCsrfKey(String html) {
    Matcher tagMatcher = Pattern.compile("<input[^>]+name=[\"']csrfKey[\"'][^>]*>", Pattern.CASE_INSENSITIVE).matcher(html);
    if (tagMatcher.find()) {
      Matcher valMatcher = Pattern.compile("value=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(tagMatcher.group());
      if (valMatcher.find()) {
        return valMatcher.group(1);
      }
    }
    return null;
  }
}
