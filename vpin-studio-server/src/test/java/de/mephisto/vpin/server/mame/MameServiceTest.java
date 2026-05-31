package de.mephisto.vpin.server.mame;

import de.mephisto.vpin.commons.SystemInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MameServiceTest {

  @InjectMocks
  private MameService mameService;

  private String originalResources;

  @BeforeEach
  void saveOriginalResources() {
    originalResources = SystemInfo.RESOURCES;
  }

  @AfterEach
  void restoreResources() {
    SystemInfo.RESOURCES = originalResources;
  }

  // ---- resolveMAMENameFor ----

  @Test
  void resolveMAMENameFor_returnsGameName_whenFoundInGameList(@TempDir Path tempDir) throws IOException {
    SystemInfo.RESOURCES = tempDir.toAbsolutePath() + "/";
    Files.writeString(tempDir.resolve("mame-gamelist.txt"),
        "funhouse \"Fun House (Williams 1990)\"\n");

    assertEquals("Fun House (Williams 1990)", mameService.resolveMAMENameFor("funhouse"));
  }

  @Test
  void resolveMAMENameFor_isCaseInsensitive(@TempDir Path tempDir) throws IOException {
    SystemInfo.RESOURCES = tempDir.toAbsolutePath() + "/";
    Files.writeString(tempDir.resolve("mame-gamelist.txt"),
        "FUNHOUSE \"Fun House (Williams 1990)\"\n");

    assertEquals("Fun House (Williams 1990)", mameService.resolveMAMENameFor("funhouse"));
  }

  @Test
  void resolveMAMENameFor_returnsBaseName_whenGameNotFound(@TempDir Path tempDir) throws IOException {
    SystemInfo.RESOURCES = tempDir.toAbsolutePath() + "/";
    Files.writeString(tempDir.resolve("mame-gamelist.txt"),
        "pacman \"Pac-Man\"\n");

    assertEquals("funhouse", mameService.resolveMAMENameFor("funhouse"));
  }

  @Test
  void resolveMAMENameFor_returnsBaseName_whenGameListDoesNotExist(@TempDir Path tempDir) {
    SystemInfo.RESOURCES = tempDir.toAbsolutePath() + "/";
    // no mame-gamelist.txt created

    assertEquals("funhouse", mameService.resolveMAMENameFor("funhouse"));
  }

  @Test
  void resolveMAMENameFor_findsEntryAmongMultipleLines(@TempDir Path tempDir) throws IOException {
    SystemInfo.RESOURCES = tempDir.toAbsolutePath() + "/";
    Files.writeString(tempDir.resolve("mame-gamelist.txt"),
        "pacman \"Pac-Man\"\n" +
        "mspacman \"Ms. Pac-Man\"\n" +
        "funhouse \"Fun House (Williams 1990)\"\n");

    assertEquals("Ms. Pac-Man", mameService.resolveMAMENameFor("mspacman"));
  }
}
