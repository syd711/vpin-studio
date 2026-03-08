package de.mephisto.vpin.server.util;

import de.mephisto.vpin.commons.utils.WinRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class WinRegistryTest {

  @Test
  public void testReg() {
    List<String> currentUserKeys = WinRegistry.getCurrentUserKeys("Software\\Freeware\\Visual PinMame");
    for (String currentUserKey : currentUserKeys) {
      Map<String, Object> currentUserValues = WinRegistry.getCurrentUserValues("Software\\Freeware\\Visual PinMame\\" + currentUserKey);
      if (!currentUserValues.isEmpty() && currentUserValues.containsKey("volume")) {
        System.out.println(currentUserKey + "\n");
        System.out.println(currentUserValues);
      }
    }
  }
}
