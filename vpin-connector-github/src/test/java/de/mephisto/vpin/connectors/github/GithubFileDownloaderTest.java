package de.mephisto.vpin.connectors.github;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GithubFileDownloaderTest {

  private static final String REPO_URL = "https://github.com/jsm174/vpx-standalone-scripts";
  private static final File TARGET_DIR = new File("resources/vpxscripts");

  @BeforeAll
  static void setup() {
    TARGET_DIR.mkdirs();
  }

  @AfterAll
  static void tearDown() throws IOException {
    FileUtils.deleteDirectory(TARGET_DIR);
  }

  @Test
  void testDownloadVbsFiles() throws IOException {
    GithubFileDownloader downloader = new GithubFileDownloader(REPO_URL);
    List<File> files = downloader.downloadVbsFiles(TARGET_DIR);

    assertFalse(files.isEmpty(), "Should have extracted at least one .vbs file");

    for (File file : files) {
      assertTrue(file.exists(), "Extracted file should exist: " + file);
      assertEquals(TARGET_DIR.getCanonicalPath(), file.getParentFile().getCanonicalPath(),
          "File should be directly in targetDir, not in a subfolder");
      assertTrue(file.getName().endsWith(".vbs"), "File should be a .vbs file: " + file.getName());
//      assertFalse(file.getName().matches("^\\d+.*"), "Filename should not start with a number: " + file.getName());
      assertTrue(file.length() > 0, "Extracted file should not be empty: " + file.getAbsolutePath());
    }
  }

  @Test
  void testStripLeadingNumber() {
    GithubFileDownloader downloader = new GithubFileDownloader(REPO_URL);

    assertEquals("foo.vbs", downloader.stripLeadingNumber("01_foo.vbs"));
    assertEquals("foo.vbs", downloader.stripLeadingNumber("1_foo.vbs"));
    assertEquals("foo.vbs", downloader.stripLeadingNumber("123-foo.vbs"));
    assertEquals("foo.vbs", downloader.stripLeadingNumber("foo.vbs")); // no prefix → unchanged
  }
}
