package de.mephisto.vpin.server.vps;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StreamUtils;

import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.vps.VpsInstallLink;


public interface VpsInstaller {

  void browse(String link, BiConsumer<VpsInstallLink, Supplier<InputStream>> consumer) throws IOException;


  public static VpsInstaller getInstaller(String link) {
    if (link.contains("vpuniverse.com")) {
      return new VpsInstallerFromVPU();
    }
    else if (link.contains("vpforums.")) {
      return new VpsInstallerFromVPF();
    }
    return null;
  }

  public static List<VpsInstallLink> getInstallLinks(String link) {
    List<VpsInstallLink> links = new ArrayList<>();
    try {
      VpsInstaller installer = getInstaller(link);
      if (installer != null) {
        installer.browse(link, (l, s) -> {
          System.out.println(l.getName() + " (" + l.getSize() +") : " + l.getUrl());
          links.add(l);
        });
      }
    }
    catch (IOException ioe) {
    }
    return links;
  }

  public static void resolveLinks(UploadDescriptor uploadDescriptor) throws IOException {
    // detection that it is a file hosted on a remote source
    String originalFilename = uploadDescriptor.getOriginalUploadFileName();
    if (originalFilename.contains(VpsInstallLink.VPS_INSTALL_LINK_PREFIX)) {

      InputStream in = uploadDescriptor.getFile().getInputStream();
      String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
      String link  = StringUtils.substringBeforeLast(content, "@");
      String order  = StringUtils.substringAfterLast(content, "@");

      try (FileOutputStream fout = new FileOutputStream(uploadDescriptor.getTempFilename())) {
        downloadLink(fout, link, Integer.parseInt(order));
      }
    }
  }

  public static void downloadLink(OutputStream out, String link, int order) throws IOException {
    // browse again and do 
    VpsInstaller installer = getInstaller(link);
    if (installer != null) {
      installer.browse(link, (l, s) -> {
        // identify the chosen file and get associated InputStream
        if (l.getOrder() == order) {
          try (InputStream downloadedContent = s.get()) {
              StreamUtils.copy(downloadedContent, out);
          }
          catch (IOException ioe) {
            // cannot get file
          }
        }
      });
    }
  }
}
