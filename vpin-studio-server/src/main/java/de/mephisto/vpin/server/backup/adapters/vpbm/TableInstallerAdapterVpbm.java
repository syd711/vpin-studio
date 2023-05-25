package de.mephisto.vpin.server.backup.adapters.vpbm;

import de.mephisto.vpin.restclient.Job;
import de.mephisto.vpin.restclient.TableInstallationResult;
import de.mephisto.vpin.server.backup.ArchiveDescriptor;
import de.mephisto.vpin.server.backup.adapters.TableInstallerAdapter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class TableInstallerAdapterVpbm implements TableInstallerAdapter, Job {
  private final static Logger LOG = LoggerFactory.getLogger(TableInstallerAdapterVpbm.class);

  private final GameService gameService;
  private final VpbmService vpbmService;
  private final ArchiveDescriptor archiveDescriptor;

  private File archiveFile;
  private double progress;
  private String status;

  public TableInstallerAdapterVpbm(@NonNull GameService gameService,
                                   @NonNull VpbmService vpbmService,
                                   @NonNull ArchiveDescriptor archiveDescriptor) {
    this.gameService = gameService;
    this.vpbmService = vpbmService;
    this.archiveDescriptor = archiveDescriptor;
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
  public boolean execute() {
    return installTable() != null;
  }

  @NotNull
  @Override
  public ArchiveDescriptor getArchiveDescriptor() {
    return archiveDescriptor;
  }

  @Nullable
  @Override
  public TableInstallationResult installTable() {
    TableInstallationResult result = new TableInstallationResult();
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
      Game game = gameService.getGameByName(baseName);
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
