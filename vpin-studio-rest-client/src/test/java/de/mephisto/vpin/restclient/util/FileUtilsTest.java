package de.mephisto.vpin.restclient.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileUtilsTest {

  @Test
  public void testEqualsUniqueFile() {
    assertTrue(FileUtils.equalsUniqueFile("acdc (Stern 2012).directb2s", "acdc (Stern 2012).directb2s"));
    assertFalse(FileUtils.equalsUniqueFile("acdc (Stern 2012).directb2s", "Spectrum (Bammy 1982).directb2s"));

    assertTrue(FileUtils.equalsUniqueFile("Ace Of Speed.directb2s", "Ace Of Speed (01).directb2s"));
    assertTrue(FileUtils.equalsUniqueFile("Ace Of Speed (02).directb2s", "Ace Of Speed (01).directb2s"));
    assertFalse(FileUtils.equalsUniqueFile("Ace Of (01) Speed.directb2s", "Ace Of Speed (01).directb2s"));

    assertTrue(FileUtils.equalsUniqueFile("Ace Of Speed (1982).directb2s", "Ace Of Speed (1982).directb2s"));
    assertFalse(FileUtils.equalsUniqueFile("Ace Of Speed (1982).directb2s", "Ace Of Speed (1983).directb2s"));
    assertFalse(FileUtils.equalsUniqueFile("Ace Of Speed (Bally 1982).directb2s", "Ace Of Speed (1982).directb2s"));

    assertTrue(FileUtils.equalsUniqueFile("Ace Of Speed/Ace Of Speed.directb2s", "Ace Of Speed/Ace Of Speed (01).directb2s"));
    assertFalse(FileUtils.equalsUniqueFile("Ace Of Speed/Ace Of Speed.directb2s", "AceOfSpeed/Ace Of Speed (01).directb2s"));
  }

  @Test
  public void testFromUniqueFile() {
    assertEquals("AC-DC (Stern 2012).directb2s", FileUtils.fromUniqueFile("AC-DC (Stern 2012).directb2s"));
    assertNotEquals("AC-DC (Stern 2012).directb2s", FileUtils.fromUniqueFile("Spectrum (Bammy 1982).directb2s"));

    assertEquals("Ace Of Speed.directb2s", FileUtils.fromUniqueFile("Ace Of Speed (01).directb2s"));
    assertEquals("Ace Of Speed (2019).vpx", FileUtils.fromUniqueFile("Ace Of Speed (2019) (01).vpx"));
    assertEquals("Ace Of Speed (Original 2019).vpx", FileUtils.fromUniqueFile("Ace Of Speed (Original 2019) (01).vpx"));

    assertEquals("Ace Of Speed/Ace Of Speed.directb2s", FileUtils.fromUniqueFile("Ace Of Speed/Ace Of Speed (01).directb2s"));
    assertEquals("Ace Of Speed/Ace Of Speed (Original 2019).png", FileUtils.fromUniqueFile("Ace Of Speed/Ace Of Speed (Original 2019) (05).png"));
  }

  @Test
  public void testBaseUniqueFile() {
    assertEquals("AC-DC (Stern 2012)", FileUtils.baseUniqueFile("AC-DC (Stern 2012).directb2s"));
    assertNotEquals("AC-DC (Stern 2012)", FileUtils.baseUniqueFile("Spectrum (Bammy 1982).directb2s"));

    assertEquals("Ace Of Speed", FileUtils.baseUniqueFile("Ace Of Speed (01).directb2s"));
    assertEquals("Ace Of Speed (2019)", FileUtils.baseUniqueFile("Ace Of Speed (2019) (01).vpx"));
    assertEquals("Ace Of Speed (Original 2019)", FileUtils.baseUniqueFile("Ace Of Speed (Original 2019) (01).vpx"));

    assertEquals("Ace Of Speed/Ace Of Speed", FileUtils.baseUniqueFile("Ace Of Speed/Ace Of Speed (01).directb2s"));
    assertEquals("Ace Of Speed/Ace Of Speed (Original 2019)", FileUtils.baseUniqueFile("Ace Of Speed/Ace Of Speed (Original 2019) (05).png"));
  }

}
