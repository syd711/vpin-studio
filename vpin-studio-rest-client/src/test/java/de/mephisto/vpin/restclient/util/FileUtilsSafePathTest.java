package de.mephisto.vpin.restclient.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileUtilsSafePathTest {

  @Test
  public void testIsSafeRelativePath() {
    assertTrue(FileUtils.isSafeRelativePath("MyTable.FlexDMD"));
    assertTrue(FileUtils.isSafeRelativePath("DMDs\\MyTable"));
    assertTrue(FileUtils.isSafeRelativePath("./MyTable"));
    assertFalse(FileUtils.isSafeRelativePath(null));
    assertFalse(FileUtils.isSafeRelativePath(""));
    assertFalse(FileUtils.isSafeRelativePath("."));
    assertFalse(FileUtils.isSafeRelativePath(".\\"));
    assertFalse(FileUtils.isSafeRelativePath(".."));
    assertFalse(FileUtils.isSafeRelativePath("..\\Other"));
    assertFalse(FileUtils.isSafeRelativePath("a/../.."));
    assertFalse(FileUtils.isSafeRelativePath("C:\\vPinball"));
    assertFalse(FileUtils.isSafeRelativePath("/tmp"));
  }

  @Test
  public void testIsStrictlyBelow(@TempDir Path tempDir) {
    File root = tempDir.toFile();
    assertTrue(FileUtils.isStrictlyBelow(root, new File(root, "sub")));
    assertTrue(FileUtils.isStrictlyBelow(root, new File(root, "sub/deeper")));
    assertFalse(FileUtils.isStrictlyBelow(root, root));
    assertFalse(FileUtils.isStrictlyBelow(root, new File(root, ".")));
    assertFalse(FileUtils.isStrictlyBelow(root, new File(root, "..")));
    assertFalse(FileUtils.isStrictlyBelow(root, new File(root, "../sibling")));
    assertFalse(FileUtils.isStrictlyBelow(null, root));
    assertFalse(FileUtils.isStrictlyBelow(root, null));
  }
}
