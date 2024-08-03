package de.mephisto.vpin.server.score;

public class GameToProcessorFactory {

  public DMDScoreProcessor getProcessor(String gameName) {
    return new DMDScoreProcessorDelegate(
      getFrameDumpProcessor(), 
      _getProcessor(gameName)
    );
  }

  public DMDScoreProcessor _getProcessor(String gameName) {
    if (gameName.startsWith("___avr_200")) {
      return getImage2SidesProcessor();
    }
    else {
      return getImageScannerProcessor();
    }
  }

  /**
   * A processor that dump all frames in dump.txt, no filtering
   */
  public DMDScoreProcessor getFrameDumpProcessor() {
    return new DMDScoreProcessorFrameDump();    
  }

  /**
   * A processor that dump images using dmd palette, skiping frames shortly displayed 
   */
  public DMDScoreProcessor getImageDumpProcessor() {
    return new DMDScoreProcessorFilterFixFrame(
      new DMDScoreProcessorImageDump());
  }

  /**
   * A processor recognize texts from images 
   */
  public DMDScoreProcessor getImageScannerProcessor() {
    return new DMDScoreProcessorFilterFixFrame(
      new DMDScoreProcessorImageDump(), 
      new DMDScoreAnalyser());
  }

    /**
   * A processor that split images in two at given position, and process them separately 
   */
  public DMDScoreProcessor getImage2SidesProcessor() {
    return new DMDScoreProcessorFilterFixFrame(
      new DMDScoreScanner2Sides(41));
  }

}
