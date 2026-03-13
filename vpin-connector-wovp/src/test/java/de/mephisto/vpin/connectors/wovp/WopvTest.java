package de.mephisto.vpin.connectors.wovp;

import de.mephisto.vpin.connectors.wovp.models.Challenge;
import de.mephisto.vpin.connectors.wovp.models.Challenges;
import de.mephisto.vpin.connectors.wovp.models.ScoreBoardItem;
import de.mephisto.vpin.connectors.wovp.models.ScoreSubmitMetadata;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class WopvTest {

  @Test
  public void testWovp() throws Exception {
    String key = System.getenv("WOVP_KEY");
    if (!StringUtils.isEmpty(key)) {
      Wovp wovp = Wovp.create(key);
      Challenges challenges = wovp.getChallenges(true);
      assertFalse(challenges.getItems().isEmpty());

      List<Challenge> items = challenges.getItems();
      for (Challenge item : items) {
        List<ScoreBoardItem> scores = item.getScoreBoard().getItems();
        for (ScoreBoardItem score : scores) {
          if (score.getValues().getParticipantName().contains("Faust")) {
            System.out.println(score.getValues().getParticipantName() + ": " + score.getValues().getScore());
          }
        }
      }

    }
  }

  @Test
  public void testWovpSubmit() throws Exception {
    String key = System.getenv("WOVP_KEY");
    if (!StringUtils.isEmpty(key)) {
      Wovp wovp = Wovp.create(key);
      wovp.submitScore(new File("C:\\vPinball\\vpin-dropins\\BLIZZARDOFOZZ_v1.0.2.png"), "0f9c5bfa-679e-4983-932e-9713797691bb", 1, new ScoreSubmitMetadata());
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
