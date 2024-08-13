package de.mephisto.vpin.server.score;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
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

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("LCD.txt")))) {
      parseChars(reader, " 01223344556678899+-*/>'", 7, 11);
      parseChars(reader, "ABCDEFGHIJKLM", 7, 11);
      parseChars(reader, "NOPQRSTUVWXYZ", 7, 11);
      parseChars(reader, " 0123456789", 7, 7);
      parseChars(reader, " 0123456789", 5, 11);
    }
    catch (IOException e) {}
  }

  private void parseChars(BufferedReader reader, String chars, int width, int height) throws IOException {
    String[] bufs = new String[chars.length()];
    Arrays.setAll(bufs, i -> "");

    // read first comment line
    @SuppressWarnings("unused")
    String comment = reader.readLine();

    for(int i = 0; i < height; i++) {
      String line = reader.readLine();
      for (int c = 0; c < chars.length(); c++) {
        String subline = line.substring(c * (width + 1), c * (width + 1) + width);
        bufs[c] += subline;
      }
    }

    for (int c = 0; c < chars.length(); c++) {
      String ch = chars.substring(c, c + 1);
      characters.put(bufs[c], ch);
    }

  }

  protected int charWidth() {
    return type.equals(Type.WIDTH_5) ? 6 : 8;
  }
  protected int _charHeight() {
    return 11;
  }
  protected int offsetX() {
    return type.equals(Type.WIDTH_5) ? 4  : 0;
  }

  //-----------------------------------------------

  @Override
  public void onFrameReceived(Frame frame, List<FrameText> texts) {
    
    byte blank = getBlankIndex(frame.getPalette());

    // Detect if DMD with 2 or 3 rows, check the first non blank pixel on row 1 (use this because of small figures of LCD7)
    int x = getFirstColorX(frame, 0, frame.getWidth(), 0, frame.getHeight(), 1, blank);

    if (x == -1) {
      extractFromLine(frame, texts, blank, 2, 13);   
      extractFromLine(frame, texts, blank, 19, 30);
    }
    else {
      extractFromLine(frame, texts, blank, 0, 11);   
      extractFromLine(frame, texts, blank, 12, 23);
      extractFromLine(frame, texts, blank, 24, 31);
    }
 }

  private void extractFromLine(Frame frame, List<FrameText> texts, byte blank, int yFrom, int yTo) {
    StringBuilder bld = new StringBuilder();
    int width = charWidth();
    int startX = 10000;
    int endX = 0;
    for (int i = 0; i < (128 - 2 * offsetX()) / width; i++) {
      StringBuilder buf = new StringBuilder();
      for (int y = yFrom; y < yTo; y++) {
        for(int dX = 0; dX < width - 1; dX++) {
          int x = offsetX() + width * i + dX;
          byte c = frame.getColor(x, y);
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
    if (startX >= 0) {
        texts.add(new FrameText(bld.toString().trim(), startX, yFrom, endX - startX, yTo - yFrom));
    }
  }
}
