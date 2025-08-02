package de.mephisto.vpin.server.vps;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import org.apache.commons.lang3.StringUtils;
import org.htmlunit.SilentCssErrorHandler;
import org.htmlunit.UnexpectedPage;
import org.htmlunit.WebClient;
import org.htmlunit.WebWindow;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;

import de.mephisto.vpin.restclient.vps.VpsInstallLink;
import de.mephisto.vpin.restclient.vpu.VPUSettings;
import org.slf4j.LoggerFactory;

public class VpsInstallerFromVPU implements VpsInstaller {

  private VPUSettings settings;

  public VpsInstallerFromVPU(VPUSettings settings) {
    this.settings = settings;
  }

  @Override
  public String login() throws IOException {
    try (final WebClient webClient = new WebClient()) {
      webClient.setCssErrorHandler(new SilentCssErrorHandler());
      webClient.getOptions().setThrowExceptionOnScriptError(false);
      webClient.getOptions().setJavaScriptEnabled(false);

      return doLogin(webClient);
    }
  }

  private String doLogin(final WebClient webClient) throws IOException {
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

  @Override
  public void browse(String link, BiConsumer<VpsInstallLink, Supplier<InputStream>> consumer) throws IOException {

    try (final WebClient webClient = new WebClient()) {
      webClient.getOptions().setThrowExceptionOnScriptError(false);
      webClient.getOptions().setJavaScriptEnabled(false);

      if (doLogin(webClient) == null) {

        HtmlPage linksPage = webClient.getPage(link + "?do=download");

        List<DomNode> files = linksPage.querySelectorAll(".ipsDataItem");
        int order = 0;
        for (DomNode file : files) {

          Iterator<HtmlElement> children = file.getHtmlElementDescendants().iterator();
          nextChild(children);
            nextChild(children);
              String name = nextChild(children, linksPage).getTextContent();
            String size = nextChild(children, linksPage).getTextContent();
          nextChild(children);
            nextChild(children);
              nextChild(children);
            HtmlAnchor a = (HtmlAnchor) nextChild(children);

          VpsInstallLink l = new VpsInstallLink();
          l.setName(name);
          l.setOrder(order++);
          l.setSize(size);
          l.setUrl(a != null ? a.getHrefAttribute() : null);

          Supplier<InputStream> inputStreamSupplier = a == null? null : () -> {
            try {
              a.click();
              WebWindow window = linksPage.getEnclosingWindow();
              UnexpectedPage enclosedPage = (UnexpectedPage) window.getEnclosedPage();
              return enclosedPage.getInputStream();
            } catch (IOException ioe) {
              return null;
            }
          };

          consumer.accept(l, inputStreamSupplier);
        }
      }
    }
  }
}
