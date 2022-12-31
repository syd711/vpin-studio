package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.server.VPinStudioException;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class VPXParserTest {

  @Test
  public void testParser() throws VPinStudioException {
    File pov = new File("src/test/resources/test.pov");
    POV settings = POVParser.parse(pov, -1);
    assertTrue(settings.getBallReflection() > 0);
    assertTrue(settings.getBallTrail() > 0);
    assertTrue(settings.getDetailsLevel() > 0);
    assertTrue(settings.getPhysicsSet() > 0);
    assertTrue(settings.getDetailsLevel() > 0);
    assertTrue(settings.getFpsLimiter() > 0);
    assertTrue(settings.getBallTrailStrength() > 0);
    assertTrue(settings.getIngameAO() > 0);
    assertTrue(settings.getBallReflection() > 0);
    assertTrue(settings.getNightDayLevel() > 0);
    assertTrue(settings.getMusicVolume() > 0);
    assertTrue(settings.getSoundVolume() > 0);
    assertTrue(settings.getScSpReflect() > 0);
    assertTrue(settings.getSsaa() > 0);
  }
}
