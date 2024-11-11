package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class B2SServerSettingsParser extends DefaultHandler {
  private final static Logger LOG = LoggerFactory.getLogger(B2SServerSettingsParser.class);

  private final File backglassServerFolder;

  private final File xmlFile;

  public B2SServerSettingsParser(@NonNull File backglassServerFolder, @NonNull File xmlFile) {
    this.backglassServerFolder = backglassServerFolder;
    this.xmlFile = xmlFile;
  }

  @Nullable
  public DirectB2ServerSettings getSettings() {
    DirectB2ServerSettings settings = new DirectB2ServerSettings();
    settings.setBackglassServerFolder(backglassServerFolder.getAbsolutePath());
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(xmlFile);

      doc.getDocumentElement().normalize();

      NodeList list = doc.getDocumentElement().getChildNodes();
      for (int temp = 0; temp < list.getLength(); temp++) {
        Node romElement = list.item(temp);
        if (romElement.getNodeType() == Node.ELEMENT_NODE) {
          Element element = (Element) romElement;
          String name = element.getNodeName();
          readNode(settings, name, element);
        }
      }

      LOG.info("Finished parsing of " + xmlFile.getAbsolutePath());
    } catch (Exception e) {
      String msg = "Failed to parse B2STableSettings file '" + xmlFile.getAbsolutePath() + "': " + e.getMessage();
      LOG.error(msg, e);
    }
    return settings;
  }

  private static void readNode(DirectB2ServerSettings settings, String qName, Node node) {
    switch (qName) {
      case "ArePluginsOn": {
        settings.setPluginsOn(Integer.parseInt(node.getTextContent().trim()) == 1);
        break;
      }
      case "DefaultStartMode": {
        int defaultStartMode = DirectB2ServerSettings.EXE_START_MODE;
        try {
          defaultStartMode = Integer.parseInt(node.getTextContent().trim());
        } catch (Exception e) {
          LOG.error("Failed to read start mode (using EXE as default): " + e.getMessage());
        }
        settings.setDefaultStartMode(defaultStartMode);
        break;
      }
      case "DisableFuzzyMatching": {
        settings.setDisableFuzzyMatching(Integer.parseInt(node.getTextContent().trim()) == 1);
        break;
      }
//      case "IsLampsStateLogOn": {
//        settings.setLampsStateLogOn(Integer.parseInt(node.getTextContent().trim()) == 1);
//        break;
//      }
//      case "IsSolenoidsStateLogOn": {
//        settings.setSolenoidsStateLogOn(Integer.parseInt(node.getTextContent().trim()) == 1);
//        break;
//      }
//      case "IsGIStringsStateLogOn": {
//        settings.setGiStringsStateLogOn(Integer.parseInt(node.getTextContent().trim()) == 1);
//        break;
//      }
//      case "IsLEDsStateLogOn": {
//        settings.setLedsStateLogOn(Integer.parseInt(node.getTextContent().trim()) == 1);
//        break;
//      }
//      case "IsPaintingLogOn": {
//        settings.setPaintingLogOn(Integer.parseInt(node.getTextContent().trim()) == 1);
//        break;
//      }
//      case "IsStatisticsBackglassOn": {
//        settings.setStatisticsBackglassOn(Integer.parseInt(node.getTextContent().trim()) == 1);
//        break;
//      }
//      case "FormToFront": {
//        settings.setFormToFront(Integer.parseInt(node.getTextContent().trim()) == 1);
//        break;
//      }
      case "ShowStartupError": {
        settings.setShowStartupError(Integer.parseInt(node.getTextContent().trim()) == 1);
        break;
      }
    }
  }
}
