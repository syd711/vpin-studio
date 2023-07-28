package de.mephisto.vpin.server.pinvol;

import de.mephisto.vpin.commons.utils.PropertiesStore;
import de.mephisto.vpin.server.util.KeyChecker;
import org.jnativehook.keyboard.NativeKeyEvent;

import java.io.File;

public class PinVolKeyManager {
  public String globalVolUp = "Windows+F10";
  public String globalVolDown = "Windows+F9";
  public String localVolUp = "VolumeUp";
  public String localVolDown = "VolumeDown";

  public PinVolKeyManager() {
  }

  public void reload() {
    File ini = getPinVolSettingsIni();
    if (ini.exists()) {
      PropertiesStore store = PropertiesStore.create(getPinVolSettingsIni());
      globalVolUp = store.getString("globalVolUp");
      globalVolDown = store.getString("globalVolDown");
      localVolUp = store.getString("localVolUp");
      localVolDown = store.getString("localVolDown");
    }
  }

  public boolean isPinVolKey(NativeKeyEvent event) {
    KeyChecker checker = new KeyChecker(globalVolDown);
    return true;
  }

  private File getPinVolSettingsIni() {
    return new File("resources", "PinVolSettings.ini");
  }
}
