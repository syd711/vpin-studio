package de.mephisto.vpin.server.frontend.pinbally;

import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.server.frontend.pinballx.PinballXTableParser;
import de.mephisto.vpin.server.games.GameEmulator;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;

public class PinballYTableParser extends PinballXTableParser {

  public PinballYTableParser(Charset charset) {
    super(charset);
  }

  protected void doPreParsing(TableDetails detail, GameEmulator emu, String name) {
    super.doPreParsing(detail, emu, name);
  }

  /**
   <game name="007 Goldeneye (Sega 1996)">
    <description>007 Goldeneye (Sega 1996)</description>
    <title>007 Goldeneye</title>
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
    <ipdbid>3792</ipdbid>
    <dateadded>2023-06-10 19:14:16</dateadded>
    <datemodified>2022-04-30 00:00:46</datemodified>
  </game>
   * @param detail 
   */
  protected void readNode(TableDetails detail, String qName, String content) throws ParseException {
    switch (qName) {
      case "description": {
        detail.setGameName(content);
        break;
      }
      case "title": {
        detail.setGameDisplayName(content);
        break;
      }
      case "ipdbid": {
        detail.setIPDBNum(content);
        break;
      }
      default: {
        super.readNode(detail, qName, content);
      }
    }
  }

  protected void doPostParsing(TableDetails detail) {
    // if no <title> provided, initiate displayName with gameName 
    if (StringUtils.isEmpty(detail.getGameDisplayName())) {
      detail.setGameDisplayName(detail.getGameName()); 
    }
    // then clean gameName
    detail.setGameName(cleanGameName(detail.getGameName()));
  }

  /**
   * Apply some transformation on gameName to make looks like a filename 
   * see https://github.com/mjrgh/PinballY/blob/9cf6686ae6d9e4c0bdf61e323f83f42655dcee8b/PinballY/GameList.cpp#L4110
   */
  private String cleanGameName(String gameName) {
    StringBuilder cleanGameName = new StringBuilder();
    int len = gameName.length();
    int i = -1;
    while (++i < len) {
      char c = gameName.charAt(i);
      switch (c) {
        case '<' : cleanGameName.append('('); break;
        case '>' : cleanGameName.append(')'); break;
        case ':' : break;
        case '/' : cleanGameName.append(';'); break;
        case '|' : cleanGameName.append(';'); break;
        case '?' : break;
        case '*' : cleanGameName.append('+'); break;
        case '"' : cleanGameName.append('\''); break;
        case '\\' : cleanGameName.append(';'); break;
        default : cleanGameName.append(c);
      }
    }
    return cleanGameName.toString();
  }

  //----------------------------------------

  protected void appendDescription(BufferedWriter writer, TableDetails detail) throws IOException {
    if (StringUtils.equals(detail.getGameName(), cleanGameName(detail.getGameDisplayName()))) {
      appendValue(writer, "description", detail.getGameDisplayName());
    }
    else {
      appendValue(writer, "description", detail.getGameName());
      appendValue(writer, "title", detail.getGameDisplayName());
    }
  }

  protected void appendValueNoEscape(BufferedWriter writer, String tag, String value) throws IOException {
    // do some tag transcodification
    if ("IPDBnr".equals(tag)) {
      super.appendValueNoEscape(writer, "ipdbid", value);
    } 
    else {
      super.appendValueNoEscape(writer, tag, value);
    }
  }

}
