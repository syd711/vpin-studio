package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.restclient.competitions.CompetitionType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CompetitionIdFactoryTest {

  // --- createId ---

  @Test
  void createId_returnsLocalUrl_whenOwner() {
    Competition c = new Competition();
    c.setType("SUBSCRIPTION");

    String id = CompetitionIdFactory.createId(c, true);

    assertTrue(id.startsWith("vps://competition/subscription/local/"));
  }

  @Test
  void createId_returnsRemoteUrl_whenNotOwner() {
    Competition c = new Competition();
    c.setType("WEEKLY");

    String id = CompetitionIdFactory.createId(c, false);

    assertTrue(id.startsWith("vps://competition/weekly/remote/"));
  }

  @Test
  void createId_lowercasesType() {
    Competition c = new Competition();
    c.setType("ISCORED");

    String localId = CompetitionIdFactory.createId(c, true);
    String remoteId = CompetitionIdFactory.createId(c, false);

    assertTrue(localId.contains("/iscored/"));
    assertTrue(remoteId.contains("/iscored/"));
  }

  // --- getCompetitionTypes ---

  @Test
  void getCompetitionTypes_returnsEmpty_forNull() {
    List<CompetitionType> types = CompetitionIdFactory.getCompetitionTypes(null);

    assertTrue(types.isEmpty());
  }

  @Test
  void getCompetitionTypes_returnsEmpty_forEmptyString() {
    List<CompetitionType> types = CompetitionIdFactory.getCompetitionTypes("");

    assertTrue(types.isEmpty());
  }

  @Test
  void getCompetitionTypes_detectsSubscription() {
    List<CompetitionType> types = CompetitionIdFactory.getCompetitionTypes("vps://competition/subscription/local/42");

    assertTrue(types.contains(CompetitionType.SUBSCRIPTION));
  }

  @Test
  void getCompetitionTypes_detectsWeekly() {
    List<CompetitionType> types = CompetitionIdFactory.getCompetitionTypes("vps://competition/weekly/remote/7");

    assertTrue(types.contains(CompetitionType.WEEKLY));
  }

  @Test
  void getCompetitionTypes_detectsMultipleTypes() {
    List<CompetitionType> types = CompetitionIdFactory.getCompetitionTypes("subscription,weekly");

    assertTrue(types.contains(CompetitionType.SUBSCRIPTION));
    assertTrue(types.contains(CompetitionType.WEEKLY));
  }

  @Test
  void getCompetitionTypes_isCaseInsensitive() {
    List<CompetitionType> types = CompetitionIdFactory.getCompetitionTypes("SUBSCRIPTION");

    assertTrue(types.contains(CompetitionType.SUBSCRIPTION));
  }
}
