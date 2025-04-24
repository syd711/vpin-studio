package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.util.XMLUtil;
import edu.umd.cs.findbugs.annotations.NonNull;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class B2STableSettingsSerializer  {
  private final static Logger LOG = LoggerFactory.getLogger(B2STableSettingsSerializer.class);

  private final static List<String> tableEntries = Arrays.asList("HideGrill", "HideB2SDMD", "HideB2SBackglass", "HideDMD",
      "LampsSkipFrames", "SolenoidsSkipFrames", "GIStringsSkipFrames", "LEDsSkipFrames",
      "UsedLEDType", "IsGlowBulbOn", "GlowIndex", "StartAsEXE", "StartBackground", "FormToFront", "FormToBack", "Animations");

      private final static List<String> serverEntries = Arrays.asList(
    "ArePluginsOn",  "DefaultStartMode", "ShowStartupError", "DisableFuzzyMatching",
        "HideGrill", "HideB2SDMD", "HideDMD", "FormToFront", "FormToBack");

  private final File xmlFile;

  public B2STableSettingsSerializer(@NonNull File xmlFile) {
    this.xmlFile = xmlFile;
  }

  public void serialize(@NonNull DirectB2ServerSettings settings) throws VPinStudioException {
    serialize(settings, null, serverEntries, (s, name) -> getServerValue(s, name));
  }

  public void serialize(@NonNull DirectB2STableSettings settings) throws VPinStudioException {
    serialize(settings, settings.getRom(), tableEntries, (s, name) -> getTableValue(s, name));
  }

  protected <T> void serialize(@NonNull T settings, String rom, List<String> tableEntries, SettingsGetter<T> getter) throws VPinStudioException {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = null;
      try {
        doc = db.parse(xmlFile);
      }
      catch (Exception e) {
        LOG.error("Cannot parse " + xmlFile + ", recreate an empty one.", e);
        doc = db.newDocument();
      }

      Node root = doc.getDocumentElement();
      if (root == null) {
        root = doc.createElement("B2STableSettings");
        doc.appendChild(root);
      }
      // check document is valid 
      else if (!root.getNodeName().equalsIgnoreCase("B2STableSettings")) {
        throw new IOException("The file exists but is not a valid B2STableSettings file. " +
          "Nothing saved to prevent erasing data, please check your file!");
      }
      else {
        root.normalize();
      }

      Node rootNodeByRom = null;
      if (rom != null) {
        rootNodeByRom = findChild(root, rom);
        if (rootNodeByRom == null) {
          rootNodeByRom = doc.createElement(rom);
          root.appendChild(rootNodeByRom);
        }
      }
      // case of server settings
      else {
        rootNodeByRom = root;
      }

     if (rootNodeByRom.getNodeType() == Node.ELEMENT_NODE) {       
        Node insertionPoint = findFirstChildWithChildNodes(rootNodeByRom);
        for (String tableEntry : tableEntries) {
          String newValue = getter.getValue(settings, tableEntry);
          Node settingsNode = findChild(rootNodeByRom, tableEntry);
          if (!StringUtils.isEmpty(newValue)) {
            if (settingsNode==null) {
              settingsNode = doc.createElement(tableEntry);
              rootNodeByRom.insertBefore(settingsNode, insertionPoint);
            }
            if (settingsNode.getNodeType() == Node.ELEMENT_NODE) {
              settingsNode.setTextContent(newValue);
            }
          }
          else {
            if (settingsNode!=null) {
              rootNodeByRom.removeChild(settingsNode);
            }
          }
        }
      }
      XMLUtil.write(xmlFile, doc, true);
      LOG.info("Written " + xmlFile.getAbsolutePath());
    }
    catch (Exception e) {
      String msg = "Failed to write '" + xmlFile.getAbsolutePath() + "': " + e.getMessage();
      throw new VPinStudioException(msg, e);
    }
  }

  private Node findChild(Node node, String tableEntry) {
    NodeList childNodes = node.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node settingsNode = childNodes.item(i);
      if (settingsNode.getNodeName().equals(tableEntry)) {
        return settingsNode;
      }
    }
    return null;
  }

  private Node findFirstChildWithChildNodes(Node node) {
    NodeList childNodes = node.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node settingsNode = childNodes.item(i);
      if (settingsNode.hasChildNodes()) {
        return settingsNode;
      }
    }
    return null;
  }

  private String getServerValue(DirectB2ServerSettings settings, String qName) {
    switch (qName) {
      case "ArePluginsOn": {
        return intValue(settings.isPluginsOn());
      }
      case "DefaultStartMode": {
        return String.valueOf(settings.getDefaultStartMode());
      }
      case "ShowStartupError": {
        return intValue(settings.isShowStartupError());
      }
      default: {
        return getTableValue(settings, qName);
      }
    }
  }

  protected String getTableValue(DirectB2STableSettings settings, String qName) {
    switch (qName) {
      case "HideGrill": {
        return String.valueOf(settings.getHideGrill());
      }
      case "HideB2SDMD": {
        return intValue(settings.isHideB2SDMD());
      }
      case "HideB2SBackglass": {
        return intValue(settings.isHideB2SBackglass());
      }
      case "HideDMD": {
        return String.valueOf(settings.getHideDMD());
      }
      case "LampsSkipFrames": {
        return String.valueOf(settings.getLampsSkipFrames());
      }
      case "SolenoidsSkipFrames": {
        return String.valueOf(settings.getSolenoidsSkipFrames());
      }
      case "GIStringsSkipFrames": {
        return String.valueOf(settings.getGiStringsSkipFrames());
      }
      case "LEDsSkipFrames": {
        return String.valueOf(settings.getLedsSkipFrames());
      }
      case "UsedLEDType": {
        return String.valueOf(settings.getUsedLEDType());
      }
      case "IsGlowBulbOn": {
        return intValue(settings.isGlowBulbOn());
      }
      case "GlowIndex": {
        return String.valueOf(settings.getGlowIndex());
      }
      case "StartAsEXE": {
        Boolean startAsEXE = settings.getStartAsEXE();
        return startAsEXE != null ? intValue(startAsEXE.booleanValue()) : "";
      }
      case "StartBackground": {
        // absence of settings means standard, else a boolean encoded as 0/1n which is invers from visibility (0=visible)
        return (settings.getStartBackground()==2) ? null : settings.getStartBackground()==1 ? "0" : "1";
      }
      case "DisableFuzzyMatching": {
        return intValue(settings.isDisableFuzzyMatching());
      }
      case "FormToFront": {
        return intValue(settings.isFormToFront());
      }
      case "FormToBack": {
        return intValue(settings.isFormToBack());
      }
      case "Animations": {
        return "";
      }
      default : {
        return null;
      }
    }
  }

  protected String intValue(boolean b) {
    return b ? "1" : "0";
  }

    @FunctionalInterface
    private static interface SettingsGetter<T> {
      String getValue(T settings, String qName);
    }
}
