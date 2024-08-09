package de.mephisto.vpin.server.score;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;

public class DMDScoreReplayTest {

  @Test
  public void testAVR() throws Exception {

    String gameName = "ind250cc";

    File f = //new File(getClass().getResource("avr_200.txt").toURI());
      new File("c:/temp/" + gameName, "dump.txt");

    // skip frames that are strictly equals
    //replayFile(f, proc, 5502975, 5503615);

    //replayFile(f, proc, 5497114, 5502506);
    try (FileInputStream in = new FileInputStream(f)) {
      doReplay(in, gameName, -1, -1);
    }  
  }

  public void doReplay(InputStream in, String gameName, int timeFrom, int timeTo) throws Exception{
    DMDScoreScannerTessAPI.TESSERACT_FOLDER = "." + DMDScoreScannerTessAPI.TESSERACT_FOLDER;
    try {
      GameToProcessorFactory factory = new GameToProcessorFactory();

      DMDScoreProcessor scanner = new DMDScoreProcessorFilterFixFrame(
        new DMDScoreProcessorImageDump(), factory.getScanner(gameName));
      DMDScoreProcessor analyser = factory.getAnalyser(gameName);

      DMDScoreGameProcessor processor = new DMDScoreGameProcessor();
      processor.setScanner(scanner);
      processor.setAnalyser(analyser);

      DMDScoreGameReplayer replayer = new DMDScoreGameReplayer();
      replayer.replay(processor, in, timeFrom, timeTo);

      replayer = null;
    }
    finally {
      DMDScoreScannerTessAPI.TESSERACT_FOLDER = DMDScoreScannerTessAPI.TESSERACT_FOLDER.substring(1);
    }
  }


}