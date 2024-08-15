package de.mephisto.vpin.server.vps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.mephisto.vpin.restclient.vps.VpsInstallLink;

public class VpsInstallerTest {

  @Test
  public void testInstallVPF() throws IOException {

    String url = "https://www.vpforums.org/index.php?app=downloads&showfile=16563";

    List<VpsInstallLink> links = VpsInstaller.getInstallLinks(url);
    assertEquals(3, links.size());
    VpsInstallLink link = links.get(0);
    assertEquals("1-2-3 - Davadruix's DMD.png", link.getName());
    assertEquals("672.69KB", link.getSize());
    assertTrue(link.getUrl().startsWith("https://www.vpforums.org/index.php?app=downloads"));

    File f = new File("c:/temp/" + link.getName());
    try (FileOutputStream fout = new FileOutputStream(f)) {
      VpsInstaller.downloadLink(fout, url, 0);
    } 
    //f.delete();
  }

  @Test
  public void testInstallVPU() throws IOException {
    String url = "https://vpuniverse.com/files/file/9442-vikings-static-wheel";

    List<VpsInstallLink> links = VpsInstaller.getInstallLinks(url);
    assertEquals(2, links.size());

    VpsInstallLink link = links.get(0);
    assertEquals("Vikings v1.png", link.getName());
    assertEquals("1.09 MB", link.getSize());
    assertTrue(link.getUrl().startsWith("https://vpuniverse.com/files/file/9442-vikings-static-wheel/?do=download&r=142081"));

    File f = new File("c:/temp/" + link.getName());
    try (FileOutputStream fout = new FileOutputStream(f)) {
      VpsInstaller.downloadLink(fout, url, 0);
    }
    //f.delete();
  }
}
