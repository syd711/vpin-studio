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

public class ArchiveInstallerJob implements Job {
  private final ArchiveDescriptor archiveDescriptor;
  private final UniversalUploadService universalUploadService;
  private final GameService gameService;
  private final GameEmulator gameEmulator;
  private final CardService cardService;

  public ArchiveInstallerJob(@NonNull ArchiveDescriptor archiveDescriptor,
                             @NonNull UniversalUploadService universalUploadService,
                             @NonNull GameService gameService,
                             @NonNull GameEmulator gameEmulator,
                             @NonNull CardService cardService) {
    this.archiveDescriptor = archiveDescriptor;
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
    Thread.currentThread().setName("Backup Installer for " + archiveDescriptor.getFilename());

    try {
      UploadDescriptor uploadDescriptor = new UploadDescriptor();
      uploadDescriptor.setOriginalUploadFileName(archiveDescriptor.getFilename());
      uploadDescriptor.setEmulatorId(gameEmulator.getId());
      uploadDescriptor.setTempFilename(archiveDescriptor.getAbsoluteFileName());
      uploadDescriptor.setBackupRestoreMode(true);
      uploadDescriptor.setAutoFill(false);
      uploadDescriptor.setAsync(false);
      uploadDescriptor.setUploadType(UploadType.uploadAndImport);

      universalUploadService.process(uploadDescriptor);

      Game game = gameService.getGame(uploadDescriptor.getGameId());
      if (game != null) {
        cardService.generateCard(game);
      }
    }
    catch (Exception e) {
      result.setError("Failed to restore backup: " + e.getMessage());
    }
  }
}
