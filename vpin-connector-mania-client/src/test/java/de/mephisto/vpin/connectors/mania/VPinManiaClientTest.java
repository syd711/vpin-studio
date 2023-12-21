package de.mephisto.vpin.connectors.mania;

import de.mephisto.vpin.restclient.mania.ManiaAccountRepresentation;
import de.mephisto.vpin.restclient.mania.ManiaTournamentRepresentation;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class VPinManiaClientTest {

  private final static String cabinetId = "test-client";
  private final static String accountUuid = "1234567890";
  private final static String tournamentUuid = "1234567890";

  private VPinManiaClient getClient() {
    return new VPinManiaClient("localhost", "vpin-mania/public/", cabinetId);
//    return new VPinManiaClient("https://vpin-mania.net", "", cabinetId);
  }

  @Test
  public void testAccounts() throws Exception {
    VPinManiaClient client = getClient();

    ManiaAccountRepresentation account = new ManiaAccountRepresentation();
    account.setInitials("AAA");
    account.setCabinetId(cabinetId);
    account.setDisplayName("Mephisto");
    account.setUuid(accountUuid);

    boolean b = client.getAccountClient().deleteAccount();
    assertTrue(b);

    ManiaAccountRepresentation writtenAccount = client.getAccountClient().register(account);
    assertNotNull(writtenAccount);

    account.setInitials("BBB");
    writtenAccount = client.getAccountClient().update(account);
    assertNotNull(writtenAccount);
    assertEquals(writtenAccount.getInitials(), "BBB");

    writtenAccount = client.getAccountClient().getAccount();
    assertNotNull(writtenAccount);
  }

  @Test
  public void testTournaments() throws Exception {
    VPinManiaClient client = getClient();
    ManiaTournamentRepresentation tournament = new ManiaTournamentRepresentation();
    tournament.setDisplayName("test tournament");
    tournament.setStartDate(new Date());
    tournament.setEndDate(new Date());
    tournament.setOwnerUuid(accountUuid);
    tournament.setUuid(tournamentUuid);
    tournament.setTournamentRuleSet("ruleset");
    tournament.setTournamentMode("mode");

    boolean b = client.getTournamentClient().deleteTournament(tournamentUuid);
    assertTrue(b);

    ManiaTournamentRepresentation written = client.getTournamentClient().create(tournament);
    assertNotNull(written);

    tournament.setDisplayName("test tournament2");
    written = client.getTournamentClient().update(tournament);
    assertNotNull(written);
    assertEquals(written.getDisplayName(), "test tournament2");

    written = client.getTournamentClient().getTournament(tournamentUuid);
    assertNotNull(written);
  }
}
