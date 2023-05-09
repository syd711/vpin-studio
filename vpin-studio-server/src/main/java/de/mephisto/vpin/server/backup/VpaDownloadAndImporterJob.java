package de.mephisto.vpin.server.backup;

import de.mephisto.vpin.restclient.VpaImportDescriptor;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.cards.CardService;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class VpaDownloadAndImporterJob extends VpaImporterJob {
  private final static Logger LOG = LoggerFactory.getLogger(VpaService.class);

  private final VpaDescriptor vpaDescriptor;
  private final VpaService vpaService;
  private boolean downloadFinished;

  private final VpaDownloadToRepositoryJob downloadToRepositoryJob;

  public VpaDownloadAndImporterJob(@NonNull VpaDescriptor vpaDescriptor,
                                   @NonNull VpaImportDescriptor descriptor,
                                   @NonNull File vpaFile,
                                   @NonNull PinUPConnector connector,
                                   @NonNull SystemService systemService,
                                   @NonNull HighscoreService highscoreService,
                                   @NonNull VpaService vpaService,
                                   @NonNull GameService gameService,
                                   @NonNull CardService cardService) {
    super(descriptor, vpaFile, connector, systemService, highscoreService, gameService, cardService);
    this.vpaDescriptor = vpaDescriptor;
    this.vpaService = vpaService;

    this.downloadToRepositoryJob = new VpaDownloadToRepositoryJob(vpaService, systemService, vpaDescriptor);
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
    this.vpaFile = downloadToRepositoryJob.getDownloadedFile();
    downloadFinished = true;

    VpaSourceAdapter vpaSourceAdapter = vpaService.getVpaSourceAdapter(vpaDescriptor.getSource().getId());
    vpaSourceAdapter.invalidate();

    if (descriptor.isInstall()) {
      return super.execute();
    }
    return true;
  }
}
