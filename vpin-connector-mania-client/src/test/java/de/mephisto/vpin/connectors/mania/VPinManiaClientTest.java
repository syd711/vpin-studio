package de.mephisto.vpin.connectors.mania;

import de.mephisto.vpin.restclient.mania.ManiaAccountRepresentation;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class VPinManiaClientTest {


  @Test
  public void testPost() throws Exception {
    VPinManiaClient client = new VPinManiaClient("localhost", "vpin-mania/public/");

    ManiaAccountRepresentation account = new ManiaAccountRepresentation();
    account.setInitials("AAA");
    account.setCabinetId("bubu");
    account.setDisplayName("Mephisto");
    account.setUuid(UUID.randomUUID().toString());
    client.getAccountClient().save(account);
  }

  @Test
  public void testGet() {
    VPinManiaClient client = new VPinManiaClient("localhost", "vpin-mania/public/");

    ManiaAccountRepresentation account = client.getAccountClient().getAccount("cadssdf");
    System.out.println(account);
  }
}
