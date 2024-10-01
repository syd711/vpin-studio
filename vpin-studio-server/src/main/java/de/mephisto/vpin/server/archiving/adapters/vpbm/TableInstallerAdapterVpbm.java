package de.mephisto.vpin.server.archiving.adapters.vpbm;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
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
  public void execute(JobDescriptor result) {
    installTable(result);
  }

  @NonNull
  @Override
  public ArchiveDescriptor getArchiveDescriptor() {
    return archiveDescriptor;
  }

  @NonNull
  @Override
  public void installTable(JobDescriptor result) {
    try {
      LOG.info("Starting import of " + archiveDescriptor.getFilename());

      archiveFile = new File(archiveDescriptor.getSource().getLocation(), archiveDescriptor.getFilename());
      if (!archiveFile.exists()) {
        LOG.error("Failed to import " + archiveFile.getAbsolutePath() + ", file does not exist.");
        result.setError("Failed to import " + archiveFile.getAbsolutePath() + ", file does not exist.");
        return;
      }

      result.setStatus("Extracting " + archiveFile.getAbsolutePath());
      String msg = vpbmService.restore(archiveFile.getAbsolutePath());
      if (msg.contains("ERROR") || msg.contains("bad password")) {
        result.setError(msg);
        return;
      }

      String baseName = FilenameUtils.getBaseName(archiveDescriptor.getFilename());
      Game game = gameService.getGameByName(emulator.getId(), baseName);
      if (game == null) {
        result.setError("Imported table \"" + baseName + "\" not found. Check the VPBM logs for details.");
        return;
      }

      LOG.info("Executing final table scan for " + game.getGameDisplayName());
      gameService.scanGame(game.getId());

      result.setProgress(1);
      result.setGameId(game.getId());
    }
    catch (Exception e) {
      LOG.error("Import of \"" + archiveFile.getName() + "\" failed: " + e.getMessage(), e);
      result.setError("Import of \"" + archiveFile.getName() + "\" failed: " + e.getMessage());
    }
  }
}
