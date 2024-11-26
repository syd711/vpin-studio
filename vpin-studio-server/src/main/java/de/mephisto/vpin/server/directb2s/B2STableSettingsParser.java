package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class B2STableSettingsParser extends DefaultHandler {
  private final static Logger LOG = LoggerFactory.getLogger(B2STableSettingsParser.class);
  private final File xmlFile;

  private Map<String, DirectB2STableSettings> cacheDirectB2STableSettings = new ConcurrentHashMap<>(); 

  public B2STableSettingsParser(@NonNull File xmlFile) {
    this.xmlFile = xmlFile;

    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(xmlFile);

      Element root = doc.getDocumentElement();
      root.normalize();

      NodeList list = root.getChildNodes();
      for (int temp = 0; temp < list.getLength(); temp++) {
        Node romElement = list.item(temp);
        if (romElement.getNodeType() == Node.ELEMENT_NODE && romElement.hasChildNodes()) {
          Element element = (Element) romElement;

          DirectB2STableSettings settings = new DirectB2STableSettings();
          settings.setRom(element.getTagName());

          NodeList childNodes = element.getChildNodes();
          for (int i = 0; i < childNodes.getLength(); i++) {
            Node settingsNode = childNodes.item(i);
            if (settingsNode.getNodeType() == Node.ELEMENT_NODE) {
              String name = settingsNode.getNodeName();
              readNode(settings, name, settingsNode);
            }
          }

          cacheDirectB2STableSettings.put(element.getTagName(), settings);
        }
      }
      
      LOG.info("Finished parsing of " + xmlFile.getAbsolutePath());
    } catch (Exception e) {
      String msg = "Failed to parse B2STableSettings file '" + xmlFile.getAbsolutePath() + "': " + e.getMessage();
      LOG.error(msg, e);
    }
  }

  @Nullable
  public DirectB2STableSettings getEntry(String rom) {
    return rom!=null? cacheDirectB2STableSettings.get(rom): null;
  }

  private static void readNode(DirectB2STableSettings settings, String qName, Node node) {
    switch (qName) {
      case "HideGrill": {
        settings.setHideGrill(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "HideB2SDMD": {
        settings.setHideB2SDMD(Integer.parseInt(node.getTextContent().trim()) == 1);
        break;
      }
      case "HideB2SBackglass": {
        settings.setHideB2SBackglass(Integer.parseInt(node.getTextContent().trim()) == 1);
        break;
      }
      case "HideDMD": {
        settings.setHideDMD(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "LampsSkipFrames": {
        settings.setLampsSkipFrames(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "SolenoidsSkipFrames": {
        settings.setSolenoidsSkipFrames(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "GIStringsSkipFrames": {
        settings.setGiStringsSkipFrames(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "LEDsSkipFrames": {
        settings.setLedsSkipFrames(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "UsedLEDType": {
        settings.setUsedLEDType(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "IsGlowBulbOn": {
        settings.setGlowBulbOn(Integer.parseInt(node.getTextContent().trim()) == 1);
        break;
      }
      case "GlowIndex": {
        settings.setGlowIndex(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "StartAsEXE": {
        try {
          String value = node.getTextContent().trim();
          boolean startAsExe = !StringUtils.isEmpty(value) &&Integer.parseInt(value) == 1;
          settings.setStartAsEXE(startAsExe);
        }
        catch (Exception e) {
          //ignore
        }
        break;
      }
      case "StartBackground": {
        // background is a boolean encoded as int so i is on, which is inverse from visiblity settings
        // absence of settings means standard which is the predfined value for this field
        settings.setStartBackground("1".equals(node.getTextContent().trim()) ? 0 : 1);
        break;
      }
      case "FormToFront": {
        settings.setFormToFront(Integer.parseInt(node.getTextContent().trim()) == 1);
        break;
      }

    }
  }
}
