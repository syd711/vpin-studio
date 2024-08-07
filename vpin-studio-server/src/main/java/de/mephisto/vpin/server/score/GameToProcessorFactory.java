package de.mephisto.vpin.server.score;

public class GameToProcessorFactory {

  public DMDScoreProcessor getProcessor(String gameName) {
    return new DMDScoreProcessorDelegate(
      getFrameDumpProcessor(), 
      _getProcessor(gameName)
    );
  }

  public DMDScoreProcessor _getProcessor(String gameName) {
    return getImageScannerProcessor();
  }

  /**
   * A processor that dump all frames in dump.txt, no filtering
   */
  public DMDScoreProcessor getFrameDumpProcessor() {
    return new DMDScoreProcessorFilterFixFrame(-1, 
      new DMDScoreProcessorFrameDump()
    );
  }

  /**
   * A processor recognize texts from images 
   */
  public DMDScoreProcessor getImageScannerProcessor() {
    return new DMDScoreProcessorFilterFixFrame( 
      new DMDScoreProcessorImageDump(), 
      new DMDScoreAnalyser()
    );
  }

}
