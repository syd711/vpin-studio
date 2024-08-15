package de.mephisto.vpin.server.vps;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.htmlunit.Page;
import org.htmlunit.UnexpectedPage;
import org.htmlunit.WebWindow;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlForm;
import org.htmlunit.html.HtmlLink;
import org.htmlunit.html.HtmlPage;

import de.mephisto.vpin.restclient.vps.VpsInstallLink;

public class VpsInstallerFromVPF implements VpsInstaller {

  @Override
  public void browse(String link, BiConsumer<VpsInstallLink, Supplier<InputStream>> consumer) throws IOException {

    try (final org.htmlunit.WebClient webClient = new org.htmlunit.WebClient()) {
      webClient.getOptions().setThrowExceptionOnScriptError(false);
      webClient.getOptions().setJavaScriptEnabled(false);

      // Perfom VPF authentication
      HtmlPage loginPage = webClient.getPage("https://www.vpforums.org/index.php?app=core&module=global&section=login");
      HtmlForm loginForm = loginPage.getForms().stream()
        .filter(f -> StringUtils.equalsIgnoreCase(f.getId(), "login"))
        .findFirst().orElseThrow();
      loginForm.getInputByName("ips_username").setValue("leprinco");
      loginForm.getInputByName("ips_password").setValue("Oliver01");
      DomElement submitBtn = loginPage.querySelector("input.input_submit");
      submitBtn.click();
      
      // Access the page of the link and click on the download button
      HtmlPage downloadPage = webClient.getPage(link);
      HtmlLink linkBtn = downloadPage.querySelector("a.download_button");
      
      Page page = linkBtn.click();
      if (page instanceof UnexpectedPage) {
        // some link goes though direct download
        VpsInstallLink l = new VpsInstallLink();
        //FIXME tmp to try
        l.setName("FIXME TMP");
        l.setOrder(0);
        l.setSize("### GB");
        l.setUrl(linkBtn.getHrefAttribute());

        Supplier<InputStream> inputStreamSupplier = () -> {
          try {
            UnexpectedPage enclosedPage = (UnexpectedPage) page;
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
      DomElement agreeBtn = agreePage.querySelector("a#agree_disclaimer");
      HtmlPage linksPage = agreeBtn.click();

      // display files
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

  private HtmlElement nextChild(Iterator<HtmlElement> children) {
    return children.hasNext()? children.next(): null;
  }
  private HtmlElement nextChild(Iterator<HtmlElement> children, HtmlPage page) {
    return children.hasNext()? children.next(): new HtmlDivision("div", page, null);
  }
}
