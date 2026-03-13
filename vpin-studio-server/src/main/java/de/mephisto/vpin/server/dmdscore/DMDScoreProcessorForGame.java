package de.mephisto.vpin.server.dmdscore;

public class DMDScoreProcessorForGame implements DMDScoreProcessor {

  private DMDScoreProcessor currentForGame;


  @Override
  public void onFrameStart(String gameName) {
    currentForGame = getProcessor(gameName);
    if (currentForGame != null) {
      currentForGame.onFrameStart(gameName);
    }
  }

  @Override
  public void onFrameReceived(Frame frame) {
    if (currentForGame != null) {
      currentForGame.onFrameReceived(frame);
    }
  }
  
  @Override
  public void onFrameStop(String gameName) {
    if (currentForGame != null) {
      currentForGame.onFrameStop(gameName);
    }
    currentForGame = null;
  }

  public DMDScoreProcessor getProcessor(String gameName) {
    return getImage2SidesProcessor();
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
