package de.mephisto.vpin.server.client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.server.VPinStudioServer;
import de.mephisto.vpin.server.system.SystemService;

@SpringBootTest
@ActiveProfiles("pinballx")
public class InstallTableTest {

  static {
    SystemService.RESOURCES = "../resources/";
    //Features.MANIA_ENABLED = false;
  }

  @Test
  public void testConnect() {

    //ServerUpdatePreProcessing.execute();

    new SpringApplicationBuilder(VPinStudioServer.class).headless(false).run();

    VPinStudioClient client = new VPinStudioClient("localhost");
    String version = client.getSystemService().getVersion();
    
    System.out.println(version);


  }

}

