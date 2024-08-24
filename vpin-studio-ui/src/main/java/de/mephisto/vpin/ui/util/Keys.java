package de.mephisto.vpin.ui.util;

import org.apache.commons.lang3.StringUtils;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class Keys {

  public static int[] KEY_CODES = {KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4, KeyEvent.VK_F5,
    KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8, KeyEvent.VK_F9, KeyEvent.VK_F10, KeyEvent.VK_F11, KeyEvent.VK_F12,
    KeyEvent.VK_A,
    KeyEvent.VK_B,
    KeyEvent.VK_C,
    KeyEvent.VK_D,
    KeyEvent.VK_E,
    KeyEvent.VK_F,
    KeyEvent.VK_G,
    KeyEvent.VK_H,
    KeyEvent.VK_I,
    KeyEvent.VK_J,
    KeyEvent.VK_K,
    KeyEvent.VK_L,
    KeyEvent.VK_M,
    KeyEvent.VK_N,
    KeyEvent.VK_O,
    KeyEvent.VK_Q,
    KeyEvent.VK_R,
    KeyEvent.VK_S,
    KeyEvent.VK_T,
    KeyEvent.VK_U,
    KeyEvent.VK_V,
    KeyEvent.VK_W,
    KeyEvent.VK_X,
    KeyEvent.VK_Y,
    KeyEvent.VK_Z,
    KeyEvent.VK_0,
    KeyEvent.VK_1,
    KeyEvent.VK_2,
    KeyEvent.VK_3,
    KeyEvent.VK_4,
    KeyEvent.VK_5,
    KeyEvent.VK_6,
    KeyEvent.VK_7,
    KeyEvent.VK_8,
    KeyEvent.VK_9,
    KeyEvent.VK_LEFT,
    KeyEvent.VK_RIGHT,
    KeyEvent.VK_UP,
    KeyEvent.VK_DOWN,
    KeyEvent.VK_ADD,
    KeyEvent.VK_SUBTRACT,
    KeyEvent.VK_MULTIPLY,
    KeyEvent.VK_DIVIDE,
    KeyEvent.VK_ALT,
    KeyEvent.VK_AMPERSAND,
    KeyEvent.VK_ASTERISK,
    KeyEvent.VK_ESCAPE,
    KeyEvent.VK_END,
    KeyEvent.VK_STOP,
    KeyEvent.VK_BRACELEFT,
    KeyEvent.VK_BRACERIGHT,
    KeyEvent.VK_OPEN_BRACKET,
    KeyEvent.VK_CLOSE_BRACKET,
    KeyEvent.VK_ENTER,
    KeyEvent.VK_SPACE
  };

  public static int[] KEY_CODES_PINEMHI = {KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4, KeyEvent.VK_F5,
    KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8, KeyEvent.VK_F9, KeyEvent.VK_F10, KeyEvent.VK_F11, KeyEvent.VK_F12,
    KeyEvent.VK_A,
    KeyEvent.VK_B,
    KeyEvent.VK_C,
    KeyEvent.VK_D,
    KeyEvent.VK_E,
    KeyEvent.VK_F,
    KeyEvent.VK_G,
    KeyEvent.VK_H,
    KeyEvent.VK_I,
    KeyEvent.VK_J,
    KeyEvent.VK_K,
    KeyEvent.VK_L,
    KeyEvent.VK_M,
    KeyEvent.VK_N,
    KeyEvent.VK_O,
    KeyEvent.VK_Q,
    KeyEvent.VK_R,
    KeyEvent.VK_S,
    KeyEvent.VK_T,
    KeyEvent.VK_U,
    KeyEvent.VK_V,
    KeyEvent.VK_W,
    KeyEvent.VK_X,
    KeyEvent.VK_Y,
    KeyEvent.VK_Z,
    KeyEvent.VK_0,
    KeyEvent.VK_1,
    KeyEvent.VK_2,
    KeyEvent.VK_3,
    KeyEvent.VK_4,
    KeyEvent.VK_5,
    KeyEvent.VK_6,
    KeyEvent.VK_7,
    KeyEvent.VK_8,
    KeyEvent.VK_9,
    KeyEvent.VK_LEFT,
    KeyEvent.VK_RIGHT,
    KeyEvent.VK_UP,
    KeyEvent.VK_DOWN,
    KeyEvent.VK_ALT,
    KeyEvent.VK_AMPERSAND,
    KeyEvent.VK_ASTERISK,
    KeyEvent.VK_ESCAPE,
    KeyEvent.VK_END,
    KeyEvent.VK_STOP,
    KeyEvent.VK_BRACELEFT,
    KeyEvent.VK_BRACERIGHT,
    KeyEvent.VK_OPEN_BRACKET,
    KeyEvent.VK_CLOSE_BRACKET,
    KeyEvent.VK_ENTER,
    KeyEvent.VK_SPACE
  };

  public static boolean isSpecial(javafx.scene.input.KeyEvent event) {
    if (event.isControlDown() || event.isAltDown() || event.isMetaDown() || event.isShiftDown() || event.isShortcutDown()) {
      return true;
    }

    String text = event.getText();
    if (StringUtils.isEmpty(text)) {
      return true;
    }

    return false;
  }

  public static String toKeyValue(String value) {
    if (value.length() > 1) {
      if (value.equals(KeyEvent.getKeyText(KeyEvent.VK_CLOSE_BRACKET))) {
        return "]";
      }
      else if (value.equals(KeyEvent.getKeyText(KeyEvent.VK_OPEN_BRACKET))) {
        return "[";
      }
      else if (value.equals(KeyEvent.getKeyText(KeyEvent.VK_BRACELEFT))) {
        return "{";
      }
      else if (value.equals(KeyEvent.getKeyText(KeyEvent.VK_BRACERIGHT))) {
        return "}";
      }
      else if (value.equals(KeyEvent.getKeyText(KeyEvent.VK_ASTERISK))) {
        return "*";
      }
      else if (value.equals(KeyEvent.getKeyText(KeyEvent.VK_DIVIDE))) {
        return "NumPad /";
      }
      else if (value.equals(KeyEvent.getKeyText(KeyEvent.VK_ADD))) {
        return "NumPad +";
      }
      else if (value.equals(KeyEvent.getKeyText(KeyEvent.VK_SUBTRACT))) {
        return "NumPad -";
      }
      else if (value.equals(KeyEvent.getKeyText(KeyEvent.VK_MULTIPLY))) {
        return "NumPad *";
      }
      else if (value.equals(KeyEvent.getKeyText(KeyEvent.VK_SLASH))) {
        return "/";
      }
      else if (value.equals(KeyEvent.getKeyText(KeyEvent.VK_AMPERSAND))) {
        return "*";
      }
      else if (value.equals(KeyEvent.getKeyText(KeyEvent.VK_ESCAPE))) {
        return "Esc";
      }
    }
    return value;
  }

  public static List<String> getKeyNames() {
    List<String> result = new ArrayList<>();
    for (int keyCode : KEY_CODES) {
      result.add(getKeyDisplayName(keyCode));
    }
    Collections.sort(result);
    return result;
  }

  public static List<String> getUIKeyNames() {
    List<String> result = new ArrayList<>();
    for (int keyCode : KEY_CODES) {
      result.add(getKeyDisplayName(keyCode));
    }
    result.add(0, "");
    Collections.sort(result);
    return result;
  }

  public static List<String> getPinemHiKeyNames() {
    List<String> result = new ArrayList<>();
    for (int keyCode : KEY_CODES_PINEMHI) {
      result.add(getKeyDisplayName(keyCode));
    }
    result.add(0, "");
    Collections.sort(result);
    return result;
  }

  private static String getKeyDisplayName(int code) {
    return KeyEvent.getKeyText(code);
  }
}
