package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.commons.POV;
import de.mephisto.vpin.server.VPinStudioException;
import edu.umd.cs.findbugs.annotations.NonNull;
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

public class POVParser extends DefaultHandler {
  private final static Logger LOG = LoggerFactory.getLogger(POVParser.class);


  public static POV parse(@NonNull File povFile, int gameId) throws VPinStudioException {
    POV pov = new POV();
    pov.setGameId(gameId);
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(povFile);

      doc.getDocumentElement().normalize();

      NodeList list = doc.getElementsByTagName("customsettings");

      for (int temp = 0; temp < list.getLength(); temp++) {
        Node node = list.item(temp);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          Element element = (Element) node;
          NodeList childNodes = element.getChildNodes();

          for (int i = 0; i < childNodes.getLength(); i++) {
            Node settingsNode = childNodes.item(i);
            if (settingsNode.getNodeType() == Node.ELEMENT_NODE) {
              String name = settingsNode.getNodeName();
              readNode(pov, name, settingsNode);
            }
          }
        }
      }


      list = doc.getElementsByTagName("fullscreen");
      for (int temp = 0; temp < list.getLength(); temp++) {
        Node node = list.item(temp);
        if (node.getNodeType() == Node.ELEMENT_NODE) {
          Element element = (Element) node;
          NodeList childNodes = element.getChildNodes();
          for (int i = 0; i < childNodes.getLength(); i++) {
            Node settingsNode = childNodes.item(i);
            if (settingsNode.getNodeType() == Node.ELEMENT_NODE) {
              String name = settingsNode.getNodeName();
              readNode(pov, name, settingsNode);
            }
          }
        }
      }

      LOG.info("Finished parsing of " + povFile.getAbsolutePath());
    } catch (Exception e) {
      String msg = "Failed to parse pov file '" + povFile.getAbsolutePath() + "': " + e.getMessage();
      LOG.error(msg, e);
      throw new VPinStudioException(msg, e);
    }
    return pov;
  }

  private static void readNode(POV pov, String qName, Node node) {
    switch (qName) {
      case "SSAA": {
        pov.setSsaa(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "postprocAA": {
        pov.setPostprocAA(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "ingameAO": {
        pov.setIngameAO(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "ScSpReflect": {
        pov.setScSpReflect(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "FPSLimiter": {
        pov.setFpsLimiter(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "OverwriteDetailsLevel": {
        pov.setOverwriteDetailsLevel(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "DetailsLevel": {
        pov.setDetailsLevel(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "BallReflection": {
        pov.setBallReflection(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "BallTrail": {
        pov.setBallTrail(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "BallTrailStrength": {
        pov.setBallTrailStrength(Double.parseDouble(node.getTextContent().trim()));
        break;
      }
      case "OverwriteNightDay": {
        pov.setOverwriteNightDay(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "NightDayLevel": {
        pov.setNightDayLevel(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "GameplayDifficulty": {
        pov.setGameplayDifficulty(Double.parseDouble(node.getTextContent().trim()));
        break;
      }
      case "PhysicsSet": {
        pov.setPhysicsSet(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "IncludeFlipperPhysics": {
        pov.setIncludeFlipperPhysics(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "SoundVolume": {
        pov.setSoundVolume(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "MusicVolume": {
        pov.setMusicVolume(Integer.parseInt(node.getTextContent().trim()));
        break;
      }
      case "rotation": {
        pov.setRotationFullscreen((int) Double.parseDouble(node.getTextContent().trim()));
        break;
      }
    }
  }
}
