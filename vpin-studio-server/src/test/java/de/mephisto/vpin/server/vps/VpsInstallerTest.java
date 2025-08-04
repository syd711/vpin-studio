package de.mephisto.vpin.server.vps;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.junit.Ignore;

import de.mephisto.vpin.restclient.vps.VpsInstallLink;
import de.mephisto.vpin.restclient.vpf.VPFSettings;
import de.mephisto.vpin.restclient.vpu.VPUSettings;

public class VpsInstallerTest {

  @Ignore
  public void testInstallVPF() throws IOException {

    String url = "https://www.vpforums.org/index.php?app=downloads&showfile=16563";

    VPFSettings settings = new VPFSettings();
    settings.setLogin("change me");
    settings.setPassword("change me");
    VpsInstaller installer = new VpsInstallerFromVPF(settings);

    // check login fails for wrong password
    assertNotNull(installer.login());

    //FIXME change here
    settings.setLogin("####");
    settings.setPassword("####");

    List<VpsInstallLink> links = installer.getInstallLinks(url);

    assertEquals(3, links.size());
    VpsInstallLink link = links.get(0);
    assertEquals("1-2-3 - Davadruix's DMD.png", link.getName());
    assertEquals("672.69KB", link.getSize());
    assertTrue(link.getUrl().startsWith("https://www.vpforums.org/index.php?app=downloads"));

    File f = new File("c:/temp/" + link.getName());
    try (FileOutputStream fout = new FileOutputStream(f)) {
      installer.downloadLink(fout, url, 0);
      assertTrue(Files.size(f.toPath()) > 0);
    } 
    finally {
      f.delete();
    }
  }

  @Ignore
  public void testInstallVPU() throws IOException {
    String url = "https://vpuniverse.com/files/file/9442-vikings-static-wheel";

    VPUSettings settings = new VPUSettings();
    settings.setLogin("change me");
    settings.setPassword("change me");
    VpsInstaller installer = new VpsInstallerFromVPU(settings);

    // check login fails for wrong password
    assertNotNull(installer.login());

    //FIXME change here
    settings.setLogin("####");
    settings.setPassword("####");

    List<VpsInstallLink> links = installer.getInstallLinks(url);
    assertEquals(2, links.size());

    VpsInstallLink link = links.get(0);
    assertEquals("Vikings v1.png", link.getName());
    assertEquals("1.09 MB", link.getSize());
    assertTrue(link.getUrl().startsWith("https://vpuniverse.com/files/file/9442-vikings-static-wheel/?do=download&r=142081"));

    File f = new File("c:/temp/" + link.getName());
    try (FileOutputStream fout = new FileOutputStream(f)) {
      installer.downloadLink(fout, url, 0);
      assertTrue(Files.size(f.toPath()) > 0);
    }
    finally {
      f.delete();
    }
  }
}
