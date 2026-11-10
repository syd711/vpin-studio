package de.mephisto.vpin.ui.tables.dialogs.bulk;

import de.mephisto.vpin.commons.fx.ConfirmationResult;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetMetaData;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.JobType;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.util.MimeTypeUtil;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.dialogs.TableAssetManagerDialogController;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class BulkImportProgressModel extends ProgressModel<BulkImportItem> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final List<BulkImportItem> items;
  private final Iterator<BulkImportItem> iterator;
  private final Set<Integer> touchedGames = new HashSet<>();
  /**
   * Games whose only loading media is an imported landscape video, Popper needs a blank video after it.
   */
  private final Set<Integer> fullscreenLoadingGames = new LinkedHashSet<>();
  private final File moveTarget;
  private final BulkImportMoveStructure structure;
  @Nullable
  private final BulkImportMatcher matcher;
  private final double minScore;
  /**
   * The subfolder of the move target per game id, resolved on the first move for {@link BulkImportMoveStructure#TABLE_FOLDER}.
   */
  private Map<Integer, File> tableFolders;
  private int currentIndex = -1;
  private int imported = 0;
  private int skipped = 0;
  private int failed = 0;
  private int moved = 0;
  private int moveFailed = 0;

  /**
   * @param moveTarget    folder the successfully imported files are moved to, null to leave them in place
   * @param structure  where the files are placed below the move target
   * @param matcher    matcher of all tables, used to find the existing table subfolders of the move target
   * @param minScore   minimum match score of an existing subfolder
   */
  public BulkImportProgressModel(List<BulkImportItem> items, @Nullable File moveTarget, BulkImportMoveStructure structure,
                                 @Nullable BulkImportMatcher matcher, double minScore) {
    super(Messages.get("dialog.bulk_importer"));
    this.items = items;
    this.iterator = items.iterator();
    this.moveTarget = moveTarget;
    this.structure = structure;
    this.matcher = matcher;
    this.minScore = minScore;
  }

  @Override
  public int getMax() {
    return items.size();
  }

  @Override
  public BulkImportItem getNext() {
    currentIndex++;
    return iterator.next();
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public String nextToString(BulkImportItem item) {
    return Messages.get("msg.uploading", item.getFile().getName());
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, BulkImportItem item) {
    try {
      int gameId = item.gameProperty().get().getId();
      VPinScreen screen = item.screenProperty().get();
      BulkImportMode mode = item.modeProperty().get();
      // fetched for every file, so media imported earlier in the same run is taken into account
      if (mode == BulkImportMode.IF_MISSING && !Studio.client.getGameMediaService().getGameMedia(gameId).getMediaItems(screen).isEmpty()) {
        LOG.info("Skipped bulk import of {}, screen {} already has media", item.getFile().getName(), screen);
        skipped++;
        return;
      }
      JobDescriptor result = Studio.client.getGameMediaService().uploadMedia(item.getFile(), gameId, false, screen,
          mode != BulkImportMode.OVERWRITE, VPinScreen.Loading, percent -> progressResultModel.setProgress((currentIndex + percent) / items.size()));
      if (!StringUtils.isEmpty(result.getError())) {
        LOG.error("Bulk import of {} failed: {}", item.getFile().getName(), result.getError());
        failed++;
        Platform.runLater(() -> WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), result.getError()));
      }
      else {
        imported++;
        LOG.info("Bulk imported {} to game {}, screen {}, mode {}", item.getFile().getAbsolutePath(), gameId, screen, mode);
        touchedGames.add(gameId);
        if (VPinScreen.Loading.equals(screen)) {
          // a later loading video of the same table in this run may change the result
          if (isFullscreenLoadingVideo(gameId)) {
            fullscreenLoadingGames.add(gameId);
          }
          else {
            fullscreenLoadingGames.remove(gameId);
          }
        }
        progressResultModel.addProcessed();
        // only files that have been imported are moved
        if (moveTarget != null) {
          moveFile(item);
        }
      }
    }
    catch (Exception e) {
      failed++;
      LOG.error("Bulk import of {} failed: {}", item.getFile().getName(), e.getMessage(), e);
    }
  }

  private void moveFile(BulkImportItem item) {
    File source = item.getFile();
    File folder = moveTarget;
    if (structure == BulkImportMoveStructure.KEEP_STRUCTURE) {
      String parent = new File(item.getRelativePath()).getParent();
      if (parent != null) {
        folder = new File(moveTarget, parent);
      }
    }
    else if (structure == BulkImportMoveStructure.TABLE_FOLDER) {
      folder = getTableFolder(item.gameProperty().get());
    }
    try {
      Files.createDirectories(folder.toPath());
      File target = uniqueTarget(folder, source.getName());
      Files.move(source.toPath(), target.toPath());
      // double check the result, a move across drives is a copy plus delete
      if (target.isFile() && !source.exists()) {
        moved++;
        LOG.info("Moved imported file {} to {}", source.getAbsolutePath(), target.getAbsolutePath());
      }
      else {
        moveFailed++;
        LOG.error("Move of imported file {} to {} incomplete: target exists={}, source still exists={}",
            source.getAbsolutePath(), target.getAbsolutePath(), target.isFile(), source.exists());
      }
    }
    catch (IOException e) {
      moveFailed++;
      LOG.error("Failed to move imported file {} to {}: {}", source.getAbsolutePath(), folder.getAbsolutePath(), e.getMessage(), e);
    }
  }

  /**
   * The existing subfolder of the move target that matches the table, or a new one named after the table.
   */
  private File getTableFolder(GameRepresentation game) {
    if (tableFolders == null) {
      tableFolders = findTableFolders();
    }
    return tableFolders.computeIfAbsent(game.getId(), id -> {
      File folder = new File(moveTarget, folderName(game));
      LOG.info("No existing folder found for table \"{}\", using \"{}\"", game.getGameDisplayName(), folder.getAbsolutePath());
      return folder;
    });
  }

  /**
   * Matches the existing subfolders of the move target against the tables of the import. A folder is only used for
   * a table when that table is the best match of the folder among all tables and the score is high enough.
   */
  private Map<Integer, File> findTableFolders() {
    Map<Integer, File> result = new HashMap<>();
    File[] folders = moveTarget.listFiles(File::isDirectory);
    if (folders == null || matcher == null) {
      return result;
    }
    Set<Integer> gameIds = items.stream().map(i -> i.gameProperty().get().getId()).collect(Collectors.toSet());
    Map<Integer, Double> scores = new HashMap<>();
    for (File folder : folders) {
      // the matcher looks at the parent folder of a file, so a dummy file inside the folder is matched
      List<BulkImportMatcher.Match> matches = matcher.suggest(new File(folder, "_"), moveTarget, BulkImportMatcher.MatchMode.FOLDER, 1);
      if (matches.isEmpty()) {
        continue;
      }
      BulkImportMatcher.Match match = matches.get(0);
      int gameId = match.game.getId();
      if (match.score >= minScore && gameIds.contains(gameId) && match.score > scores.getOrDefault(gameId, 0d)) {
        scores.put(gameId, match.score);
        result.put(gameId, folder);
      }
    }
    result.forEach((gameId, folder) -> LOG.info("Matched existing folder \"{}\" to game {} ({}%)",
        folder.getAbsolutePath(), gameId, Math.round(scores.get(gameId) * 100)));
    return result;
  }

  /**
   * The table name as folder name, without the characters that are not allowed in file names.
   */
  static String folderName(GameRepresentation game) {
    String name = StringUtils.defaultIfBlank(game.getGameDisplayName(), FilenameUtils.getBaseName(game.getGameFileName()));
    name = StringUtils.defaultString(name).replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").trim();
    // Windows does not allow names ending with a dot or space
    name = StringUtils.stripEnd(name, ". ");
    return StringUtils.isBlank(name) ? "Table " + game.getId() : name;
  }

  /**
   * Files with the same name are not overwritten, a counter is added to the name instead.
   */
  private static File uniqueTarget(File folder, String name) {
    File target = new File(folder, name);
    String baseName = FilenameUtils.getBaseName(name);
    String extension = FilenameUtils.getExtension(name);
    int counter = 1;
    while (target.exists()) {
      target = new File(folder, baseName + " (" + counter++ + ")" + (extension.isEmpty() ? "" : "." + extension));
    }
    return target;
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    int notProcessed = items.size() - imported - skipped - failed;
    LOG.info("Bulk import finished: files={}, imported={}, skipped={}, failed={}, notProcessed={}, games={}{}",
        items.size(), imported, skipped, failed, notProcessed, touchedGames.size(),
        moveTarget != null ? ", moved=" + moved + ", moveFailed=" + moveFailed + " (target \"" + moveTarget.getAbsolutePath() + "\", structure " + structure + ")" : "");
    Platform.runLater(() -> {
      if (!fullscreenLoadingGames.isEmpty() && confirmBlankVideos()) {
        // the blank videos are added before the tables are refreshed
        new Thread(() -> {
          addBlankVideos();
          Platform.runLater(this::notifyTouchedGames);
        }, "bulk-importer-blank-videos").start();
      }
      else {
        notifyTouchedGames();
      }
    });
    super.finalizeModel(progressResultModel);
  }

  private void notifyTouchedGames() {
    for (Integer gameId : touchedGames) {
      EventManager.getInstance().notifyJobFinished(JobType.POPPER_MEDIA_INSTALL, gameId, false, true);
    }
  }

  /**
   * Popper plays a landscape loading video in fullscreen, so a blank video has to be added after it.
   * Like in the asset manager, this is only the case when the imported video is the only loading media of the table.
   */
  private boolean isFullscreenLoadingVideo(int gameId) {
    try {
      if (!FrontendType.Popper.equals(Studio.client.getFrontendService().getFrontendType())) {
        return false;
      }
      List<FrontendMediaItemRepresentation> mediaItems = Studio.client.getGameMediaService().getGameMedia(gameId).getMediaItems(VPinScreen.Loading);
      if (mediaItems.size() != 1) {
        return false;
      }
      FrontendMediaItemRepresentation item = mediaItems.get(0);
      if (!MimeTypeUtil.isVideo(item.getMimeType()) || item.getName().contains("(SCREEN")) {
        return false;
      }
      AssetMetaData metadata = Studio.client.getGameMediaService().getMetadata(gameId, false, VPinScreen.Loading, item.getName());
      return TableAssetManagerDialogController.isLandscape(metadata);
    }
    catch (Exception e) {
      LOG.error("Fullscreen loading video check for game {} failed: {}", gameId, e.getMessage(), e);
      return false;
    }
  }

  /**
   * Asks once for all tables, the answer is stored in the same UI settings the asset manager uses.
   */
  private boolean confirmBlankVideos() {
    try {
      UISettings uiSettings = Studio.client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
      if (uiSettings.isAlwaysAddLoadingBlankVideo()) {
        return true;
      }
      if (uiSettings.isHideLoadingBlankVideoConfirmation()) {
        LOG.info("Skipped adding blank videos for {} fullscreen loading videos, confirmation is disabled", fullscreenLoadingGames.size());
        return false;
      }

      ConfirmationResult confirmationResult = WidgetFactory.showConfirmationWithCheckboxAndCancel(Studio.stage, Messages.get("dialog.fullscreen_loading_video"),
          Messages.get("dialog.add_blank"), Messages.get("dialog.always_add_blank"),
          Messages.get("bulk_importer.fullscreen_loading_videos_text", fullscreenLoadingGames.size()), Messages.get("dialog.fullscreen_loading_video_help"),
          Messages.get("dialog.do_not_show_again"), false);

      if (confirmationResult.isApplyClicked()) {
        uiSettings.setAlwaysAddLoadingBlankVideo(true);
        Studio.client.getPreferenceService().setJsonPreference(uiSettings);
      }
      else if (confirmationResult.isChecked()) {
        uiSettings.setHideLoadingBlankVideoConfirmation(true);
        Studio.client.getPreferenceService().setJsonPreference(uiSettings);
      }
      return confirmationResult.isOkClicked() || confirmationResult.isApplyClicked();
    }
    catch (Exception e) {
      LOG.error("Fullscreen loading video confirmation failed: {}", e.getMessage(), e);
      return false;
    }
  }

  private void addBlankVideos() {
    List<Integer> failedGames = new ArrayList<>();
    for (Integer gameId : fullscreenLoadingGames) {
      try {
        if (Studio.client.getGameMediaService().toFullScreen(gameId, false, VPinScreen.Loading, false)) {
          LOG.info("Added blank video after the fullscreen loading video of game {}", gameId);
        }
        else {
          failedGames.add(gameId);
          LOG.error("Adding blank video after the fullscreen loading video of game {} failed", gameId);
        }
      }
      catch (Exception e) {
        failedGames.add(gameId);
        LOG.error("Adding blank video after the fullscreen loading video of game {} failed: {}", gameId, e.getMessage(), e);
      }
    }
    if (!failedGames.isEmpty()) {
      Platform.runLater(() -> WidgetFactory.showAlert(Studio.stage, Messages.get("common.error"), Messages.get("dialog.fullscreen_switch_failed")));
    }
  }
}
