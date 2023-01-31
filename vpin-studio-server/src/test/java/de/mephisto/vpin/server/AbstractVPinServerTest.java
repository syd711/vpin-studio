package de.mephisto.vpin.server;

import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.BeforeAll;

abstract public class AbstractVPinServerTest {

  public final static String TEST_GAME_FILENAME = "Attack from Mars 2.0.1.vpx";

  @BeforeAll
  public static void before() {
    SystemService.RESOURCES = "../resources/";
    SystemService.PINEMHI_FOLDER =  SystemService.RESOURCES + "pinemhi";
  }
}
