package de.mephisto.vpin.server.frontend.pinballx;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PinballXFtpClientTest {

  private PinballXFtpClient client;

  @BeforeEach
  void setUp() {
    client = new PinballXFtpClient();
  }

  @Test
  void configure_setsHostPortAndRootFolder() {
    client.configure("192.168.1.100", 21, "/media");

    assertEquals("192.168.1.100", client.host);
    assertEquals(21, client.port);
    assertEquals("/media", client.rootfolder);
  }

  @Test
  void configureCredentials_setsUserAndPassword() {
    client.configureCredentials("admin", "secret");

    assertEquals("admin", client.user);
    assertEquals("secret", client.pwd);
  }

  @Test
  void configure_allowsHostOverride() {
    client.configure("host1", 2121, "/root1");
    client.configure("host2", 22, "/root2");

    assertEquals("host2", client.host);
    assertEquals(22, client.port);
    assertEquals("/root2", client.rootfolder);
  }

  @Test
  void testConnection_returnsFalse_whenNotConnected() {
    client.configure("nonexistent.local", 21, "/");
    client.configureCredentials("user", "pass");
    client.usePassiveMode = true;

    // Should not throw; just return false when host is unreachable
    boolean result = client.testConnection();
    assertFalse(result);
  }
}
