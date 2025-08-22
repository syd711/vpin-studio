package de.mephisto.vpin.restclient.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;

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

  @Test
  public void testFileAttributes() throws IOException {
    File wheelIcon;
    //wheelIcon = new File("../testsystem/vPinball/PinUPSystem/POPMedia/Visual Pinball X/Wheel/Jaws.png");
    //wheelIcon = new File("../testsystem/vPinball/PinUPSystem/POPMedia/Visual Pinball X/Wheel/Jaws (Animated).gif");
    wheelIcon = new File("../testsystem/vPinball/PinUPSystem/POPMedia/Visual Pinball X/Wheel/Jaws (Animated).apng");
    //wheelIcon = new File("../testsystem/vPinball/PinUPSystem/POPMedia/Visual Pinball X/Wheel/Atlantis (Bally 1989).apng");

    File notFound = new File("not found file.png");

    String attrName = "test attribute";
    try {
      assertNull(FileUtils.getAttribute(wheelIcon, attrName));
      assertNull(FileUtils.getAttribute(notFound, attrName));

      String value = "testtesttest";
      FileUtils.setAttribute(wheelIcon, attrName, value);
      FileUtils.setAttribute(notFound, attrName, value);

      assertEquals(value, FileUtils.getAttribute(wheelIcon, attrName));
      assertNull(FileUtils.getAttribute(notFound, attrName));
    }
    finally {
      // make sure file attribute is removed  for next test
      FileUtils.removeAttribute(wheelIcon, attrName);
      FileUtils.removeAttribute(notFound, attrName);

      assertNull(FileUtils.getAttribute(wheelIcon, attrName));
      assertNull(FileUtils.getAttribute(notFound, attrName));
    }
  }

}
