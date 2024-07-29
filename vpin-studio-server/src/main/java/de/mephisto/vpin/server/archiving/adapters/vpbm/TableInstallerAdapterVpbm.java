package de.mephisto.vpin.server.archiving.adapters.vpbm;

import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.server.archiving.ArchiveDescriptor;
import de.mephisto.vpin.server.archiving.adapters.TableInstallerAdapter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class TableInstallerAdapterVpbm implements TableInstallerAdapter, Job {
  private final static Logger LOG = LoggerFactory.getLogger(TableInstallerAdapterVpbm.class);

  private final GameService gameService;
  private final VpbmService vpbmService;
  private final ArchiveDescriptor archiveDescriptor;
  private final GameEmulator emulator;

  private File archiveFile;
  private double progress;
  private String status;

  public TableInstallerAdapterVpbm(@NonNull GameService gameService,
                                   @NonNull VpbmService vpbmService,
                                   @NonNull ArchiveDescriptor archiveDescriptor,
                                   @NonNull GameEmulator emulator) {
    this.gameService = gameService;
    this.vpbmService = vpbmService;
    this.archiveDescriptor = archiveDescriptor;
    this.emulator = emulator;
  }

  @Override
  public double getProgress() {
    return progress;
  }

  @Override
  public String getStatus() {
    return status;
  }

  @Override
  public JobExecutionResult execute() {
    return installTable();
  }

  @NonNull
  @Override
  public ArchiveDescriptor getArchiveDescriptor() {
    return archiveDescriptor;
  }

  @NonNull
  @Override
  public JobExecutionResult installTable() {
    JobExecutionResult result = new JobExecutionResult();
    try {
      LOG.info("Starting import of " + archiveDescriptor.getFilename());

      archiveFile = new File(archiveDescriptor.getSource().getLocation(), archiveDescriptor.getFilename());
      if (!archiveFile.exists()) {
        LOG.error("Failed to import " + archiveFile.getAbsolutePath() + ", file does not exist.");
        return null;
      }

      status = "Extracting " + archiveFile.getAbsolutePath();
      String msg = vpbmService.restore(archiveFile.getAbsolutePath());
      if(msg.contains("ERROR")) {
        result.setError(msg);
        return result;
      }

      String baseName = FilenameUtils.getBaseName(archiveDescriptor.getFilename());
      Game game = gameService.getGameByName(emulator.getId(), baseName);
      LOG.info("Executing final table scan for " + game.getGameDisplayName());
      gameService.scanGame(game.getId());

      this.progress = 100;
      result.setGameId(game.getId());
    } catch (Exception e) {
      LOG.error("Import of \"" + archiveFile.getName() + "\" failed: " + e.getMessage(), e);
      result.setError("Import of \"" + archiveFile.getName() + "\" failed: " + e.getMessage());
    }
    return result;
  }
}
