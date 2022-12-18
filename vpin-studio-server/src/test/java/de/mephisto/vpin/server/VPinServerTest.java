package de.mephisto.vpin.server;

import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.BeforeAll;

abstract public class VPinServerTest {

  @BeforeAll
  public static void before() {
    SystemService.RESOURCES = "../resources/";
    SystemService.PINEMHI_FOLDER =  SystemService.RESOURCES + "pinemhi";
  }
}
