package de.mephisto.vpin.server.backup;

import de.mephisto.vpin.restclient.descriptors.ArchiveDownloadAndInstallDescriptor;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.File;

public class DownloadArchiveAndInstallJob extends ArchiveInstallerJob {
  private final ArchiveDescriptor archiveDescriptor;
  private final ArchiveService archiveService;

  private boolean downloadFinished;

  private final DownloadArchiveToRepositorySubJob downloadToRepositoryJob;
  private final ArchiveDownloadAndInstallDescriptor descriptor;

  public DownloadArchiveAndInstallJob(@NonNull ArchiveDescriptor archiveDescriptor,
                                      @NonNull ArchiveDownloadAndInstallDescriptor descriptor,
                                      @NonNull File vpaFile,
                                      @NonNull PinUPConnector connector,
                                      @NonNull SystemService systemService,
                                      @NonNull HighscoreService highscoreService,
                                      @NonNull ArchiveService archiveService,
                                      @NonNull GameService gameService,
                                      @NonNull CardService cardService) {
    super(descriptor, vpaFile, connector, systemService, highscoreService, gameService, cardService);
    this.archiveDescriptor = archiveDescriptor;
    this.archiveService = archiveService;
    this.descriptor = descriptor;

    this.downloadToRepositoryJob = new DownloadArchiveToRepositorySubJob(archiveService, systemService, archiveDescriptor);
  }

  @Override
  public double getProgress() {
    return this.downloadToRepositoryJob.getProgress() * (4 / 5d) + (super.getProgress() * (1 / 5d));
  }

  @Override
  public String getStatus() {
    if (!downloadFinished) {
      return downloadToRepositoryJob.getStatus();
    }
    return super.getStatus();
  }

  @Override
  public boolean execute() {
    downloadToRepositoryJob.execute();
    this.archiveFile = downloadToRepositoryJob.getDownloadedFile();
    downloadFinished = true;

    ArchiveSourceAdapter vpaSourceAdapter = archiveService.getArchiveSourceAdapter(archiveDescriptor.getSource().getId());
    vpaSourceAdapter.invalidate();

    if (descriptor.isInstall()) {
      return super.execute();
    }
    return true;
  }
}
