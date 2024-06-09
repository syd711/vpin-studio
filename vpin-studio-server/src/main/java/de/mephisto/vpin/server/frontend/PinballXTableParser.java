package de.mephisto.vpin.server.frontend;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.helpers.DefaultHandler;

import de.mephisto.vpin.restclient.popper.Emulator;
import de.mephisto.vpin.restclient.popper.GameType;
import de.mephisto.vpin.restclient.popper.TableDetails;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.Nullable;

public class PinballXTableParser extends DefaultHandler {
  private final static Logger LOG = LoggerFactory.getLogger(PinballXTableParser.class);

  private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  @Nullable
  public int addGames(File xmlFile, List<Game> games, Map<Integer, TableDetails> tabledetails, Emulator emu) {
    int gamecount = 0;
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(xmlFile);

      Element root = doc.getDocumentElement();
      root.normalize();

      NodeList list = root.getChildNodes();
      for (int temp = 0; temp < list.getLength(); temp++) {
        Node node = list.item(temp);
        if (node.getNodeType() == Node.ELEMENT_NODE && node.hasChildNodes()) {
          Element element = (Element) node;
          if (StringUtils.equalsIgnoreCase(element.getTagName(), "game")) {
            
            int gameid = emu.getId() * 1000000 + (++gamecount);            
            String gameName = element.getAttribute("name");

            Game game = new Game();
            game.setId(gameid);
            game.setGameName(gameName);
            game.setGameFileName(gameName + "." + emu.getGamesExt());

            File vpxFile = new File(emu.getDirGames(), game.getGameFileName());
            game.setGameFile(vpxFile);

            game.setEmulatorId(emu.getId());

            TableDetails detail = new TableDetails();
            detail.setGameName(gameName);
            detail.setGameFileName(gameName);
            detail.setEmulatorId(emu.getId());

            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
              Node childNode = childNodes.item(i);
              if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                String name = childNode.getNodeName();
                String content = childNode.getTextContent().trim();
                try {
                  readNode(game, detail, name, content);
                } catch (Exception e) {
                  LOG.warn("Ignored exception while parsing " + name + " '" + content+ "'' of table '" + gameName + 
                    " for emulator "  + emu.getName() +  "': " + e.getMessage());
                }
              }
            }
            games.add(game);
            tabledetails.put(gameid, detail);
          }
        }
      }
      LOG.info("Finished parsing of " + xmlFile.getAbsolutePath());
    } catch (Exception e) {
      String msg = "Failed to parse database file '" + xmlFile.getAbsolutePath() + "': " + e.getMessage();
      LOG.error(msg, e);
    }
    return gamecount;
  }


  /**
   <game name="007 Goldeneye (Sega 1996)">
    <description>007 Goldeneye (Sega 1996)</description>
    <rom>gldneye</rom>
    <manufacturer>Sega</manufacturer>
    <year>1996</year>
    <type>SS</type>
    <hidedmd>True</hidedmd>
    <hidetopper>True</hidetopper>
    <hidebackglass>True</hidebackglass>
    <enabled>True</enabled>
    <rating>0</rating>
    <players>6</players>
    <theme>Spies - Licensed theme</theme>
    <author>JPJ/Assassin32/Pingod/UncleReamus/Destruk, The Trout</author>
    <version>v2.0</version>
    <IPDBnr>3792</IPDBnr>
    <dateadded>2023-06-10 19:14:16</dateadded>
    <datemodified>2022-04-30 00:00:46</datemodified>
  </game>
   * @param detail 
   */
  private void readNode(Game game, TableDetails detail, String qName, String content) throws ParseException {
    switch (qName) {
      case "description": {
        game.setGameDisplayName(content);
        detail.setGameDisplayName(content);
        break;
      }
      case "rom": {
        game.setRom(content);
        detail.setRomName(content);
        break;
      }
      case "year":
        if (StringUtils.isNotBlank(content)) { detail.setGameYear(Integer.parseInt(content)); }
        break;
      case "manufacturer": {
        detail.setManufacturer(content);
        break;
      }
      case "type": {
        if (ArrayUtils.contains(GameType.values(), content)) { detail.setGameType(GameType.valueOf(content)); }
        break;
      }
      case "enabled": {
        game.setDisabled(!BooleanUtils.toBoolean(content));
        break;
      }
      case "version": {
        game.setVersion(content);
        detail.setGameVersion(content);
        break;
      }
      case "theme": {
        detail.setGameTheme(content);
        break;
      }
      case "author": {
        detail.setAuthor(content);
        break;
      }
      case "IPDBnr": {
        detail.setIPDBNum(content);
        break;
      }
      case "rating": {
        detail.setGameRating(Integer.parseInt(content));
        break;
      }
      case "players": {
        detail.setNumberOfPlayers(Integer.parseInt(content));
        break;
      }
      case "dateadded": {
        Date dateAdded = sdf.parse(content);
        game.setDateAdded(dateAdded);
        detail.setDateAdded(dateAdded);
        break;
      }
      case "datemodified": {
        Date dateUpdated = sdf.parse(content);
        game.setDateUpdated(dateUpdated);
        break;
      }
    }
  }

}
