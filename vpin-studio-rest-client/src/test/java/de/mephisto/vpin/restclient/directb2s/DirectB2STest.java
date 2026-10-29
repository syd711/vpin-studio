package de.mephisto.vpin.restclient.directb2s;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DirectB2STest {

  @Test
  public void testMainVersionIsFirst() {
    DirectB2S b2s = new DirectB2S();
    b2s.addVersion("Twister (1).directb2s");
    b2s.addVersion("Twister.directb2s");

    assertTrue(b2s.isEnabled());
    assertEquals("Twister.directb2s", b2s.getFileName());
    assertEquals(List.of("Twister.directb2s", "Twister (1).directb2s"), b2s.getVersions());
    assertEquals(BackglassFileType.DIRECTB2S, b2s.getFileType());
  }

  @Test
  public void testB2SProMainVersionIsPreferred() {
    DirectB2S b2s = new DirectB2S();
    b2s.addVersion("Twister.directb2s");
    b2s.addVersion("Twister (1).directb2s");
    b2s.addVersion("Twister.B2SPro");

    assertTrue(b2s.isEnabled());
    assertEquals("Twister.B2SPro", b2s.getFileName());
    assertEquals(List.of("Twister.B2SPro", "Twister.directb2s", "Twister (1).directb2s"), b2s.getVersions());
    assertEquals(BackglassFileType.B2SPRO, b2s.getFileType());
  }

  @Test
  public void testB2SProMainVersionStaysFirst() {
    DirectB2S b2s = new DirectB2S();
    b2s.addVersion("Twister.B2SPro");
    b2s.addVersion("Twister.directb2s");

    assertEquals("Twister.B2SPro", b2s.getFileName());
    assertEquals(List.of("Twister.B2SPro", "Twister.directb2s"), b2s.getVersions());
  }

  @Test
  public void testMixedVersionsWithoutMain() {
    DirectB2S b2s = new DirectB2S();
    b2s.addVersion("Twister (1).B2SPro");
    b2s.addVersion("Twister (2).directb2s");

    assertFalse(b2s.isEnabled());
    assertEquals("Twister", b2s.getName());
  }

  @Test
  public void testFileTypes() {
    assertTrue(BackglassFileType.isBackglassFile("Twister.B2SPro"));
    assertTrue(BackglassFileType.isBackglassFile("Twister.b2spro"));
    assertTrue(BackglassFileType.isBackglassFile("Twister.DirectB2S"));
    assertFalse(BackglassFileType.isBackglassFile("Twister.res"));
    assertTrue(BackglassFileType.isB2SPro("sub/Twister (1).B2SPro"));
    assertFalse(BackglassFileType.isB2SPro("Twister.directb2s"));
  }
}
