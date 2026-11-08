package de.mephisto.vpin.server.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SecretCipherTest {

  @Test
  public void testRoundTrip() {
    String enc = SecretCipher.encrypt("s3cret äöü");
    assertTrue(SecretCipher.isEncrypted(enc));
    assertFalse(enc.contains("s3cret"));
    assertEquals("s3cret äöü", SecretCipher.decrypt(enc));
  }

  @Test
  public void testEncryptIsIdempotent() {
    String enc = SecretCipher.encrypt("pwd");
    assertEquals(enc, SecretCipher.encrypt(enc));
  }

  @Test
  public void testBlankAndLegacyValues() {
    assertNull(SecretCipher.encrypt(null));
    assertEquals("", SecretCipher.encrypt(""));
    // legacy plain text values are returned unchanged
    assertEquals("plain", SecretCipher.decrypt("plain"));
    assertNull(SecretCipher.decrypt(null));
  }

  @Test
  public void testUndecryptableValueReturnsNull() {
    assertNull(SecretCipher.decrypt("enc:dpapi:AAAA"));
    assertNull(SecretCipher.decrypt("enc:unknown:AAAA"));
  }
}
