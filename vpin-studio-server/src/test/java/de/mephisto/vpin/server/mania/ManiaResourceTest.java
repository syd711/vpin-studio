package de.mephisto.vpin.server.mania;

import de.mephisto.vpin.connectors.mania.model.ManiaAccountRepresentation;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ManiaResourceTest extends AbstractVPinServerTest {

  private final static String cabinetId = "test-client";
  private final static String uuid = "1234567890";

  @Autowired
  protected ManiaService maniaService;

  @Test
  public void testAccounts() throws Exception {
    super.setupSystem();
    maniaService.setCabinetId(cabinetId);

    ManiaAccountRepresentation account = new ManiaAccountRepresentation();
    account.setInitials("AAA");
    account.setCabinetId(cabinetId);
    account.setDisplayName("Mephisto");
    account.setUuid(uuid);

    boolean b = maniaService.deleteAccount();
    assertTrue(b);

    ManiaAccountRepresentation writtenAccount = maniaService.save(account);
    assertNotNull(writtenAccount);

    account.setInitials("BBB");
    writtenAccount = maniaService.save(account);
    assertNotNull(writtenAccount);
    assertEquals(writtenAccount.getInitials(), "BBB");
  }
}
