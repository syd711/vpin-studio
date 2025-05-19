package de.mephisto.vpin.server.recorder;

import de.mephisto.vpin.restclient.frontend.FrontendPlayerDisplay;
import de.mephisto.vpin.server.frontend.popper.PinUPConnector;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class RecorderTest {

  @Test
  public void testRecorder() throws Exception {
    PinUPConnector connector = new PinUPConnector() {
      @NonNull
      @Override
      public File getInstallationFolder() {
        return new File("../testsystem/vPinball/PinUPSystem");
      }
    };

    List<FrontendPlayerDisplay> frontendPlayerDisplays = connector.getFrontendPlayerDisplays();
    assertFalse(frontendPlayerDisplays.isEmpty());
//
//    RecordingScreen s = new RecordingScreen();
//    s.setDisplay(frontendPlayerDisplays.stream().filter(d -> d.getScreen().equals(VPinScreen.PlayField)).findFirst().get());
//    s.setScreen(VPinScreen.PlayField);
//
//    RecordingScreenOptions options = new RecordingScreenOptions();
//    options.setRecordMode(RecordMode.overwrite);
//    options.setEnabled(true);
//    options.setRecordingDuration(3);
//
//
//    File target = File.createTempFile("recorder", ".mp4");
//    target.deleteOnExit();
//
//    ScreenRecorder recorder = new ScreenRecorder(s, target);
//
//    recorder.record(options);
//
//
//    Thread.sleep(5);
//    assertTrue(target.exists());
//    assertTrue(target.length() > 0);
//
////    FileUtils.copyFile(target, new File("C:/temp/" + target.getName()));
//
//    target.delete();
  }
}
