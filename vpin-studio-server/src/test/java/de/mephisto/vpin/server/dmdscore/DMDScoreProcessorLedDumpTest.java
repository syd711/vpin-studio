package de.mephisto.vpin.server.dmdscore;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;

/**
 * Read a dump.txt and replay it
 */
public class DMDScoreProcessorLedDumpTest {

  @Test
  public void testDump() throws IOException {

    File temp = Files.createTempDirectory("tmpLedDump").toFile();
    Runtime.getRuntime().addShutdownHook(new Thread(() -> FileUtils.deleteQuietly(temp)));

    //DMDScoreProcessorLedDump processor = new DMDScoreProcessorLedDump(temp);
    DMDScoreProcessorFrameDump processor = new DMDScoreProcessorFrameDump(temp);

    try (InputStream in = getClass().getResourceAsStream("dump.txt");
         BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
      String gameName = br.readLine();
      br.readLine();

      processor.onFrameStart(gameName);

      String line;
      while ((line = br.readLine()) != null) {
        String[] meta = StringUtils.splitByWholeSeparator(line, " / ");
        int timeStamp = Integer.parseInt(meta[0]);
        FrameType frameType = FrameType.getEnum(meta[1]);
        int width = Integer.parseInt(meta[2]);
        int height = Integer.parseInt(meta[3]);

        String allpalette = br.readLine();
        String[] paletteStr = StringUtils.split(allpalette, ',');
        int[] palette = new int[paletteStr.length];
        for (int p = 0; p < paletteStr.length; p++) {
          palette[p] = Integer.parseInt(paletteStr[p]);
        }

        int[] plane = new int[width * height];
        for (int j = 0; j < height; j++) {
          line = br.readLine();
          for (int i = 0; i < width; i++) {
            int idx = line.codePointAt(i);
            byte b = (byte) (idx == 32 ? 0 : Character.isDigit(idx) ? idx - 48: idx - 55);
            plane[j * width + i] = palette[b];
          }
        }
        br.readLine();

        Frame f = new Frame(frameType, timeStamp, plane, width, height);
        processor.onFrameReceived(f);
      }
      processor.onFrameStop(gameName);
    }
  }
}
