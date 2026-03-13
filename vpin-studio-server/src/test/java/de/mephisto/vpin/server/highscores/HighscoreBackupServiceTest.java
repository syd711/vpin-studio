package de.mephisto.vpin.server.highscores;

import de.mephisto.vpin.restclient.highscores.HighscoreBackup;
import de.mephisto.vpin.server.AbstractVPinServerTest;
import de.mephisto.vpin.server.games.Game;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class HighscoreBackupServiceTest extends AbstractVPinServerTest {

  @Autowired
  private HighscoreBackupService highscoreBackupService;

  @BeforeAll
  public void setup() {
    setupSystem();
  }

  @Test
  public void testBackupAndGetBackups() {
    Game game = gameService.getGameByFilename(1, NVRAM_TABLE_NAME);
    assertNotNull(game);

    try {
      File backupFile = highscoreBackupService.backup(game);
      // backup may be null if no highscore file exists for this game
      if (backupFile != null) {
        assertTrue(backupFile.exists());

        String rom = game.getRom();
        assertNotNull(rom);

        List<HighscoreBackup> backups = highscoreBackupService.getBackups(rom);
        assertNotNull(backups);
        assertFalse(backups.isEmpty());

        // cleanup
        highscoreBackupService.delete(rom, backupFile.getName());
      }
    }
    catch (Exception e) {
      fail("Backup test failed: " + e.getMessage());
    }
  }

  @Test
  public void testGetBackupsEmpty() {
    List<HighscoreBackup> backups = highscoreBackupService.getBackups("nonexistent_rom");
    assertNotNull(backups);
    assertTrue(backups.isEmpty());
  }

  @Test
  public void testBackupEmTable() {
    Game game = gameService.getGameByFilename(1, EM_TABLE_NAME);
    assertNotNull(game);

    try {
      File backupFile = highscoreBackupService.backup(game);
      if (backupFile != null) {
        assertTrue(backupFile.exists());
        String rom = game.getRom();
        if (rom != null) {
          highscoreBackupService.delete(rom, backupFile.getName());
        }
      }
    }
    catch (Exception e) {
      fail("EM backup test failed: " + e.getMessage());
    }
  }
}
