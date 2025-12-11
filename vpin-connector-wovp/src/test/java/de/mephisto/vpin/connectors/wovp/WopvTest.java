package de.mephisto.vpin.connectors.wovp;

import de.mephisto.vpin.connectors.wovp.models.Challenges;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class WopvTest {

  @Test
  public void testWovp() throws Exception {
    String key = System.getenv("WOVP_KEY");
    if (!StringUtils.isEmpty(key)) {
      Wovp wovp = Wovp.create(key);
      Challenges challenges = wovp.getChallenges();
      assertFalse(challenges.getItems().isEmpty());
    }
  }

  @Test
  public void testWovpSubmit() throws Exception {
    String key = System.getenv("WOVP_KEY");
    if (!StringUtils.isEmpty(key)) {
      Wovp wovp = Wovp.create(key);
      wovp.submitScore(new File("C:\\vPinball\\vpin-dropins\\BLIZZARDOFOZZ_v1.0.2.png"), "0f9c5bfa-679e-4983-932e-9713797691bb", 1, "Studio Test");
    }
  }

  @Test
  public void testApiKey() {
    String key = System.getenv("WOVP_KEY");
    if (!StringUtils.isEmpty(key)) {
      Wovp wovp = Wovp.create(key);
      assertNull(wovp.validateKey());
    }

    Wovp wovp = Wovp.create("bubu");
    assertFalse(wovp.validateKey() == null);
  }
}
