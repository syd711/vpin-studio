package de.mephisto.vpin.server.vpauthenticators;

import de.mephisto.vpin.restclient.vpu.VPUSettings;
import org.apache.commons.lang3.StringUtils;
import org.htmlunit.SilentCssErrorHandler;
import org.htmlunit.WebClient;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class VpuVPAuthenticator implements VPAuthenticator {
  private final static Logger LOG = LoggerFactory.getLogger(VpuVPAuthenticator.class);

  private final VPUSettings settings;

  public VpuVPAuthenticator(VPUSettings settings) {
    this.settings = settings;
  }

  @Override
  public String login() {
    try (final WebClient webClient = new WebClient()) {
      webClient.setCssErrorHandler(new SilentCssErrorHandler());
      webClient.getOptions().setThrowExceptionOnScriptError(false);
      webClient.getOptions().setJavaScriptEnabled(false);

      return doLogin(webClient);
    }
  }

  private String doLogin(final WebClient webClient) {
    try {
      // don't even try to authenticate if settings are not set
      if (StringUtils.isBlank(settings.getLogin())) {
        return "Login cannot be empty";
      }

      final HtmlPage loginPage = webClient.getPage("https://vpuniverse.com/login/");

      HtmlForm loginForm = loginPage.getForms().stream()
          .filter(f -> StringUtils.containsIgnoreCase(f.getActionAttribute(), "/login"))
          .findFirst().orElseThrow();

      loginForm.getInputByName("auth").setValue(settings.getLogin());
      loginForm.getInputByName("password").setValue(settings.getPassword());
      final HtmlPage homePage = loginForm.getButtonByName("_processLogin").click();

      // check that authentication happens successfully
      String title = homePage.getTitleText();
      if (StringUtils.containsIgnoreCase(title, "sign in")) {
        DomNode node = homePage.querySelector("div.ipsMessage_error");
        return node != null ? node.getTextContent() : "Cannot login";
      }
      return null;
    }
    catch (IOException e) {
      LOG.error("Login failed: {}", e.getMessage(), e);
      return "Login failed: " + e.getMessage();
    }
  }
}
