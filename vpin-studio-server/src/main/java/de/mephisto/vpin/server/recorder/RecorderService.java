package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.server.frontend.FrontendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecorderService {

  @Autowired
  private FrontendService frontendService;

  public List<RecordingScreen> getRecordingScreens() {
    List<VPinScreen> supportedRecodingScreens = frontendService.getFrontend().getSupportedRecodingScreens();
    List<FrontendPlayerDisplay> frontendPlayerDisplays = frontendService.getFrontendConnector().getFrontendPlayerDisplays();

    List<RecordingScreen> recordingOptions = new ArrayList<>();
    for (VPinScreen screen : supportedRecodingScreens) {
      FrontendPlayerDisplay display = VPinScreen.valueOfScreen(frontendPlayerDisplays, screen);
      RecordingScreen recordingScreen = new RecordingScreen();
      recordingScreen.setScreen(screen);
      recordingScreen.setDisplay(display);

      recordingOptions.add(recordingScreen);
    }
    return recordingOptions;
  }
}
