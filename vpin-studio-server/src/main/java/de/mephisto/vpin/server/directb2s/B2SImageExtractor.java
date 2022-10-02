package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.server.GameInfo;
import de.mephisto.vpin.server.VPinServiceException;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileOutputStream;

public class B2SImageExtractor extends DefaultHandler {
  private final static Logger LOG = LoggerFactory.getLogger(B2SImageExtractor.class);

  private String imageData;
  private GameInfo game;

  public B2SImageExtractor(GameInfo game) {
    this.game = game;
  }

  @Nullable
  public File extractImage(@NonNull File file) throws VPinServiceException {
    try {
      if (file.exists()) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(file.getAbsolutePath(), this);

        File target = File.createTempFile(FilenameUtils.getBaseName(file.getName()), ".jpg");
        target.deleteOnExit();

        byte[] bytes = DatatypeConverter.parseBase64Binary(imageData);
        FileOutputStream out = new FileOutputStream(target);
        IOUtils.write(bytes, out);
        out.close();
        return target;
      }
    } catch (Exception e) {
      String msg = "Failed to parse directb2s file '" + file.getAbsolutePath() + "': " + e.getMessage();
      LOG.error(msg, e);
      throw new VPinServiceException(msg, e);
    }
    return null;
  }

  @Override
  public void startElement(String uri, String lName, String qName, Attributes attr) {
    switch (qName) {
      case "BackglassImage": {
        this.imageData = attr.getValue("Value");
        break;
      }
    }
  }
}
