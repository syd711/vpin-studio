package de.mephisto.vpin.server.score;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * A simple Processor that writes frames in a file
 */
public class DMDScoreScannerLCD extends DMDScoreProcessorBase {

  Map<String, String> characters = new HashMap<>();

  public DMDScoreScannerLCD() {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("LCD.txt")))) {
      parseChars(reader, "0123456789+-*/>'");
      parseChars(reader, "ABCDEFGHIJKLM");
      parseChars(reader, "NOPQRSTUVWXYZ");
    }
    catch (IOException e) {
    }
  }
  private void parseChars(BufferedReader reader, String chars) throws IOException {
    for(int i = 0; i < 11; i++) {
      String line = reader.readLine();
      for (int c = 0; c < chars.length(); c++) {
        String ch = chars.substring(c, c + 1);
        String buf = StringUtils.defaultString(characters.remove(ch));
        buf += line.substring(c * 8, c * 8 + 7);
        if (i == 10) {
          characters.put(buf, ch);
        } else {
          characters.put(ch, buf);
        }
      }
    }
    // read last blank line
    reader.readLine();
  }

  @Override
  public String onFrameReceived(Frame frame) {
    byte blank = getBlankIndex(frame.getPalette());
    String l1 = extractFromLine(frame, blank, 2);
    String l2 = extractFromLine(frame, blank, 19);
    return l1 + "\n" + l2;
  }
  private String extractFromLine(Frame frame, byte blank, int startY) {
    StringBuilder bld = new StringBuilder();
    for (int i = 0; i < 16; i++) {
      StringBuilder buf = new StringBuilder();
      for (int y = startY; y < startY + 11; y++) {
        for(int x = 8 * i; x < 8 * i + 7; x++) {
          byte c = frame.getColor(x, y);
          buf.append(c == blank ? " " : "8");
        }
      }
      String ch = characters.get(buf.toString());
      bld.append(StringUtils.defaultString(ch, " "));
    }
    return bld.toString().trim();
  }

}
