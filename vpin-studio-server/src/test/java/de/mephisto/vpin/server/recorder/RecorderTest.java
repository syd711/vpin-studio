package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.recorder.RecordMode;
import de.mephisto.vpin.restclient.recorder.RecordingScreen;
import de.mephisto.vpin.restclient.recorder.RecordingScreenOptions;
import de.mephisto.vpin.server.frontend.popper.PinUPConnector;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RecorderTest {

  @Test
  public void testRecorder() throws Exception {
    PinUPConnector connector = new PinUPConnector() {
      @NotNull
      @Override
      public File getInstallationFolder() {
        return new File("../testsystem/vPinball/PinUPSystem");
      }
    };

    List<FrontendPlayerDisplay> frontendPlayerDisplays = connector.getFrontendPlayerDisplays();
    assertFalse(frontendPlayerDisplays.isEmpty());

    RecordingScreen s = new RecordingScreen();
    s.setDisplay(frontendPlayerDisplays.stream().filter(d -> d.getName().equals(VPinScreen.PlayField.name())).findFirst().get());
    s.setScreen(VPinScreen.PlayField);

    RecordingScreenOptions options = new RecordingScreenOptions();
    options.setRecordMode(RecordMode.overwrite);
    options.setEnabled(true);
    options.setRecordingDuration(3);


    File target = File.createTempFile("recorder", ".mp4");
    target.deleteOnExit();

    ScreenRecorder recorder = new ScreenRecorder(s, target);

    recorder.start(options);


    Thread.sleep(5);
    assertTrue(target.exists());
    assertTrue(target.length() > 0);

//    FileUtils.copyFile(target, new File("C:/temp/" + target.getName()));

    target.delete();
  }
}
