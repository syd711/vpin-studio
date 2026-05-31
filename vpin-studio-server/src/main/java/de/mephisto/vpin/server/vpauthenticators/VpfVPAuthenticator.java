package de.mephisto.vpin.server.vpauthenticators;

import de.mephisto.vpin.restclient.vpf.VPFSettings;
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

public class VpfVPAuthenticator implements VPAuthenticator {
  private final static Logger LOG = LoggerFactory.getLogger(VpfVPAuthenticator.class);

  private static final String LOGIN_PAGE_URL = "https://www.vpforums.org/index.php?app=core&module=global&section=login";
  private static final String LOGIN_POST_URL  = "https://www.vpforums.org/index.php?app=core&module=global&section=login&do=process";
  private static final String USER_AGENT      = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/148.0.0.0 Safari/537.36";

  private final VPFSettings settings;

  public VpfVPAuthenticator(VPFSettings settings) {
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
      LOG.error("VPF login failed: {}", e.getMessage(), e);
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

    // Step 1: GET login page to establish session cookie and extract auth_key
    HttpResponse<String> getResponse = client.send(
        HttpRequest.newBuilder()
            .uri(URI.create(LOGIN_PAGE_URL))
            .header("User-Agent", USER_AGENT)
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .GET()
            .build(),
        HttpResponse.BodyHandlers.ofString());

    String authKey = extractAuthKey(getResponse.body());
    if (authKey == null) {
      throw new RuntimeException("auth_key not found on VPF login page");
    }

    // Step 2: POST credentials with the extracted auth_key
    String formData = "auth_key=" + URLEncoder.encode(authKey, StandardCharsets.UTF_8)
        + "&referer=" + URLEncoder.encode("https://www.vpforums.org/", StandardCharsets.UTF_8)
        + "&ips_username=" + URLEncoder.encode(settings.getLogin(), StandardCharsets.UTF_8)
        + "&ips_password=" + URLEncoder.encode(settings.getPassword(), StandardCharsets.UTF_8)
        + "&rememberMe=1";

    HttpResponse<String> postResponse = client.send(
        HttpRequest.newBuilder()
            .uri(URI.create(LOGIN_POST_URL))
            .header("User-Agent", USER_AGENT)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header("Origin", "https://www.vpforums.org")
            .header("Referer", "https://www.vpforums.org/")
            .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .POST(HttpRequest.BodyPublishers.ofString(formData))
            .build(),
        HttpResponse.BodyHandlers.ofString());

    // Successful login: IPS sets member_id to the user's numeric ID (non-zero)
    boolean loggedIn = cookieManager.getCookieStore().getCookies().stream()
        .anyMatch(c -> "member_id".equals(c.getName()) && !"0".equals(c.getValue()));

    if (loggedIn) {
      LOG.info("VPF login successful for '{}'", settings.getLogin());
      return null;
    }

    // Extract error message from IPS 3.x error paragraph
    Pattern errorPattern = Pattern.compile("<p[^>]+class=[\"'][^\"']*(?:message|error)[^\"']*[\"'][^>]*>(.*?)</p>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
    Matcher m = errorPattern.matcher(postResponse.body());
    if (m.find()) {
      String msg = m.group(1).replaceAll("<[^>]+>", "").trim();
      if (StringUtils.isNotBlank(msg)) {
        return msg;
      }
    }
    return "Cannot login";
  }

  private String extractAuthKey(String html) {
    // Find the <input> tag with name="auth_key" then pull its value attribute
    Matcher tagMatcher = Pattern.compile("<input[^>]+name=[\"']auth_key[\"'][^>]*>", Pattern.CASE_INSENSITIVE).matcher(html);
    if (tagMatcher.find()) {
      Matcher valMatcher = Pattern.compile("value=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE).matcher(tagMatcher.group());
      if (valMatcher.find()) {
        return valMatcher.group(1);
      }
    }
    return null;
  }
}
