package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.server.VPinStudioException;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class B2STableSettingsSerializer {
  private final static Logger LOG = LoggerFactory.getLogger(B2STableSettingsSerializer.class);
  private final static List<String> tableEntries = Arrays.asList("HideGrill", "HideB2SDMD", "HideB2SBackglass", "HideDMD",
      "SolenoidsSkipFrames", "GIStringsSkipFrames", "LEDsSkipFrames",
      "UsedLEDType", "IsGlowBulbOn", "GlowIndex", "StartAsEXE", "StartBackground", "FormToFront", "Animations");
  private final File xmlFile;

  public B2STableSettingsSerializer(@NonNull File xmlFile) {
    this.xmlFile = xmlFile;
  }

  public void serialize(@NonNull DirectB2STableSettings settings) throws VPinStudioException {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(xmlFile);

      doc.getDocumentElement().normalize();

      NodeList list = doc.getElementsByTagName(settings.getRom());
      Node rootNodeByRom = null;
      if (list.getLength() == 0) {
        rootNodeByRom = doc.createElement(settings.getRom());
        doc.getDocumentElement().appendChild(rootNodeByRom);
        for (String tableEntry : tableEntries) {
          Element child = doc.createElement(tableEntry);
          rootNodeByRom.appendChild(child);
        }
      }
      else {
        //check if all children are present
        rootNodeByRom = list.item(0);
        for (String tableEntry : tableEntries) {
          if (!isChildOf(rootNodeByRom.getChildNodes(), tableEntry)) {
            Element child = doc.createElement(tableEntry);
            rootNodeByRom.appendChild(child);
          }
        }
      }


      if (rootNodeByRom.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) rootNodeByRom;
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
          Node settingsNode = childNodes.item(i);
          if (settingsNode.getNodeType() == Node.ELEMENT_NODE) {
            String name = settingsNode.getNodeName();
            writeNode(settings, name, settingsNode);
          }
        }
      }

      write(xmlFile, doc);
    }
    catch (Exception e) {
      String msg = "Failed to write '" + xmlFile.getAbsolutePath() + "': " + e.getMessage();
      throw new VPinStudioException(msg, e);
    }
  }

  private boolean isChildOf(NodeList childNodes, String tableEntry) {
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node settingsNode = childNodes.item(i);
      if (settingsNode.getNodeName().equals(tableEntry)) {
        return true;
      }
    }
    return false;
  }

  private static void write(File povFile, Document doc) throws IOException, TransformerException {
    FileWriter writer = null;
    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformerFactory.setAttribute("indent-number", 2);
      DOMSource source = new DOMSource(doc);
      writer = new FileWriter(povFile);
      StreamResult result = new StreamResult(writer);
      transformer.transform(source, result);
      LOG.info("Written " + povFile.getAbsolutePath());
    }
    finally {
      if (writer != null) {
        writer.close();
      }
    }
  }


  private static void writeNode(DirectB2STableSettings settings, String qName, Node node) {
    switch (qName) {
      case "HideGrill": {
        node.setTextContent(String.valueOf(settings.getHideGrill()));
        break;
      }
      case "HideB2SDMD": {
        node.setTextContent(intValue(settings.isHideB2SDMD()));
        break;
      }
      case "HideB2SBackglass": {
        node.setTextContent(intValue(settings.isHideB2SBackglass()));
        break;
      }
      case "HideDMD": {
        node.setTextContent(String.valueOf(settings.getHideDMD()));
        break;
      }
      case "LampsSkipFrames": {
        node.setTextContent(String.valueOf(settings.getLampsSkipFrames()));
        break;
      }
      case "SolenoidsSkipFrames": {
        node.setTextContent(String.valueOf(settings.getSolenoidsSkipFrames()));
        break;
      }
      case "GIStringsSkipFrames": {
        node.setTextContent(String.valueOf(settings.getGiStringsSkipFrames()));
        break;
      }
      case "LEDsSkipFrames": {
        node.setTextContent(String.valueOf(settings.getLedsSkipFrames()));
        break;
      }
      case "UsedLEDType": {
        node.setTextContent(String.valueOf(settings.getUsedLEDType()));
        break;
      }
      case "IsGlowBulbOn": {
        node.setTextContent(intValue(settings.isGlowBulbOn()));
        break;
      }
      case "GlowIndex": {
        node.setTextContent(String.valueOf(settings.getGlowIndex()));
        break;
      }
      case "StartAsEXE": {
        node.setTextContent(intValue(settings.isStartAsEXE()));
        break;
      }
      case "StartBackground": {
        node.setTextContent(intValue(settings.isStartBackground()));
        break;
      }
      case "FormToFront": {
        node.setTextContent(intValue(settings.isFormToFront()));
        break;
      }
    }
  }

  private static String intValue(boolean b) {
    return b ? "1" : "0";
  }

}
