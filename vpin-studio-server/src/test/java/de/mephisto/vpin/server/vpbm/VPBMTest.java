package de.mephisto.vpin.server.vpbm;

import de.mephisto.vpin.restclient.VpbmHosts;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.archiving.adapters.vpbm.VpbmService;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class VPBMTest extends AbstractVPinServerTest {

  @Autowired
  private VpbmService vpbmService;

  @Test
  public void testGetVersion() {
    String version = vpbmService.getVersion();
    assertTrue(version.startsWith("Version: "));
  }

  @Test
  public void testHostIds() {
    VpbmHosts hostIds = vpbmService.getHostIds();
    assertNotNull(hostIds);
    assertFalse(StringUtils.isEmpty(hostIds.getInternalHostId()));
  }

  @Test
  public void testUpdate() {
    assertFalse(vpbmService.isUpdateAvailable());
  }

//  public static void main(String[] args) throws Exception {
//    List<String> objects = new ArrayList<>();
//    objects.add("D:/workspace/vpin-studio/resources/vpbm/vPinBackupManager.exe");
//    objects.add("-v");
//    ProcessBuilder pb = new ProcessBuilder(objects);
//    pb.directory(new File("D:/workspace/vpin-studio/resources/vpbm/"));
//    pb.redirectErrorStream(true);
//    Process process = pb.start();
//    BufferedReader reader = new BufferedReader(
//      new InputStreamReader(process.getInputStream()));
//    String line;
//    while ((line = reader.readLine()) != null) {
//      System.out.println(line);
//    }
//    reader.close();
//
//
//    process.waitFor();
//  }
}
