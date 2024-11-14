package de.mephisto.vpin.server.frontend;

import de.mephisto.vpin.restclient.alx.TableAlxEntry;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.frontend.pinballx.PinballXConnector;
import de.mephisto.vpin.server.frontend.pinbally.PinballYStatisticsParser;
import de.mephisto.vpin.server.games.Game;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class FrontendMigrationTest extends AbstractVPinServerTest {

  @Autowired
  private PinballXConnector pbxConnector;

  @Test
  public void testMigrateStats() {
  
    pbxConnector.initializeConnector();
    pbxConnector.getEmulators();

    List<TableAlxEntry> stats = pbxConnector.getAlxData();

    PinballYStatisticsParser parser = new PinballYStatisticsParser(pbxConnector);
    for (TableAlxEntry stat : stats) {
      Game g = pbxConnector.getGame(stat.getGameId());
      parser.writeStat(g, stat);
    }  
  }
}
