package de.mephisto.vpin.server.vps;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.htmlunit.html.HtmlDivision;
import org.htmlunit.html.HtmlElement;
import org.htmlunit.html.HtmlPage;
import org.springframework.util.StreamUtils;

import de.mephisto.vpin.restclient.vps.VpsInstallLink;

public interface VpsInstaller {

  /**
   * @return the login error message or null if successful
   */
  String login() throws IOException;

  void browse(String link, BiConsumer<VpsInstallLink, Supplier<InputStream>> consumer) throws IOException;

  default List<VpsInstallLink> getInstallLinks(String url) throws IOException {
    List<VpsInstallLink> links = new ArrayList<>();
    browse(url, (l, s) -> {
      links.add(l);
    });
    return links;
  }

  default void downloadLink(OutputStream out, String link, int order) throws IOException {
    IOException[] exception = new IOException[1];
    // browse and download 
    browse(link, (l, s) -> {
      // identify the chosen file and get associated InputStream
      if (l.getOrder() == order) {
        try (InputStream downloadedContent = s.get()) {
            StreamUtils.copy(downloadedContent, out);
        }
        catch (IOException ioe) {
          exception[0] = ioe;
        }
      }
    });
    if (exception[0] != null) {
      throw exception[0];
    }
  }

  default HtmlElement nextChild(Iterator<HtmlElement> children) {
    return children.hasNext()? children.next(): null;
  }
  default HtmlElement nextChild(Iterator<HtmlElement> children, HtmlPage page) {
    return children.hasNext()? children.next(): new HtmlDivision("div", page, null);
  }
}
