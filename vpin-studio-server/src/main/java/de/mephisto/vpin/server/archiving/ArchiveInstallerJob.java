package de.mephisto.vpin.server.archiving;

import de.mephisto.vpin.commons.ArchiveSourceType;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.server.archiving.adapters.TableInstallerAdapter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

public class ArchiveInstallerJob implements Job {
  private final TableInstallerAdapter tableInstallerAdapter;
  private final ArchiveDescriptor archiveDescriptor;
  private final CardService cardService;
  private final GameService gameService;
  private final ArchiveService archiveService;
  private CopyArchiveToRepositoryJob downloadJob;

  public ArchiveInstallerJob(@NonNull TableInstallerAdapter tableInstallerAdapter,
                             @NonNull ArchiveDescriptor archiveDescriptor,
                             @NonNull CardService cardService,
                             @NonNull GameService gameService,
                             @NonNull ArchiveService archiveService) {
    this.tableInstallerAdapter = tableInstallerAdapter;
    this.archiveDescriptor = archiveDescriptor;
    this.cardService = cardService;
    this.gameService = gameService;
    this.archiveService = archiveService;
  }

  /**
   * @return
   */
  @Override
  public void execute(JobDescriptor result) {
    Thread.currentThread().setName("Archive Installer for " + archiveDescriptor.getFilename());

    try {
      ArchiveSource source = archiveDescriptor.getSource();
      if (source.getType().equals(ArchiveSourceType.Http.name())) {
        downloadJob = new CopyArchiveToRepositoryJob(archiveService, archiveDescriptor, false);
        downloadJob.execute(result);
        this.downloadJob = null;
        tableInstallerAdapter.getArchiveDescriptor().setFilename(archiveDescriptor.getFilename());
        tableInstallerAdapter.getArchiveDescriptor().setSource(archiveService.getDefaultArchiveSourceAdapter().getArchiveSource());
      }

      tableInstallerAdapter.installTable(result);
      if (StringUtils.isEmpty(result.getError())) {
        Game game = gameService.getGame(result.getGameId());
        cardService.generateCard(game);
      }
    } catch (Exception e) {
      result.setError("Failed to install archive: " + e.getMessage());
    }
  }
}
