package de.mephisto.vpin.restclient.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Outside Windows there is no cmd.exe: prepending it made every command fail with an IOException.
 * The command's side effect is checked rather than its exit code or output, which executeCommand()
 * does not reliably report.
 */
@DisabledOnOs(OS.WINDOWS)
public class SystemCommandExecutorTest {

  @TempDir
  File tempDir;

  @Test
  public void testCommandRunsDirectlyOutsideWindows() throws Exception {
    File marker = new File(tempDir, "default.txt");
    // prependCmd defaults to true
    new SystemCommandExecutor(Arrays.asList("touch", marker.getAbsolutePath())).executeCommand();
    assertTrue(marker.exists());
  }

  @Test
  public void testExplicitPrependCmdIgnoredOutsideWindows() throws Exception {
    File marker = new File(tempDir, "explicit.txt");
    new SystemCommandExecutor(Arrays.asList("touch", marker.getAbsolutePath()), true).executeCommand();
    assertTrue(marker.exists());
  }
}
