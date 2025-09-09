package de.mephisto.vpin.server.directb2s;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.mephisto.vpin.commons.fx.ImageUtil;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2SFrameType;

public class DirectB2SFrameTypeGeneratorTest {

  @Test
  public void testAmbilight() throws IOException {
    doTest(DirectB2SFrameType.AMBILIGHT);
  }

  @Test
  public void testBlurred() throws IOException {
    doTest(DirectB2SFrameType.BLURRED);
  }

  @Test
  public void testMirror() throws IOException {
    doTest(DirectB2SFrameType.MIRROR);
  }

  @Test
  public void testGradient() throws IOException {
    doTest(DirectB2SFrameType.GRADIENT);
  }

  public void doTest(DirectB2SFrameType frameType) throws IOException {
    BufferedImage img = loadDirectB2S("Jaws.directb2s");
    Assertions.assertNotNull(img);

    //File f;
    //f = new File("C:\\temp\\framegenerators\\sources\\Abra Ca Dabra (Gottlieb 1975).directb2s");
    //f = new File("C:\\temp\\framegenerators\\sources\\2001 (Gottlieb 1971).directb2s");
    //img = loadDirectB2S(f);

    int targetW = (int) (img.getHeight() * 16.0 / 9.0);
    int targetH = img.getHeight();


    BufferedImage res = DirectB2SFrameTypeGenerator.generate(img, targetW, targetH, frameType, true);
    Assertions.assertNotNull(res);
    Assertions.assertEquals(img.getHeight(), res.getHeight());

    //File test = new File("C:\\temp\\framegenerators\\test_" + frameType + ".png");
    //ImageIO.write(res, "PNG", test);
  }

  //@Test
  public void extractImage() throws IOException {
    BufferedImage img = loadDirectB2S("Jaws.directb2s");
    Assertions.assertNotNull(img);

    //File f;
    //f = new File("C:\\temp\\framegenerators\\sources\\Abra Ca Dabra (Gottlieb 1975).directb2s");
    //f = new File("C:\\temp\\framegenerators\\sources\\2001 (Gottlieb 1971).directb2s");
    //img = loadDirectB2S(f);

    File test = new File("C:\\temp\\framegenerators\\test.png");
    ImageIO.write(img, "PNG", test);
  }

  @Test
  public void testPerspective() throws IOException {

    BufferedImage img = loadDirectB2S("Jaws.directb2s");
    Assertions.assertNotNull(img);

    //File f;
    //f = new File("C:\\temp\\framegenerators\\sources\\Abra Ca Dabra (Gottlieb 1975).directb2s");
    //f = new File("C:\\temp\\framegenerators\\sources\\2001 (Gottlieb 1971).directb2s");
    //img = loadDirectB2S(f);

    BufferedImage res = ImageUtil.applyPerspective(img, "right", 0.11, 0.2);

    Assertions.assertNotNull(res);
    Assertions.assertEquals(img.getHeight(), res.getHeight());

    //File test = new File("C:\\temp\\framegenerators\\test_Perspective.png");
    //ImageIO.write(res, "PNG", test);
  }


  private BufferedImage loadDirectB2S(String filename) throws IOException {
    File folder = new File("../testsystem/vPinball/VisualPinball/Tables");
    File directB2SFile = new File(folder, filename);
    return loadDirectB2S(directB2SFile);
  }

  private BufferedImage loadDirectB2S(File directB2SFile) throws IOException {
    DirectB2SDataExtractor extractor = new DirectB2SDataExtractor();
    DirectB2SData data = extractor.extractData(directB2SFile, 1, directB2SFile.getName());
    if (data.isBackgroundAvailable()) {
      byte[] imageData = DatatypeConverter.parseBase64Binary(extractor.getBackgroundBase64());
      return ImageIO.read(new ByteArrayInputStream(imageData));
    }
    return null;
  }

}
