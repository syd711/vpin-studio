package de.mephisto.vpin.server.vpauthenticators;

import de.mephisto.vpin.restclient.vpf.VPFSettings;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.htmlunit.SilentCssErrorHandler;
import org.htmlunit.WebClient;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VpfVPAuthenticator implements VPAuthenticator {
  private final static Logger LOG = LoggerFactory.getLogger(VpfVPAuthenticator.class);

  private final VPFSettings settings;

  public VpfVPAuthenticator(VPFSettings settings) {
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
      if (StringUtils.isBlank(settings.getLogin())) {
        return "Login cannot be empty";
      }

      // VPF runs IPS 3.x — login form is present on the main page as a widget
      HtmlPage loginPage = webClient.getPage("https://www.vpforums.org/index.php?app=core&module=global&section=login");
      LOG.info("VPF login page title: '{}', forms: {}", loginPage.getTitleText(), loginPage.getForms().size());

      // IPS 3.x login form has id="login"
      HtmlForm loginForm = loginPage.getForms().stream()
          .filter(f -> Strings.CI.equals(f.getId(), "login"))
          .findFirst()
          .orElseThrow(() -> new RuntimeException("Login form not found on page: " + loginPage.getTitleText()));

      // IPS 3.x field names
      loginForm.getInputByName("ips_username").setValue(settings.getLogin());
      loginForm.getInputByName("ips_password").setValue(settings.getPassword());

      HtmlElement submitButton = loginForm.getFirstByXPath(".//input[@type='submit']");
      if (submitButton == null) {
        submitButton = loginForm.getFirstByXPath(".//button[@type='submit']");
      }
      if (submitButton == null) {
        throw new RuntimeException("Submit button not found in login form");
      }

      HtmlPage homePage = (HtmlPage) submitButton.click();
      LOG.info("VPF result page title: '{}'", homePage.getTitleText());

      // Successful login redirects away from the sign-in page
      if (Strings.CI.contains(homePage.getTitleText(), "sign in")) {
        DomNode node = homePage.querySelector("p.message.error");
        return node != null ? node.getTextContent().trim() : "Cannot login";
      }
      return null;
    }
    catch (Exception e) {
      LOG.error("VPF login failed: {}", e.getMessage(), e);
      return "Login failed: " + e.getMessage();
    }
  }
}
