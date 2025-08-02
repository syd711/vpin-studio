package de.mephisto.vpin.server.vps;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.*;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;

import de.mephisto.vpin.restclient.vps.VpsInstallLink;
import de.mephisto.vpin.restclient.vpf.VPFSettings;

public class VpsInstallerFromVPF implements VpsInstaller {

  private VPFSettings settings;

  public VpsInstallerFromVPF(VPFSettings settings) {
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

  @Override
  public void browse(String link, BiConsumer<VpsInstallLink, Supplier<InputStream>> consumer) throws IOException {

    try (final WebClient webClient = new WebClient()) {
      webClient.getOptions().setThrowExceptionOnScriptError(false);
      webClient.getOptions().setJavaScriptEnabled(false);

      if (doLogin(webClient) == null) {
        // Access the page of the link and click on the download button
        HtmlPage downloadPage = webClient.getPage(link);
        HtmlAnchor linkBtn = downloadPage.querySelector("a.download_button");
        
        Page page = linkBtn.click();
        if (page instanceof UnexpectedPage) {

          UnexpectedPage enclosedPage = (UnexpectedPage) page;
          String attname = enclosedPage.getWebResponse().getResponseHeaderValue("content-disposition");
          attname = StringUtils.substringBetween(attname, "\"", "\"");

          String contentLength = enclosedPage.getWebResponse().getResponseHeaderValue("content-length");
          Double size = Double.parseDouble(contentLength) / 1024;
          String unit = "KB";
          if (size > 1024) {
            size /= 1024;
            unit = "MB";
          }

          // some link goes though direct download
          VpsInstallLink l = new VpsInstallLink();
          l.setName(attname);
          l.setOrder(0);
          l.setSize(size + unit);
          l.setUrl(linkBtn.getHrefAttribute());

          Supplier<InputStream> inputStreamSupplier = () -> {
            try {
              return enclosedPage.getInputStream();
            } catch (IOException ioe) {
              return null;
            }
          };
          consumer.accept(l, inputStreamSupplier);
          return;
        }
        // else go through agreement page 
        HtmlPage agreePage = (HtmlPage) page;      
        HtmlAnchor agreeBtn = agreePage.querySelector("a#agree_disclaimer");
        if (agreeBtn != null) {
          agreePage = agreeBtn.click();
        }

        // display files
        HtmlPage linksPage = agreePage;
        List<DomNode> files = linksPage.querySelectorAll("#files li");
        int order = 0;
        for (DomNode file : files) {
          Iterator<HtmlElement> children = file.getHtmlElementDescendants().iterator();

          String size = nextChild(children, linksPage).getTextContent().trim();

          HtmlAnchor a = (HtmlAnchor) nextChild(children);
          String url = a != null? a.getHrefAttribute() : null;

          String name = nextChild(children, linksPage).getTextContent().trim();

          VpsInstallLink l = new VpsInstallLink();
          l.setName(name);
          l.setOrder(order++);
          l.setSize(size);
          l.setUrl(url);

          Supplier<InputStream> inputStreamSupplier = a == null ? null : () -> {
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
