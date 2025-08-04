package de.mephisto.vpin.server.vpauthenticators;

import de.mephisto.vpin.restclient.vpf.VPFSettings;
import org.apache.commons.lang3.StringUtils;
import org.htmlunit.SilentCssErrorHandler;
import org.htmlunit.WebClient;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
      // don't even try to authenticate if settings are not set
      if (StringUtils.isBlank(settings.getLogin())) {
        return "Login cannot be empty";
      }

      // Perfom VPF authentication
      HtmlPage loginPage = webClient.getPage("https://www.vpforums.org/index.php?app=core&module=global&section=login");
      HtmlForm loginForm = loginPage.getForms().stream()
          .filter(f -> StringUtils.equalsIgnoreCase(f.getId(), "login"))
          .findFirst().orElseThrow();
      loginForm.getInputByName("ips_username").setValue(settings.getLogin());
      loginForm.getInputByName("ips_password").setValue(settings.getPassword());
      DomElement submitBtn = loginPage.querySelector("input.input_submit");
      HtmlPage homePage = submitBtn.click();

      // check authentication happens correctly
      String title = homePage.getTitleText();
      if (StringUtils.containsIgnoreCase(title, "sign in")) {
        DomNode node = homePage.querySelector("p.message.error");
        return node != null ? node.getTextContent().trim() : "Cannot login";
      }
      // login successful, no error
      return null;
    }
    catch (IOException e) {
      LOG.error("Login failed: {}", e.getMessage(), e);
      return "Login failed: " + e.getMessage();
    }
  }
}
