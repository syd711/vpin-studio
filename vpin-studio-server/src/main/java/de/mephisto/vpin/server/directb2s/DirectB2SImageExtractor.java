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
import java.io.*;

public class DirectB2SImageExtractor extends DefaultHandler {
  private final static Logger LOG = LoggerFactory.getLogger(DirectB2SImageExtractor.class);

  private String imageData;

  public DirectB2SImageExtractor() {
  }

  public void extractImage(@NonNull File directB2S, @NonNull File target) throws VPinStudioException {
    FileOutputStream out = null;
    InputStream in = null;
    try {
      if (directB2S.exists()) {
        in = new FileInputStream(directB2S);
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        saxParser.parse(in, this);

        byte[] bytes = DatatypeConverter.parseBase64Binary(imageData);
        out = new FileOutputStream(target);
        IOUtils.write(bytes, out);
        out.close();
      }
    } catch (Exception e) {
      String msg = "Failed to parse directb2s directB2S '" + directB2S.getAbsolutePath() + "': " + e.getMessage();
      LOG.error(msg);
      throw new VPinStudioException(msg, e);
    }
    finally {
      if(in != null) {
        try {
          in.close();
        } catch (IOException e) {
          //ignore
        }
      }

      if(out != null) {
        try {
          out.close();
        } catch (IOException e) {
          //ignore
        }
      }
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
