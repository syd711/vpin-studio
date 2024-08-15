package de.mephisto.vpin.server.vps;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.BrowserVersion;
import org.htmlunit.UnexpectedPage;
import org.htmlunit.WebWindow;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlPage;

import de.mephisto.vpin.restclient.vps.VpsInstallLink;

public class VpsInstallerFromVPU implements VpsInstaller {

  @Override
  public void browse(String link, BiConsumer<VpsInstallLink, Supplier<InputStream>> consumer) throws IOException {

    try (final org.htmlunit.WebClient webClient = new org.htmlunit.WebClient()) {

      webClient.getOptions().setThrowExceptionOnScriptError(false);
      webClient.getOptions().setJavaScriptEnabled(false);

      final HtmlPage loginPage = webClient.getPage("https://vpuniverse.com/login/");

      HtmlForm loginForm = loginPage.getForms().stream()
        .filter(f -> StringUtils.containsIgnoreCase(f.getActionAttribute(), "/login"))
        .findFirst().orElseThrow();

      loginForm.getInputByName("auth").setValue("leprinco");
      loginForm.getInputByName("password").setValue("vpuniverse is very cool");
      loginForm.getButtonByName("_processLogin").click();

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

  private HtmlElement nextChild(Iterator<HtmlElement> children) {
    return children.hasNext()? children.next(): null;
  }
  private HtmlElement nextChild(Iterator<HtmlElement> children, HtmlPage page) {
    return children.hasNext()? children.next(): new HtmlDivision("div", page, null);
  }
}
