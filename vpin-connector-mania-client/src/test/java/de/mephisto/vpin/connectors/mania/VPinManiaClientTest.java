package de.mephisto.vpin.connectors.mania;

import de.mephisto.vpin.restclient.mania.ManiaAccountRepresentation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class VPinManiaClientTest {

  private final static String cabinetId = "test-client";
  private final static String uuid = "1234567890";

  @Test
  public void testAccounts() throws Exception {
    VPinManiaClient client = new VPinManiaClient("localhost", "vpin-mania/public/", cabinetId);

    ManiaAccountRepresentation account = new ManiaAccountRepresentation();
    account.setInitials("AAA");
    account.setCabinetId(cabinetId);
    account.setDisplayName("Mephisto");
    account.setUuid(uuid);

    boolean b = client.getAccountClient().deleteAccount();
    assertTrue(b);

    ManiaAccountRepresentation writtenAccount = client.getAccountClient().register(account);
    assertNotNull(writtenAccount);

    account.setInitials("BBB");
    writtenAccount = client.getAccountClient().update(account);
    assertNotNull(writtenAccount);
    assertEquals(writtenAccount.getInitials(), "BBB");
  }
}
