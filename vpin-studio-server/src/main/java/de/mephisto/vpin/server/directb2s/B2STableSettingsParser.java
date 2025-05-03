package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2sConstants;
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

  private DirectB2ServerSettings serverSettings;

  private Map<String, DirectB2STableSettings> cacheDirectB2STableSettings = new ConcurrentHashMap<>(); 

  public B2STableSettingsParser(@NonNull File backglassServerFolder, @NonNull File xmlFile) {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(xmlFile);

      Element root = doc.getDocumentElement();
      root.normalize();

      serverSettings = new DirectB2ServerSettings();
      serverSettings.setBackglassServerFolder(backglassServerFolder.getAbsolutePath());
      serverSettings.setB2STableSettingsFile(xmlFile.getAbsolutePath());

      parse(root, serverSettings, (s, name, value) -> setServerValue(s, name, value));

      NodeList list = root.getChildNodes();
      for (int temp = 0; temp < list.getLength(); temp++) {
        Node romElement = list.item(temp);
        if (romElement.getNodeType() == Node.ELEMENT_NODE && romElement.hasChildNodes()) {
          Element element = (Element) romElement;

          DirectB2STableSettings settings = new DirectB2STableSettings();
          settings.setRom(element.getTagName());
          parse(element, settings, (s, name, value) -> setTableValue(s, name, value));

          cacheDirectB2STableSettings.put(element.getTagName(), settings);
        }
      }
      
      LOG.info("Finished parsing of " + xmlFile.getAbsolutePath());
    } catch (Exception e) {
      String msg = "Failed to parse B2STableSettings file '" + xmlFile.getAbsolutePath() + "': " + e.getMessage();
      LOG.error(msg, e);
    }
  }

  private <T> void parse(Element element, T settings, SettingsSetter<T> setter) {
    NodeList childNodes = element.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node settingsNode = childNodes.item(i);
      if (settingsNode.getNodeType() == Node.ELEMENT_NODE) {
        String name = settingsNode.getNodeName();
        String value = settingsNode.getTextContent().trim();
        setter.setValue(settings, name, value);
      }
    }
  }

  @Nullable
  public DirectB2ServerSettings getSettings() {
    return serverSettings;
  }

  @Nullable
  public DirectB2STableSettings getEntry(String rom) {
    return rom!=null? cacheDirectB2STableSettings.get(rom): null;
  }

  private void setServerValue(DirectB2ServerSettings settings, String qName, String value) {
    switch (qName) {
      case "ArePluginsOn": {
        settings.setPluginsOn(Integer.parseInt(value) == 1);
        break;
      }
      case "DefaultStartMode": {
        int defaultStartMode = DirectB2ServerSettings.EXE_START_MODE;
        try {
          defaultStartMode = Integer.parseInt(value);
        } catch (Exception e) {
          LOG.error("Failed to read start mode (using EXE as default): " + e.getMessage());
        }
        settings.setDefaultStartMode(defaultStartMode);
        break;
      }
      case "ShowStartupError": {
        settings.setShowStartupError(Integer.parseInt(value) == 1);
        break;
      }
      default: {
        setTableValue(settings, qName, value);
      }
    }

  }

  private void setTableValue(DirectB2STableSettings settings, String qName, String value) {
    switch (qName) {
      case "HideGrill": {
        settings.setHideGrill(Integer.parseInt(value));
        break;
      }
      case "HideB2SDMD": {
        settings.setHideB2SDMD(Integer.parseInt(value) == 1);
        break;
      }
      case "HideB2SBackglass": {
        settings.setHideB2SBackglass(Integer.parseInt(value) == 1);
        break;
      }
      case "HideDMD": {
        settings.setHideDMD(Integer.parseInt(value));
        break;
      }
      case "LampsSkipFrames": {
        settings.setLampsSkipFrames(Integer.parseInt(value));
        break;
      }
      case "SolenoidsSkipFrames": {
        settings.setSolenoidsSkipFrames(Integer.parseInt(value));
        break;
      }
      case "GIStringsSkipFrames": {
        settings.setGiStringsSkipFrames(Integer.parseInt(value));
        break;
      }
      case "LEDsSkipFrames": {
        settings.setLedsSkipFrames(Integer.parseInt(value));
        break;
      }
      case "UsedLEDType": {
        settings.setUsedLEDType(Integer.parseInt(value));
        break;
      }
      case "IsGlowBulbOn": {
        settings.setGlowBulbOn(Integer.parseInt(value) == 1);
        break;
      }
      case "GlowIndex": {
        settings.setGlowIndex(Integer.parseInt(value));
        break;
      }
      case "StartAsEXE": {
        try {
          int startAsExe = StringUtils.isEmpty(value) ? 2 : Integer.parseInt(value);
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
        settings.setStartBackground("1".equals(value) ? 0 : 1);
        break;
      }
      case "DisableFuzzyMatching": {
        settings.setDisableFuzzyMatching(Integer.parseInt(value) == 1);
        break;
      }
      case "FormToBack": {
        if (Integer.parseInt(value) == 1) {
          settings.setFormToPosition(DirectB2sConstants.FORM_TO_BACK);
        }
        break;
      }
      case "FormToFront": {
        if (Integer.parseInt(value) == 1) {
          settings.setFormToPosition(DirectB2sConstants.FORM_TO_FRONT);
        }
        break;
      }
    }
  }

  @FunctionalInterface
  private static interface SettingsSetter<T> {
    void setValue(T settings, String qName, String value);
  }
}
