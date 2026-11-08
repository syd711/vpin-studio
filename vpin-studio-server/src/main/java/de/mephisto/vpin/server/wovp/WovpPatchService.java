package de.mephisto.vpin.server.wovp;

import de.mephisto.vpin.connectors.wovp.patcher.WovpPatchStatus;
import de.mephisto.vpin.connectors.wovp.patcher.WovpTablePatcher;
import de.mephisto.vpin.restclient.wovp.WovpPatchResult;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.vpx.VPXUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Applies the WoVP telemetry patch to VPX tables, the Java port of the "WoVP Challenges Patcher".
 * <p>
 * The table is extracted with vpxtool into a temporary folder, patched by {@link WovpTablePatcher},
 * assembled again and finally copied over the original table file.
 * The same vpxtool is used for extracting and assembling, since the extracted formats differ between versions.
 */
@Service
public class WovpPatchService {
  private final static Logger LOG = LoggerFactory.getLogger(WovpPatchService.class);

  private static final String TABLE_NAME = "table";

  @Autowired
  private GameService gameService;

  /**
   * @param backup creates a "&lt;table&gt;.vpx.bak" copy of the original table first. An existing backup is never overwritten.
   */
  @NonNull
  public WovpPatchResult patch(int gameId, boolean backup) {
    return run(gameId, backup, true);
  }

  @NonNull
  public WovpPatchResult unpatch(int gameId, boolean backup) {
    return run(gameId, backup, false);
  }

  /**
   * Fast check based on the script embedded in the table, no extraction needed.
   */
  public boolean isPatched(int gameId) {
    Game game = gameService.getGame(gameId);
    if (game != null && game.isVpxGame() && game.getGameFile().exists()) {
      return WovpTablePatcher.isPatched(VPXUtil.readScript(game.getGameFile()));
    }
    return false;
  }

  @NonNull
  private WovpPatchResult run(int gameId, boolean backup, boolean patch) {
    WovpPatchResult result = new WovpPatchResult(gameId, WovpPatchStatus.FAILED);
    Game game = gameService.getGame(gameId);
    if (game == null) {
      result.setMessage("No table found for id " + gameId);
      return result;
    }
    if (!game.isVpxGame()) {
      result.setMessage("\"" + game.getGameDisplayName() + "\" is not a VPX table.");
      return result;
    }
    File gameFile = game.getGameFile();
    if (!gameFile.exists()) {
      result.setMessage("Table file " + gameFile.getAbsolutePath() + " not found.");
      return result;
    }

    long start = System.currentTimeMillis();
    File tempDir = null;
    try {
      LOG.info("WoVP {} of \"{}\" ({})", patch ? "patch" : "unpatch", game.getGameDisplayName(), gameFile.getAbsolutePath());
      tempDir = Files.createTempDirectory("wovp_patch_").toFile();

      // older vpxtool versions do not support an output folder and extract next to the table file,
      // so the table is copied into the temp folder first.
      File tempTable = new File(tempDir, TABLE_NAME + ".vpx");
      Files.copy(gameFile.toPath(), tempTable.toPath());
      File extractDir = new File(tempDir, TABLE_NAME);

      // 1. extract
      String err = VPXUtil.runVpxTool("extract", "-f", tempTable);
      if (!new File(extractDir, "gameitems.json").isFile()) {
        throw new IOException("Extraction failed: " + StringUtils.defaultIfBlank(err, "no gameitems.json found"));
      }

      // 2. patch
      WovpPatchStatus status = patch ? WovpTablePatcher.patch(extractDir) : WovpTablePatcher.unpatch(extractDir);
      result.setStatus(status);
      if (status == WovpPatchStatus.ALREADY_PATCHED || status == WovpPatchStatus.NOT_PATCHED) {
        LOG.info("WoVP {} of \"{}\" skipped: {}", patch ? "patch" : "unpatch", game.getGameDisplayName(), status);
        return result;
      }

      // 3. assemble into a temporary file, the original is only touched once everything succeeded
      File patchedTable = new File(tempDir, "patched.vpx");
      err = VPXUtil.runVpxTool("assemble", "-f", extractDir, patchedTable);
      if (!patchedTable.isFile() || patchedTable.length() == 0) {
        throw new IOException("Assembling failed: " + StringUtils.defaultIfBlank(err, "no table file created"));
      }

      // 4. finalize
      if (backup) {
        File backupFile = new File(gameFile.getParentFile(), gameFile.getName() + ".bak");
        if (backupFile.exists()) {
          LOG.info("Existing backup {} has been kept.", backupFile.getAbsolutePath());
        }
        else {
          Files.copy(gameFile.toPath(), backupFile.toPath());
          LOG.info("Created backup {}", backupFile.getAbsolutePath());
        }
        result.setBackupFile(backupFile.getAbsolutePath());
      }
      Files.copy(patchedTable.toPath(), gameFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      gameService.reload(gameId);

      LOG.info("WoVP {} of \"{}\" finished with {} in {}ms.", patch ? "patch" : "unpatch", game.getGameDisplayName(), status, System.currentTimeMillis() - start);
      return result;
    }
    catch (Exception e) {
      LOG.error("WoVP {} of \"{}\" failed: {}", patch ? "patch" : "unpatch", game.getGameDisplayName(), e.getMessage(), e);
      result.setStatus(WovpPatchStatus.FAILED);
      result.setMessage(e.getMessage());
      return result;
    }
    finally {
      if (tempDir != null) {
        FileUtils.deleteQuietly(tempDir);
      }
    }
  }
}
