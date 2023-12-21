package de.mephisto.vpin.connectors.mania;

import de.mephisto.vpin.restclient.mania.ManiaAccountRepresentation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class VPinManiaClientTest {

  private final static String cabinetId = "test-client";

  @Test
  public void testPost() throws Exception {
    VPinManiaClient client = new VPinManiaClient("localhost", "vpin-mania/public/", cabinetId);

    ManiaAccountRepresentation account = new ManiaAccountRepresentation();
    account.setInitials("AAA");
    account.setCabinetId("bubu");
    account.setDisplayName("Mephisto");
    account.setUuid(UUID.randomUUID().toString());
    client.getAccountClient().update(account);
  }

  @Test
  public void testGet() {
    VPinManiaClient client = new VPinManiaClient("localhost", "vpin-mania/public/", cabinetId);

    ManiaAccountRepresentation account = client.getAccountClient().getAccount("cadssdf");
    assertNotNull(account);
  }
}
