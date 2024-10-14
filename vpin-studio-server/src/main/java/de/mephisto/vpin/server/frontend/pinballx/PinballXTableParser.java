package de.mephisto.vpin.server.frontend.pinballx;

import de.mephisto.vpin.restclient.frontend.Emulator;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.server.frontend.GameEntry;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
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
import java.io.*;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PinballXTableParser extends DefaultHandler {
  private final static Logger LOG = LoggerFactory.getLogger(PinballXTableParser.class);

  /** Parser for dates */
  private final FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");


  @Nullable
  public int addGames(File xmlFile, List<String> games, Map<String, TableDetails> tabledetails, Emulator emu) {
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
            
            String gameName = element.getAttribute("name");
            String gameFileName =  gameName + "." + emu.getGamesExt();

            TableDetails detail = new TableDetails();
            detail.setGameName(gameName);
            detail.setGameFileName(gameFileName);
            // will be overriden by description but in case the tag is absent
            detail.setGameDisplayName(gameName); 
            detail.setEmulatorId(emu.getId());

            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
              Node childNode = childNodes.item(i);
              if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                String name = childNode.getNodeName();
                String content = childNode.getTextContent().trim();
                try {
                  readNode(detail, name, content);
                } catch (Exception e) {
                  LOG.warn("Ignored exception while parsing " + name + " '" + content+ "'' of table '" + gameName + 
                    " for emulator "  + emu.getName() +  "': " + e.getMessage());
                }
              }
            }
            games.add(gameFileName);
            tabledetails.put(PinballXConnector.compose(emu.getId(), gameFileName), detail);
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
  private void readNode(TableDetails detail, String qName, String content) throws ParseException {
    switch (qName) {
      case "description": {
        detail.setGameDisplayName(content);
        break;
      }
      case "rom": {
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
        // EnumUtils does not throw exception but returns null
        detail.setGameType(content);
        break;
      }
      case "enabled": {
        boolean enabled = BooleanUtils.toBoolean(content);
        // cf statuses: STATUS_DISABLED=0, STATUS_NORMAL=1, STATUS_MATURE=2, STATUS_WIP=3
        detail.setStatus(enabled? 1: 0);
        break;
      }
      case "hidedmd": {
        // cf constants in TableDataTabScreensController
        setKeepDisplays(detail, content, "1");
      }
      case "hidetopper": {
        // cf constants in TableDataTabScreensController
        setKeepDisplays(detail, content, "0");
        break;
      }
      case "hidebackglass": {
        // cf constants in TableDataTabScreensController
        setKeepDisplays(detail, content, "2");
        break;
      }
      case "version": {
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
      case "comment": {
        detail.setNotes(content);
        break;
      }
      case "alternateexe": {
        detail.setAltLaunchExe(content);
        break;
      }
      case "dateadded": {
        Date dateAdded = content.equals("1900-01-01 00:00:00") ? null : sdf.parse(content);
        detail.setDateAdded(dateAdded);
        break;
      }
      case "datemodified": {
        Date dateModified = content.equals("1900-01-01 00:00:00") ? null : sdf.parse(content);
        detail.setDateModified(dateModified);
        break;
      }
    }
  }

  private void setKeepDisplays(TableDetails detail, String content, String screen) {
    boolean hide = BooleanUtils.toBoolean(content);
    if (!hide) {
      detail.setKeepDisplays(detail.getKeepDisplays()!=null? detail.getKeepDisplays() + "," + screen: screen);
    }
  }

  //----------------------------------------

  public void writeGames(File pinballXDb, List<GameEntry> games, Map<String, TableDetails> mapTableDetails, Emulator emu) {
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pinballXDb)))) {

      writer.append("<menu>\n");
      for (GameEntry entry : games) {
        TableDetails detail = mapTableDetails.get(PinballXConnector.compose(emu.getId(), entry.getFilename()));
        if (detail!=null) {
          writer.append("  <game name=\"").append(escapeXml(detail.getGameName())).append("\">\n");

          appendValue(writer, "description", detail.getGameDisplayName());
          appendValue(writer, "rom", detail.getRomName());
          appendValue(writer, "manufacturer", detail.getManufacturer());
          appendValue(writer, "year", detail.getGameYear()!=null? detail.getGameYear().toString(): "");
          appendValue(writer, "type", detail.getGameType()!=null? detail.getGameType(): "");
          appendKeepDisplays(writer, "hidedmd", detail.getKeepDisplays(), "1");
          appendKeepDisplays(writer, "hidetopper", detail.getKeepDisplays(), "0");
          appendKeepDisplays(writer, "hidebackglass", detail.getKeepDisplays(), "2");
          appendValue(writer, "enabled", detail.getStatus()!=0);
          appendValue(writer, "rating", detail.getGameRating());
          appendValue(writer, "players", detail.getNumberOfPlayers());
          appendValue(writer, "comment", detail.getNotes());
          appendValue(writer, "alternateexe", detail.getAltLaunchExe());
          appendValue(writer, "theme", detail.getGameTheme());
          appendValue(writer, "author", detail.getAuthor());
          appendValue(writer, "version", detail.getGameVersion());
          appendValue(writer, "IPDBnr", detail.getIPDBNum());
          appendValue(writer, "dateadded", detail.getDateAdded());
          appendValue(writer, "datemodified", detail.getDateModified());

          writer.append("  </game>\n");
        }
      }
      writer.append("</menu>");
    }
    catch (Exception e) {
      LOG.error("Error while writing " + pinballXDb, e);
    }
  }

  private void appendKeepDisplays(BufferedWriter writer, String tag,String keepDisplays, String screen) throws IOException {
    // cf constants in TableDataTabScreensController
    boolean keep = keepDisplays!=null && keepDisplays.contains(screen);
    writer.append("    <" + tag + ">").append(keep? "False": "True" ).append("</" + tag + ">\n");   
  }

  private void appendValue(BufferedWriter writer, String tag, String value) throws IOException {
    if (value != null) {
      writer.append("    <" + tag + ">").append(escapeXml(value)).append("</" + tag + ">\n");
    }
  }  private void appendValue(BufferedWriter writer, String tag, Integer value) throws IOException {
    if (value != null) {
      writer.append("    <" + tag + ">").append(value.toString()).append("</" + tag + ">\n");
    }
  }
  private void appendValue(BufferedWriter writer, String tag, Boolean value) throws IOException {
    if (value != null) {
      writer.append("    <" + tag + ">").append(value? "True": "False").append("</" + tag + ">\n");
    }
  }
  private void appendValue(BufferedWriter writer, String tag, Date value) throws IOException {
    if (value != null) {
      writer.append("    <" + tag + ">").append(sdf.format(value)).append("</" + tag + ">\n");
    }
  }
  /**
   * Specific XML escape utilities (vs StringEscapeUtils) to not escape '
   */
  private String escapeXml(String value) {
    value = value.replace("&", "&amp;");
    value = value.replace("<", "&lt;");
    value = value.replace(">", "&gt;");
    return value;
  }

}
