package de.mephisto.vpin.server.directb2s;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;

public class DirectB2SDataUpdater extends DefaultHandler {

  public void updateDmdImage(File _directB2S, String filename, String dmdBase64, boolean backupOrigin) throws Exception {

    File destination = new File(_directB2S.getAbsolutePath() + ".tmp");

    try (FileReader reader = new FileReader(_directB2S);
        FileWriter writer = new FileWriter(destination)) {

      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      XMLReader xmlreader = saxParser.getXMLReader();
      XMLFilterImpl filter = new XMLFilterImpl(xmlreader) {
        /** wether Images tag is present or not */
        private boolean found = false;
        /** as Images tag can be in DirectB2SData/Reels or under DirectB2SData directly, need a depth for differentiation */
        private int depth = 0;

        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
          depth++;
          if (qName.equals("DMDType")) {
            AttributesImpl newAtts = new AttributesImpl(atts);
            setAttribute(newAtts, "Value", dmdBase64 != null ? "4" : "1");
            super.startElement(uri, localName, qName, newAtts);
          }
          else if (qName.equals("DMDImage")) {
              found = true;
              if (dmdBase64 != null) {
                AttributesImpl newAtts = new AttributesImpl(atts);
                setAttribute(newAtts, "Value", dmdBase64);
                setAttribute(newAtts, "FileName", filename);
                super.startElement(uri, localName, qName, newAtts);
              }
              // base64 image is null, the DMDImage tag is skipped, equivalent to remove the image
            }
            else {
              // any other tag, generate as identiy
              super.startElement(uri, localName, qName, atts);
            }
        }

        @Override
        public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
          depth--;

          // skip generation of DMDImage in case it was there but image must be removed
          if (qName.equals("DMDImage") && dmdBase64 == null) {
            return;
          }

          // add missing B2SData just before closing the Images tag in case it was not discovered before
          if ("Images".equals(qName) && depth == 1 && !found && dmdBase64 != null) {
            AttributesImpl newAtts = new AttributesImpl();
            setAttribute(newAtts, "Value", dmdBase64);
            setAttribute(newAtts, "FileName", filename);
            
            super.startElement("", "DMDImage", "DMDImage", newAtts);
            super.endElement("", "DMDImage", "DMDImage");
          }

          super.endElement(namespaceURI, localName, qName);
        }
      };

      Source src = new SAXSource(filter, new InputSource(reader));
      Result res = new StreamResult(writer);
      TransformerFactory.newInstance().newTransformer().transform(src, res);
    }
    catch (Exception e) {
      // error so simply delete temp file that is possibly corrupted
      destination.delete();
      throw e;
    }
    // here everything done, so switch files
    if (backupOrigin) {
      File backup = new File(_directB2S.getAbsolutePath() + ".backup");
      if (backup.exists()) {
        backup.delete();
      }
      if (_directB2S.renameTo(backup)) {
        destination.renameTo(_directB2S);
      }
    }
    else {
      if (_directB2S.delete()) {
        destination.renameTo(_directB2S);
      }      
    }
  }

  private void setAttribute(AttributesImpl newAtts, String name, String value) {
    int idx = newAtts.getIndex(name);
    if (idx >= 0) {
      newAtts.setValue(idx, value);
    }
    else {
      newAtts.addAttribute("", "", name, "String", value);
    }
  }

}
