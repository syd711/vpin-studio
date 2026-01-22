package de.mephisto.vpin.server.recorder;

import java.awt.image.BufferedImage;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.mephisto.vpin.server.dmdscore.DMDScoreProcessor;
import de.mephisto.vpin.server.dmdscore.DMDScoreProcessorLedDump;
import de.mephisto.vpin.server.dmdscore.DMDScoreWebSocketHandler;
import de.mephisto.vpin.server.dmdscore.Frame;

@Component
public class ScreenDmdRecorder implements InitializingBean {

  @Autowired
  private DMDScoreWebSocketHandler handler;

  private String currentGame;  

  private Frame currentFrame;


  public String getCurrentGame() {
    return currentGame;
  }

  public BufferedImage getCurrentImage() {
    return currentFrame != null ? DMDScoreProcessorLedDump.frameToImage(currentFrame) : null;
  }

  //--------------------------------------------

  @Override
  public void afterPropertiesSet() throws Exception {
    if (handler != null) {
      handler.addDMDScoreProcessor(new DMDScoreProcessor() {
        @Override
        public void onFrameStart(String gameName) {
          ScreenDmdRecorder.this.currentGame = gameName;
        }

        @Override
        public void onFrameReceived(Frame frame) {
          ScreenDmdRecorder.this.currentFrame = frame;
        }

        @Override
        public void onFrameStop(String gameName) {
        }
      });
    }
  }

}
