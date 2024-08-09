package de.mephisto.vpin.server.score;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

/**
 * A simple Processor that writes frames in a file
 */
public class DMDScoreScannerLCD extends DMDScoreProcessorBase {

  public static enum Type {
    WIDTH_7, WIDTH_5
  }

  private Type type;

  /** The map between sequence of dot and a Letter */
  private Map<String, String> characters = new HashMap<>();


  public DMDScoreScannerLCD(Type type) {
    this.type = type;
    switch (type) {
      case WIDTH_5:
        parseLCD5();        
        break;
      case WIDTH_7:
        parseLCD7();        
        break;
    }
  }
  private void parseLCD5() {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("LCD5.txt")))) {
      parseChars(reader, " 0123456789");
    }
    catch (IOException e) {}
  }
  private void parseLCD7() {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("LCD7.txt")))) {
      parseChars(reader, " 0123456789+-*/>'");
      parseChars(reader, "ABCDEFGHIJKLM");
      parseChars(reader, "NOPQRSTUVWXYZ");
    }
    catch (IOException e) {}
  }

  private void parseChars(BufferedReader reader, String chars) throws IOException {
    // here 7 and 11 numbers are aligned with chars in LCD.txt
    int width = charWidth();
    int height = charHeight();

    for(int i = 0; i < height; i++) {
      String line = reader.readLine();
      for (int c = 0; c < chars.length(); c++) {
        String ch = chars.substring(c, c + 1);
        String subline = line.substring(c * width, (c + 1) * width - 1);
        subline = subline.substring(0, charWidth() - 1);
        String buf = StringUtils.defaultString(characters.remove(ch));
        buf += subline;
        if (i == height - 1) {
          characters.put(buf, ch);
        } else {
          characters.put(ch, buf);
        }
      }
    }
    // read last blank line
    reader.readLine();
  }

  protected int charWidth() {
    return type.equals(Type.WIDTH_5) ? 6 : 8;
  }
  protected int charHeight() {
    return 11;
  }
  protected int offsetX() {
    return type.equals(Type.WIDTH_5) ? 4  : 0;
  }

  //-----------------------------------------------

  @Override
  public void onFrameReceived(Frame frame, List<FrameText> texts) {
    byte blank = getBlankIndex(frame.getPalette());
    FrameText l1 = extractFromLine(frame, blank, 2);
    if (l1 != null) {
      texts.add(l1);
    }
    FrameText l2 = extractFromLine(frame, blank, 19);
    if (l2 != null) {
      texts.add(l2);
    }
  }

  private FrameText extractFromLine(Frame frame, byte blank, int startY) {
    StringBuilder bld = new StringBuilder();
    int width = charWidth();
    int height = charHeight();
    int startX = 10000;
    int endX = 0;
    for (int i = 0; i < (128 - 2 * offsetX()) / width; i++) {
      StringBuilder buf = new StringBuilder();
      for (int dY = 0; dY < height; dY++) {
        for(int dX = 0; dX < width - 1; dX++) {
          int x = offsetX() + width * i + dX;
          byte c = frame.getColor(x, startY + dY);
          if (c != blank) {
            startX = Math.min(startX, x);
            endX = Math.max(endX, x + width - 1);
            buf.append("8");
          }
          else {
            buf.append(" ");
          }
        }
      }
      String ch = characters.get(buf.toString());
      bld.append(StringUtils.defaultString(ch, "?"));
    }
    return startX >= 0 ? new FrameText(bld.toString().trim(), startX, startY, endX - startX, 11): null;
  }

}
