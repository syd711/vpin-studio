package de.mephisto.vpin.server.backup;

import de.mephisto.vpin.commons.ArchiveSourceType;
import de.mephisto.vpin.restclient.Job;
import de.mephisto.vpin.restclient.JobExecutionResult;
import de.mephisto.vpin.restclient.JobExecutionResultFactory;
import de.mephisto.vpin.restclient.descriptors.ArchiveRestoreDescriptor;
import de.mephisto.vpin.server.backup.adapters.TableInstallerAdapter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.popper.PinUPConnector;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;

public class ArchiveInstallerJob implements Job {
  private final TableInstallerAdapter tableInstallerAdapter;
  private final ArchiveDescriptor archiveDescriptor;
  private final PinUPConnector pinUPConnector;
  private final CardService cardService;
  private final GameService gameService;
  private final ArchiveService archiveService;
  private final ArchiveRestoreDescriptor installDescriptor;
  private CopyArchiveToRepositoryJob downloadJob;

  public ArchiveInstallerJob(@NonNull TableInstallerAdapter tableInstallerAdapter,
                             @NonNull ArchiveDescriptor archiveDescriptor,
                             @NonNull PinUPConnector pinUPConnector,
                             @NonNull CardService cardService,
                             @NonNull GameService gameService,
                             @NonNull ArchiveService archiveService,
                             @NonNull ArchiveRestoreDescriptor installDescriptor) {
    this.tableInstallerAdapter = tableInstallerAdapter;
    this.archiveDescriptor = archiveDescriptor;
    this.pinUPConnector = pinUPConnector;
    this.cardService = cardService;
    this.gameService = gameService;
    this.archiveService = archiveService;
    this.installDescriptor = installDescriptor;
  }

  @Override
  public double getProgress() {
    if (this.downloadJob != null) {
      return this.downloadJob.getProgress();
    }
    return tableInstallerAdapter.getProgress();
  }

  @Override
  public String getStatus() {
    if (this.downloadJob != null) {
      return this.downloadJob.getStatus();
    }
    return tableInstallerAdapter.getStatus();
  }

  /**
   *
   * @return
   */
  @Override
  public JobExecutionResult execute() {
    Thread.currentThread().setName("Archive Installer for " + archiveDescriptor.getFilename());

    JobExecutionResult result = null;
    try {
      ArchiveSource source = archiveDescriptor.getSource();
      if (source.getType().equals(ArchiveSourceType.Http.name())) {
        downloadJob = new CopyArchiveToRepositoryJob(archiveService, archiveDescriptor, false);
        downloadJob.execute();
        this.downloadJob = null;
        tableInstallerAdapter.getArchiveDescriptor().setFilename(archiveDescriptor.getFilename());
        tableInstallerAdapter.getArchiveDescriptor().setSource(archiveService.getDefaultArchiveSourceAdapter().getArchiveSource());
      }

      result = tableInstallerAdapter.installTable();
      if (StringUtils.isEmpty(result.getError())) {
        Game game = gameService.getGame(result.getGameId());
        try {
          cardService.generateCard(game, false);
        } catch (Exception e) {
          //ignore
        }

        if (installDescriptor.getPlaylistId() != -1) {
          pinUPConnector.addToPlaylist(game.getId(), installDescriptor.getPlaylistId());
        }
      }
      return result;
    } catch (Exception e) {
      if (result != null) {
        return result;
      }
      return JobExecutionResultFactory.create("Failed to install archive: " + e.getMessage());
    }
  }
}
