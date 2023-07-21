package de.mephisto.vpin.server.volume;

import org.junit.jupiter.api.Test;

import javax.sound.sampled.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class VolumeServiceTest {

  @Test
  public void volumeTest() {
    VolumeService volumeService = new VolumeService();
    float currentVolume = volumeService.getCurrentVolume();
    assertTrue(currentVolume > 0);
  }

  @Test
  public void testMixer() throws LineUnavailableException {
    Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
    for (int i = 0; i <mixerInfo.length; i++) {
      Mixer mixer = AudioSystem.getMixer(mixerInfo[i]);
      System.out.println(mixer.getMixerInfo().getName());
      mixer.open();
      Line[] line = mixer.getTargetLines();
      for (Line line1 : line) {
        System.out.println(line1);
      }

      Control[] ctrl = mixer.getControls();
      System.out.println("Controls found : " + ctrl.length);
      for (int j = 0; j < ctrl.length; j++) {
        System.out.println("Control " + ctrl[j].toString() + " : " + ctrl[j].getType());
      }
      break;
    }
  }
}
