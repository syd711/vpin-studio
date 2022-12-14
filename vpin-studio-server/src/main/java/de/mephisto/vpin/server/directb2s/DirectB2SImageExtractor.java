package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.server.VPinStudioException;
import edu.umd.cs.findbugs.annotations.NonNull;
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

public class DirectB2SImageExtractor extends DefaultHandler {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SImageExtractor.class);

  private String imageData;

  public DirectB2SImageExtractor() {
  }

  public void extractImage(@NonNull File directB2S, @NonNull File target) throws VPinStudioException {
    try {
      if (directB2S.exists()) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(directB2S.getAbsolutePath(), this);

        byte[] bytes = DatatypeConverter.parseBase64Binary(imageData);
        FileOutputStream out = new FileOutputStream(target);
        IOUtils.write(bytes, out);
        out.close();
      }
    } catch (Exception e) {
      String msg = "Failed to parse directb2s directB2S '" + directB2S.getAbsolutePath() + "': " + e.getMessage();
      LOG.error(msg, e);
      throw new VPinStudioException(msg, e);
    }
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
