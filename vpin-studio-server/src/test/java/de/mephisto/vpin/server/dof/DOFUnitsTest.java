package de.mephisto.vpin.server.dof;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DOFUnitsTest {
  private final static String WIZ_TEST = "  LedWiz unit 1: name pins outputs";
  private final static String PINSCAPE_TEST = "  Pinscape unit 1: name pins outputs";

  @Test
  public void testUnitParsing() {
    Unit unit = DOFCommandExecutor.parseUnit(WIZ_TEST);
    assertNotNull(unit);
    unit = DOFCommandExecutor.parseUnit(PINSCAPE_TEST);
    assertNotNull(unit);
  }

}
