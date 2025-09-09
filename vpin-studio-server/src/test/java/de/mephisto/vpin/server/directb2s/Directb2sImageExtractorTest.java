package de.mephisto.vpin.server.directb2s;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.mephisto.vpin.restclient.directb2s.DirectB2SData;


public class Directb2sImageExtractorTest {

  @Test
  public void readDirectb2s() throws Exception {
    File b2sFile = new File("../testsystem/vPinball/VisualPinball/Tables/Jaws.directb2s");
    Assertions.assertTrue(b2sFile.exists());

    DirectB2SDataExtractor extractor = new DirectB2SDataExtractor();
    DirectB2SData data = extractor.extractData(b2sFile, 1, b2sFile.getName());
    Assertions.assertTrue(data.isBackgroundAvailable());

    byte[] imageData = DatatypeConverter.parseBase64Binary(extractor.getBackgroundBase64());
    BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
    Assertions.assertEquals(2011, img.getWidth());
    Assertions.assertEquals(1865, img.getHeight());
  }
}
