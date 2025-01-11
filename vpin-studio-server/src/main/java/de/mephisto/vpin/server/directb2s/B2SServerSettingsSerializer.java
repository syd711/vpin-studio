package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
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

public class B2SServerSettingsSerializer {
  private final static Logger LOG = LoggerFactory.getLogger(B2SServerSettingsSerializer.class);
  private final File xmlFile;

  public B2SServerSettingsSerializer(@NonNull File xmlFile) {
    this.xmlFile = xmlFile;
  }

  public void serialize(@NonNull DirectB2ServerSettings settings) {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(xmlFile);

      doc.getDocumentElement().normalize();

      NodeList list = doc.getDocumentElement().getChildNodes();
      for (int temp = 0; temp < list.getLength(); temp++) {
        Node node = list.item(temp);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          Element element = (Element) node;
          String name = element.getNodeName();
          writeNode(settings, name, element);
        }
      }

      write(xmlFile, doc);
    } catch (Exception e) {
      String msg = "Failed to write '" + xmlFile.getAbsolutePath() + "': " + e.getMessage();
      LOG.error(msg, e);
    }
  }

  private static void write(File povFile, Document doc) throws IOException, TransformerException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    transformerFactory.setAttribute("indent-number", 2);
    DOMSource source = new DOMSource(doc);
    FileWriter writer = new FileWriter(povFile);
    StreamResult result = new StreamResult(writer);
    transformer.transform(source, result);
    writer.close();
    LOG.info("Written B2S server settings: " + povFile.getAbsolutePath());
  }


  private static void writeNode(DirectB2ServerSettings settings, String qName, Node node) {
    switch (qName) {
      case "ArePluginsOn": {
        node.setTextContent(intValue(settings.isPluginsOn()));
        break;
      }
      case "DefaultStartMode": {
        node.setTextContent(String.valueOf(settings.getDefaultStartMode()));
        break;
      }
      case "DisableFuzzyMatching": {
        node.setTextContent(intValue(settings.isDisableFuzzyMatching()));
        break;
      }
      case "HideGrill": {
        node.setTextContent(settings.isHideGrill() ? "1" : "0");
        break;
      }
      case "HideB2sDMD": {
        node.setTextContent(settings.isHideB2SDMD() ? "1" : "0");
        break;
      }
      case "HideDMD": {
        node.setTextContent(settings.isHideDMD() ? "1" : "0");
        break;
      }
//      case "IsLampsStateLogOn": {
//        node.setTextContent(intValue(settings.isLampsStateLogOn()));
//        break;
//      }
//      case "IsSolenoidsStateLogOn": {
//        node.setTextContent(intValue(settings.isSolenoidsStateLogOn()));
//        break;
//      }
//      case "IsGIStringsStateLogOn": {
//        node.setTextContent(intValue(settings.isGiStringsStateLogOn()));
//        break;
//      }
//      case "IsLEDsStateLogOn": {
//        node.setTextContent(intValue(settings.isLedsStateLogOn()));
//        break;
//      }
//      case "IsPaintingLogOn": {
//        node.setTextContent(intValue(settings.isPaintingLogOn()));
//        break;
//      }
//      case "IsStatisticsBackglassOn": {
//        node.setTextContent(intValue(settings.isStatisticsBackglassOn()));
//        break;
//      }
      case "FormToFront": {
        node.setTextContent(intValue(settings.isFormToFront()));
        break;
      }
      case "FormToBack": {
        node.setTextContent(intValue(settings.isFormToBack()));
        break;
      }
      case "ShowStartupError": {
        node.setTextContent(intValue(settings.isShowStartupError()));
        break;
      }
      default: {
//        LOG.info("Skipped serialization of B2S server settings node: " + qName);
      }
    }
  }

  private static String intValue(boolean b) {
    return b ? "1" : "0";
  }

}
