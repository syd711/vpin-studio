package de.mephisto.vpin.server.backups;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.UniversalUploadService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;

public class BackupInstallerJob implements Job {
  private final BackupDescriptor backupDescriptor;
  private final UniversalUploadService universalUploadService;
  private final GameService gameService;
  private final GameEmulator gameEmulator;
  private final CardService cardService;

  public BackupInstallerJob(@NonNull BackupDescriptor backupDescriptor,
                            @NonNull UniversalUploadService universalUploadService,
                            @NonNull GameService gameService,
                            @NonNull GameEmulator gameEmulator,
                            @NonNull CardService cardService) {
    this.backupDescriptor = backupDescriptor;
    this.universalUploadService = universalUploadService;
    this.gameService = gameService;
    this.gameEmulator = gameEmulator;
    this.cardService = cardService;
  }

  /**
   * @return
   */
  @Override
  public void execute(JobDescriptor result) {
    Thread.currentThread().setName("Backup Installer for " + backupDescriptor.getFilename());

    try {
      UploadDescriptor uploadDescriptor = new UploadDescriptor();
      uploadDescriptor.setOriginalUploadFileName(backupDescriptor.getFilename());
      uploadDescriptor.setEmulatorId(gameEmulator.getId());
      uploadDescriptor.setFolderBasedImport(backupDescriptor.getTableDetails().getGameFileName().contains(File.separator));
      uploadDescriptor.setTempFilename(backupDescriptor.getAbsoluteFileName());
      uploadDescriptor.setBackupRestoreMode(true);
      uploadDescriptor.setAutoFill(false);
      uploadDescriptor.setAsync(false);
      uploadDescriptor.setUploadType(UploadType.uploadAndImport);

      universalUploadService.process(uploadDescriptor);

      Game game = gameService.getGame(uploadDescriptor.getGameId());
      if (game != null) {
        cardService.generateHighscoreCard(game);
      }
    }
    catch (Exception e) {
      result.setError("Failed to restore backup: " + e.getMessage());
    }
  }
}
