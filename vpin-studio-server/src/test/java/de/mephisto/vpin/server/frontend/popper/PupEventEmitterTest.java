package de.mephisto.vpin.server.frontend.popper;

import org.junit.jupiter.api.Test;

import java.io.File;

public class PupEventEmitterTest {

  @Test
  public void testEmitting() {
    File f = new File("C:\\vPinball\\PinUPSystem");
    if(f.exists()) {
      PupEventEmitter emitter = new PupEventEmitter(f);
      emitter.sendPupEvent(11, 2);
    }
  }
}
