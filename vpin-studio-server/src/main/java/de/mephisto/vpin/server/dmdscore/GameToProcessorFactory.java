package de.mephisto.vpin.server.dmdscore;

public class GameToProcessorFactory {

  public DMDScoreProcessor getProcessor(String gameName) {
    return getImage2SidesProcessor();
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
    return new DMDScoreProcessorFilterFixFrame(new DMDScoreProcessorImageDump());
  }

  /**
   * A processor that dump images using dmd palette, skiping frames shortly displayed 
   */
  public DMDScoreProcessor getImageGenProcessor() {
    return new DMDScoreProcessorFilterFixFrame(new DMDScoreProcessorImageScanner());
  }

    /**
   * A processor that dump images using dmd palette, skiping frames shortly displayed 
   */
  public DMDScoreProcessor getImage2SidesProcessor() {
    return new DMDScoreProcessorFilterFixFrame(new DMDScoreProcessor2Sides(41));
  }

}
