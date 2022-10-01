package de.mephisto.vpin.server.util;

import org.apache.commons.lang3.StringUtils;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks the native key event, e.g.
 * NATIVE_KEY_PRESSED,keyCode=46,keyText=C,keyChar=Undefiniert,modifiers=Strg,keyLocation=KEY_LOCATION_STANDARD,rawCode=67
 * for Ctrl+C
 */
public class KeyChecker {
  private final static Logger LOG = LoggerFactory.getLogger(KeyChecker.class);

  private int modifier;
  private String letter = null;

  public KeyChecker(String hotkey) {
    if (hotkey != null) {
      if (hotkey.contains("+")) {
        this.modifier = Integer.parseInt(hotkey.split("\\+")[0]);
        this.letter = hotkey.split("\\+")[1];
      }
      else {
        this.letter = hotkey;
      }
    }
  }

  public boolean matches(NativeKeyEvent event) {
    String keyText = NativeKeyEvent.getKeyText(event.getKeyCode());
    if (StringUtils.isEmpty(keyText)) {
      LOG.error("No key binding configured, ignoring key event.");
      return false;
    }
    return (keyText.equalsIgnoreCase(this.letter) && this.modifier == event.getModifiers()) ||
        (StringUtils.isEmpty(letter) && this.modifier == event.getModifiers());
  }
}
