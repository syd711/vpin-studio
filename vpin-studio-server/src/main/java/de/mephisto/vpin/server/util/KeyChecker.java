package de.mephisto.vpin.server.util;

import org.apache.commons.lang3.StringUtils;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Checks the native key event, e.g.
 */
public class KeyChecker {
  private final static Logger LOG = LoggerFactory.getLogger(KeyChecker.class);

  private int key;

  public KeyChecker(int key) {
    this.key = key;
  }

  public boolean matches(NativeKeyEvent event) {
    return event.getRawCode() == key;
  }
}
