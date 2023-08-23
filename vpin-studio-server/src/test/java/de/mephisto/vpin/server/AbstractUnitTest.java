package de.mephisto.vpin.server;

import de.mephisto.vpin.server.system.SystemService;

abstract public class AbstractUnitTest {

  static {
    SystemService.RESOURCES = "../resources/";
  }
}
