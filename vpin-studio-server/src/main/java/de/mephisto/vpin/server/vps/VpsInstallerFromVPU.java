package de.mephisto.vpin.server.vps;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import de.mephisto.vpin.server.vpauthenticators.VpfVPAuthenticator;
import de.mephisto.vpin.server.vpauthenticators.VpuVPAuthenticator;
import org.htmlunit.UnexpectedPage;
import org.htmlunit.WebClient;
import org.htmlunit.WebWindow;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;

import de.mephisto.vpin.restclient.vps.VpsInstallLink;
import de.mephisto.vpin.restclient.vpu.VPUSettings;

public class VpsInstallerFromVPU implements VpsInstaller {

  private VPUSettings settings;

  public VpsInstallerFromVPU(VPUSettings settings) {
    this.settings = settings;
  }

  @Override
  public String login() throws IOException {
    return new VpuVPAuthenticator(this.settings).login();
  }

  @Override
  public void browse(String link, BiConsumer<VpsInstallLink, Supplier<InputStream>> consumer) throws IOException {
//    try (final WebClient webClient = new WebClient()) {
//      webClient.getOptions().setThrowExceptionOnScriptError(false);
//      webClient.getOptions().setJavaScriptEnabled(false);
//
//      if (doLogin(webClient) == null) {
//
//        HtmlPage linksPage = webClient.getPage(link + "?do=download");
//
//        List<DomNode> files = linksPage.querySelectorAll(".ipsDataItem");
//        int order = 0;
//        for (DomNode file : files) {
//
//          Iterator<HtmlElement> children = file.getHtmlElementDescendants().iterator();
//          nextChild(children);
//            nextChild(children);
//              String name = nextChild(children, linksPage).getTextContent();
//            String size = nextChild(children, linksPage).getTextContent();
//          nextChild(children);
//            nextChild(children);
//              nextChild(children);
//            HtmlAnchor a = (HtmlAnchor) nextChild(children);
//
//          VpsInstallLink l = new VpsInstallLink();
//          l.setName(name);
//          l.setOrder(order++);
//          l.setSize(size);
//          l.setUrl(a != null ? a.getHrefAttribute() : null);
//
//          Supplier<InputStream> inputStreamSupplier = a == null? null : () -> {
//            try {
//              a.click();
//              WebWindow window = linksPage.getEnclosingWindow();
//              UnexpectedPage enclosedPage = (UnexpectedPage) window.getEnclosedPage();
//              return enclosedPage.getInputStream();
//            } catch (IOException ioe) {
//              return null;
//            }
//          };
//
//          consumer.accept(l, inputStreamSupplier);
//        }
//      }
//    }
  }
}
