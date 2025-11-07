package de.mephisto.vpin.server.frontend.pinballx;

import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.frontend.GameEntry;
import de.mephisto.vpin.server.games.GameEmulator;
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
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PinballXTableParser extends DefaultHandler {
  private final static Logger LOG = LoggerFactory.getLogger(PinballXTableParser.class);

  /** Parser for dates */
  protected final FastDateFormat sdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

  private final Charset charset;

  public PinballXTableParser(Charset charset) {
    this.charset = charset;
  }


  public int addGames(File xmlFile, List<String> games, Map<String, TableDetails> tabledetails, GameEmulator emu) {
    int gamecount = 0;
    try (Reader reader = new BufferedReader(new FileReader(xmlFile, charset))) {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(new InputSource(reader));

      Element root = doc.getDocumentElement();
      root.normalize();

      NodeList list = root.getChildNodes();
      for (int temp = 0; temp < list.getLength(); temp++) {
        Node node = list.item(temp);
        if (node.getNodeType() == Node.ELEMENT_NODE && node.hasChildNodes()) {
          Element element = (Element) node;
          if (StringUtils.equalsIgnoreCase(element.getTagName(), "game")) {
            
            String gameName = element.getAttribute("name");

            TableDetails detail = new TableDetails();
            doPreParsing(detail, emu, gameName);

            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
              Node childNode = childNodes.item(i);
              if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                String name = childNode.getNodeName();
                String content = childNode.getTextContent().trim();
                try {
                  readNode(detail, name, content);
                } catch (Exception e) {
                  LOG.warn("Ignored exception while parsing " + name + " '" + content+ "' of table '" + gameName + 
                    " for emulator "  + emu.getName() +  "': " + e.getMessage());
                }
              }
            }
            doPostParsing(detail);

            games.add(detail.getGameFileName());
            tabledetails.put(PinballXConnector.compose(emu.getId(), detail.getGameFileName()), detail);
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

  protected void doPreParsing(TableDetails detail, GameEmulator emu, String name) {
    String gameFileName =  name + "." + emu.getGameExt();
    detail.setGameFileName(gameFileName);
    detail.setGameName(name);
    detail.setEmulatorId(emu.getId());
    // will be overriden but enabled by default
    detail.setStatus(1);  // STATUS_NORMAL
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
  protected void readNode(TableDetails detail, String qName, String content) throws ParseException {
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
        setKeepDisplays(detail, content, VPinScreen.Menu);
        break;
      }
      case "hidetopper": {
        setKeepDisplays(detail, content, VPinScreen.Topper);
        break;
      }
      case "hidebackglass": {
        setKeepDisplays(detail, content, VPinScreen.BackGlass);
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
        detail.setGameRating(StringUtils.isNotEmpty(content) ? Integer.parseInt(content) : null);
        break;
      }
      case "players": {
        detail.setNumberOfPlayers(StringUtils.isNotEmpty(content) ? Integer.parseInt(content): null);
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
      case "vpsid": {
        detail.setWebGameId(content);
        detail.setWebLink2Url("https://virtualpinballspreadsheet.github.io/?game=" + content);
        break;
      }
    }
  }

  private void setKeepDisplays(TableDetails detail, String content, VPinScreen screen) {
    boolean hide = BooleanUtils.toBoolean(content);
    if (!hide) {
      detail.setKeepDisplays(detail.getKeepDisplays()!=null? detail.getKeepDisplays() + "," + screen.getCode(): 
          Integer.toString(screen.getCode()));
    }
  }

  protected void doPostParsing(TableDetails detail) {
    // if no description provided (should never happen), let's initiate displayName with gameName 
    if (StringUtils.isEmpty(detail.getGameDisplayName())) {
      detail.setGameDisplayName(detail.getGameName()); 
    }
  }

  //----------------------------------------

  public void writeGames(File pinballXDb, List<GameEntry> games, Map<String, TableDetails> mapTableDetails, GameEmulator emu) {
    if (!pinballXDb.exists()) {
      try {
        pinballXDb.getParentFile().mkdirs();
        pinballXDb.createNewFile();
      }
      catch (IOException ioe) {
        LOG.error("Cannot create file " + pinballXDb, ioe);
      }
    }
    
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pinballXDb), charset))) {

      writer.append("<menu>\n");
      for (GameEntry entry : games) {
        TableDetails detail = mapTableDetails.get(PinballXConnector.compose(emu.getId(), entry.getFilename()));
        if (detail!=null) {
          // <game name= /> stores the filename without extension 
          String gameFileName =StringUtils.removeEndIgnoreCase(entry.getFilename(), "." + emu.getGameExt());
          writer.append("  <game name=\"").append(escapeXml(gameFileName)).append("\">\n");

          appendDescription(writer, detail);

          appendValue(writer, "rom", detail.getRomName());
          appendValue(writer, "manufacturer", detail.getManufacturer());
          appendValue(writer, "year", detail.getGameYear()!=null? detail.getGameYear().toString(): "");
          appendValue(writer, "type", detail.getGameType()!=null? detail.getGameType(): "");
          appendKeepDisplays(writer, "hidedmd", detail.getKeepDisplays(), VPinScreen.Menu);
          appendKeepDisplays(writer, "hidetopper", detail.getKeepDisplays(), VPinScreen.Topper);
          appendKeepDisplays(writer, "hidebackglass", detail.getKeepDisplays(), VPinScreen.BackGlass);
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
          appendValue(writer, "vpsid", detail.getWebGameId());

          writer.append("  </game>\n");
        }
      }
      writer.append("</menu>");
    }
    catch (Exception e) {
      LOG.error("Error while writing " + pinballXDb, e);
    }
  }

  protected void appendDescription(BufferedWriter writer, TableDetails detail) throws IOException {
    appendValue(writer, "description", detail.getGameDisplayName());
  }

  protected void appendKeepDisplays(BufferedWriter writer, String tag, String keepDisplays, VPinScreen screen) throws IOException {
    boolean keep = VPinScreen.keepDisplaysContainsScreen(keepDisplays, screen);
    writer.append("    <" + tag + ">").append(keep? "False": "True" ).append("</" + tag + ">\n");   
  }

  protected void appendValue(BufferedWriter writer, String tag, String value) throws IOException {
    if (value != null) {
      appendValueNoEscape(writer, tag, escapeXml(value));
    }
  }
  protected void appendValue(BufferedWriter writer, String tag, Integer value) throws IOException {
    if (value != null) {
      appendValueNoEscape(writer, tag, value.toString());
    }
  }
  protected void appendValue(BufferedWriter writer, String tag, Boolean value) throws IOException {
    if (value != null) {
      appendValueNoEscape(writer, tag, value? "True": "False");
    }
  }
  protected void appendValue(BufferedWriter writer, String tag, Date value) throws IOException {
    if (value != null) {
      appendValueNoEscape(writer, tag, sdf.format(value));
    }
  }

  protected void appendValueNoEscape(BufferedWriter writer, String tag, String value) throws IOException {
    writer.append("    <" + tag + ">").append(value).append("</" + tag + ">\n");
  }

  /**
   * Specific XML escape utilities (vs StringEscapeUtils) to not escape '
   */
  protected String escapeXml(String value) {
    value = value.replace("&", "&amp;");
    value = value.replace("<", "&lt;");
    value = value.replace(">", "&gt;");
    return value;
  }
}
