package de.mephisto.vpin.server.vpx;

import de.mephisto.vpin.commons.POV;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.games.Game;
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

public class POVSerializer {
  private final static Logger LOG = LoggerFactory.getLogger(POVSerializer.class);

  public static void serialize(@NonNull POV pov, @NonNull Game game) throws VPinStudioException {
    File povFile = game.getPOVFile();
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
              writeNode(pov, name, settingsNode);
            }
          }
        }
      }

      write(povFile, doc);
    } catch (Exception e) {
      String msg = "Failed to parse pov file '" + povFile.getAbsolutePath() + "': " + e.getMessage();
      LOG.error(msg, e);
      throw new VPinStudioException(msg, e);
    }
  }

  private static void write(File povFile, Document doc) throws IOException, TransformerException {
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    Transformer transformer = transformerFactory.newTransformer();
    DOMSource source = new DOMSource(doc);
    FileWriter writer = new FileWriter(povFile);
    StreamResult result = new StreamResult(writer);
    transformer.transform(source, result);
    LOG.info("Written " + povFile.getAbsolutePath());
  }

  private static void writeNode(POV pov, String qName, Node node) {
    switch (qName) {
      case "SSAA": {
        node.setTextContent(String.valueOf(pov.getSsaa()));
        break;
      }
      case "postprocAA": {
        node.setTextContent(String.valueOf(pov.getPostprocAA()));
        break;
      }
      case "ingameAO": {
        node.setTextContent(String.valueOf(pov.getIngameAO()));
        break;
      }
      case "ScSpReflect": {
        node.setTextContent(String.valueOf(pov.getScSpReflect()));
        break;
      }
      case "FPSLimiter": {
        node.setTextContent(String.valueOf(pov.getFpsLimiter()));
        break;
      }
      case "OverwriteDetailsLevel": {
        node.setTextContent(String.valueOf(pov.getOverwriteDetailsLevel()));
        break;
      }
      case "DetailsLevel": {
        node.setTextContent(String.valueOf(pov.getDetailsLevel()));
        break;
      }
      case "BallReflection": {
        node.setTextContent(String.valueOf(pov.getBallReflection()));
        break;
      }
      case "BallTrail": {
        node.setTextContent(String.valueOf(pov.getBallTrail()));
        break;
      }
      case "BallTrailStrength": {
        node.setTextContent(String.valueOf(pov.getBallTrailStrength()));
        break;
      }
      case "OverwriteNightDay": {
        node.setTextContent(String.valueOf(pov.getOverwriteNightDay()));
        break;
      }
      case "NightDayLevel": {
        node.setTextContent(String.valueOf(pov.getNightDayLevel()));
        break;
      }
      case "GameplayDifficulty": {
        node.setTextContent(String.valueOf(pov.getGameplayDifficulty()));
        break;
      }
      case "PhysicsSet": {
        node.setTextContent(String.valueOf(pov.getPhysicsSet()));
        break;
      }
      case "IncludeFlipperPhysics": {
        node.setTextContent(String.valueOf(pov.getIncludeFlipperPhysics()));
        break;
      }
      case "SoundVolume": {
        node.setTextContent(String.valueOf(pov.getSoundVolume()));
        break;
      }
      case "MusicVolume": {
        node.setTextContent(String.valueOf(pov.getMusicVolume()));
        break;
      }
    }
  }
}
