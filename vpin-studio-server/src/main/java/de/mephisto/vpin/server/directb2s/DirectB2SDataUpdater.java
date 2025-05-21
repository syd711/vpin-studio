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
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;

public class DirectB2SDataUpdater extends DefaultHandler {

  public void updateDmdImage(File directB2SFile, String filename, String dmdBase64, boolean backupOrigin) throws Exception {

    XMLFilterImpl filter = new XMLFilterImpl() {
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

    // Now update the backglass
    updateBackglass(directB2SFile, filter, backupOrigin);
  }

  //------------------------------------

  public void upddateScoresDisplayState(File directB2SFile, boolean disable, boolean backupOrigin) throws Exception {
    XMLFilterImpl filter = new XMLFilterImpl() {

      @Override
      public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {

        //<Score Parent="DMD" ID="1" ReelType="Dream7LED8" ReelLitColor="255.0.0" ReelDarkColor="15.15.15" 
        //    Glow="1500" Thickness="1000" Shear="6" Digits="7" Spacing="25" DisplayState="0" 
        //    LocX="58" LocY="568" Width="585" Height="70" />

        if (qName.equals("Score")) {
          AttributesImpl newAtts = new AttributesImpl(atts);
          // activate, so set scores display back 
          if (disable) {
            String displayState = getAttribute(newAtts, "DisplayState");
            // is score ON ? 
            if ("0".equals(displayState)) {
              // backup old state and disable it (STATE=1)
              setAttribute(newAtts, "_DisplayState", displayState);
              setAttribute(newAtts, "DisplayState", "1"); 
            }
          }
          else {
            // get backup score
            String _displayState = getAttribute(newAtts, "_DisplayState");
            if (_displayState != null) {
              // restore old state if was backup
              setAttribute(newAtts, "DisplayState", _displayState);
              removeAttribute(newAtts, "_DisplayState");
            }
          }
          super.startElement(uri, localName, qName, newAtts);
        }
        else {
          // any other tag, generate as identiy
          super.startElement(uri, localName, qName, atts);
        }
      }
    };

    updateBackglass(directB2SFile, filter, backupOrigin);
  }

  //------------------------------------

  protected void updateBackglass(File directB2SFile, XMLFilter filter, boolean backupOrigin) throws Exception {

    File destination = new File(directB2SFile.getAbsolutePath() + ".tmp");

    try (FileReader reader = new FileReader(directB2SFile)) {
      try (FileWriter writer = new FileWriter(destination)) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        XMLReader xmlreader = saxParser.getXMLReader();
        filter.setParent(xmlreader);
        Source src = new SAXSource(filter, new InputSource(reader));
        Result res = new StreamResult(writer);
        TransformerFactory.newInstance().newTransformer().transform(src, res);
      }
    }
    catch (Exception e) {
      // error so simply delete temp file that is possibly corrupted
      destination.delete();
      throw e;
    }
    // here everything done, so switch files
    if (backupOrigin) {
      File backup = new File(directB2SFile.getAbsolutePath() + ".backup");
      if (backup.exists()) {
        backup.delete();
      }
      if (directB2SFile.renameTo(backup)) {
        destination.renameTo(directB2SFile);
      }
    }
    else {
      if (directB2SFile.delete()) {
        destination.renameTo(directB2SFile);
      }      
    }
  }

  private String getAttribute(AttributesImpl newAtts, String name) {
    return getAttribute(newAtts, name, null);
  }
  private String getAttribute(AttributesImpl newAtts, String name, String defaultValue) {
    int idx = newAtts.getIndex(name);
    if (idx >= 0) {
      return newAtts.getValue(idx);
    }
    else {
      return defaultValue;
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

  private void removeAttribute(AttributesImpl newAtts, String name) {
    int idx = newAtts.getIndex(name);
    if (idx >= 0) {
      newAtts.removeAttribute(idx);
    }
  }
}
