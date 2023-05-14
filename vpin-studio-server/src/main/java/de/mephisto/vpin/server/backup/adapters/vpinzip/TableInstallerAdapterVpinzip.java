package de.mephisto.vpin.server.backup.adapters.vpinzip;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.restclient.Job;
import de.mephisto.vpin.server.backup.ArchiveDescriptor;
import de.mephisto.vpin.server.backup.adapters.TableInstallerAdapter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.system.SystemService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class TableInstallerAdapterVpinzip implements TableInstallerAdapter, Job {
  private final static Logger LOG = LoggerFactory.getLogger(TableInstallerAdapterVpinzip.class);

  private final GameService gameService;
  private final ArchiveDescriptor archiveDescriptor;

  private File archiveFile;
  private double progress;
  private String status;

  public TableInstallerAdapterVpinzip(@NonNull GameService gameService,
                                      @NonNull ArchiveDescriptor archiveDescriptor) {
    this.gameService = gameService;
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

  @Nullable
  @Override
  public Game installTable() {
    try {
      LOG.info("Starting import of " + archiveDescriptor.getFilename());

      archiveFile = new File(archiveDescriptor.getSource().getLocation(), archiveDescriptor.getFilename());
      if (!archiveFile.exists()) {
        LOG.error("Failed to import " + archiveFile.getAbsolutePath() + ", file does not exist.");
        return null;
      }

      status = "Extracting " + archiveFile.getAbsolutePath();

      String baseName = FilenameUtils.getBaseName(archiveDescriptor.getFilename());

      //install is the default
      List<String> commands = Arrays.asList("vPinBackupManager.exe", "-i", "\"" + archiveFile.getAbsolutePath() + "\"");

      //check if it can be simply restored
      Game gameByFilename = gameService.getGameByName(baseName);
      if (gameByFilename != null) {
        commands = Arrays.asList("vPinBackupManager.exe", "-r", String.valueOf(gameByFilename.getId()));
      }

      LOG.info("Executing restore command: " + String.join(" ", commands));
      File dir = new File(SystemService.RESOURCES, VpinzipArchiveSource.FOLDER_NAME);
      SystemCommandExecutor executor = new SystemCommandExecutor(commands);
      executor.setDir(dir);
      StringBuilder standardOutputFromCommand = executor.getStandardOutputFromCommand();
      StringBuilder standardErrorFromCommand = executor.getStandardErrorFromCommand();
      if (!StringUtils.isEmpty(standardErrorFromCommand.toString())) {
        LOG.error("Vpinzip Command Error:\n" + standardErrorFromCommand);
      }
      if (!StringUtils.isEmpty(standardOutputFromCommand.toString())) {
        LOG.info("Vpinzip Command StdOut:\n" + standardOutputFromCommand);
      }
      executor.executeCommand();

      Game game = gameService.getGameByName(baseName);
      LOG.info("Executing final table scan for " + game.getGameDisplayName());
      gameService.scanGame(game.getId());

      this.progress = 100;
      return game;
    } catch (Exception e) {
      LOG.error("Import of \"" + archiveFile.getName() + "\" failed: " + e.getMessage(), e);
      return null;
    }
  }
}
