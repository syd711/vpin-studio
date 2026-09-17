package de.mephisto.vpin.commons.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Without the guards, JNA throws an UnsatisfiedLinkError for Advapi32 here.
 */
@DisabledOnOs(OS.WINDOWS)
public class WinRegistryTest {

  private static final String PATH = "Software\\Visual Pinball\\Controller";

  @Test
  public void testReadsAreEmptyOutsideWindows() {
    assertTrue(WinRegistry.getCurrentUserKeys(PATH).isEmpty());
    assertTrue(WinRegistry.getLocalMachineKeys(PATH).isEmpty());
    assertTrue(WinRegistry.getClassesValues(PATH).isEmpty());
    assertTrue(WinRegistry.getCurrentUserValues(PATH).isEmpty());
    assertFalse(WinRegistry.hasCurrentUserValues(PATH));
    assertNull(WinRegistry.readUserValue(PATH, "key"));
  }

  @Test
  public void testWritesAreNoOpsOutsideWindows() {
    assertDoesNotThrow(() -> {
      WinRegistry.setUserIntValue(PATH, "key", 1);
      WinRegistry.setUserValue(PATH, "key", "value");
      WinRegistry.createUserKey(PATH);
      WinRegistry.deleteUserValue(PATH, "key");
      WinRegistry.deleteUserKey(PATH);
    });
  }
}
