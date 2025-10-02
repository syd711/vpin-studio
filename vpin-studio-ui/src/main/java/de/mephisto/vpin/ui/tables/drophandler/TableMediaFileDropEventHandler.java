package de.mephisto.vpin.ui.tables.drophandler;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.games.FrontendMediaRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TableOverviewController;
import de.mephisto.vpin.ui.tables.dialogs.FrontendMediaUploadProgressModel;
import de.mephisto.vpin.ui.tables.dialogs.TableAssetManagerDialogController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.ButtonType;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class TableMediaFileDropEventHandler implements EventHandler<DragEvent> {
  private final static Logger LOG = LoggerFactory.getLogger(TableMediaFileDropEventHandler.class);

  private final VPinScreen screen;
  private final List<String> suffixes;
  private TableOverviewController tablesController;
  private TableAssetManagerDialogController dialogController;

  public TableMediaFileDropEventHandler(TableOverviewController tablesController, VPinScreen screen, String... suffix) {
    this.tablesController = tablesController;
    this.screen = screen;
    this.suffixes = Arrays.asList(suffix);
  }

  public TableMediaFileDropEventHandler(TableAssetManagerDialogController dialogController, VPinScreen screen, String... suffix) {
    this.dialogController = dialogController;
    this.screen = screen;
    this.suffixes = Arrays.asList(suffix);
  }

  @Override
  public void handle(DragEvent event) {
    List<File> files = event.getDragboard().getFiles();
    List<File> filtered = files.stream().filter(f -> {
      String suffix = FilenameUtils.getExtension(f.getName());
      return suffixes.contains(suffix.toLowerCase());
    }).collect(Collectors.toList());

    if (filtered.isEmpty() && !event.getDragboard().hasContent(DataFormat.URL)) {
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "None of the selected is valid for this upload.",
            "Only files with extension(s) \"" + String.join("\", \"", suffixes) + "\" are accepted here.");
      });
      return;
    }

    if (event.getDragboard().hasContent(DataFormat.URL)) {
      dropAsset(event);
    }
    else if (!files.isEmpty()) {
      dropFiles(files);
    }
  }

  private void dropAsset(DragEvent event) {
    try {
      FrontendMediaItemRepresentation media = (FrontendMediaItemRepresentation) event.getDragboard().getContent(DataFormat.URL);
      Platform.runLater(() -> {
        ProgressDialog.createProgressDialog(new TableMediaCopyProgressModel(screen, media));
        refreshView();
      });
    }
    catch (Exception e) {
      LOG.warn("Media drop failed: {}", e.getMessage());
    }
  }

  private void dropFiles(List<File> filtered) {
    List<File> draggedCopies = new ArrayList<>();
    try {
      for (File file : filtered) {
        String baseName = FilenameUtils.getBaseName(file.getName());
        String suffix = FilenameUtils.getExtension(file.getName());
        File copy = File.createTempFile(baseName, "." + suffix);
        FileUtils.copyFile(file, copy);
        draggedCopies.add(copy);
        LOG.info("Writted dropped copy: " + copy.getAbsolutePath());
      }
    }
    catch (IOException e) {
      LOG.info("Creating drop copies failed: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Creating copies from drop failed: " + e.getMessage());
      });
      return;
    }


    Platform.runLater(() -> {
      FrontendMediaRepresentation medias = null;
      GameRepresentation game = null;
      PlaylistRepresentation playlist = null;

      if (this.tablesController != null) {
        game = tablesController.getSelection();
        medias = client.getGameMediaService().getGameMedia(game.getId());
      }
      else {
        medias = dialogController.getFrontendMedia();
        if (dialogController.isPlaylistMode()) {
          playlist = dialogController.getPlaylist();
        }
        else {
          game = dialogController.getGame();
        }
      }

      boolean append = true;
      if (!medias.getMediaItems(screen).isEmpty()) {
        append = false;
        Optional<ButtonType> buttonType = WidgetFactory.showConfirmationWithOption(Studio.stage, "Replace Media?",
            "A media asset already exists.",
            "Append new asset or overwrite existing asset?", "Overwrite", "Append");
        if (buttonType.isPresent() && buttonType.get().equals(ButtonType.OK)) {
        }
        else if (buttonType.isPresent() && buttonType.get().equals(ButtonType.APPLY)) {
          append = true;
        }
        else {
          return;
        }
      }

      if (playlist != null) {
        FrontendMediaUploadProgressModel model = new FrontendMediaUploadProgressModel(playlist,
            "Media Upload", draggedCopies, screen, append);
        ProgressDialog.createProgressDialog(model);
      }
      else if (game != null) {
        FrontendMediaUploadProgressModel model = new FrontendMediaUploadProgressModel(game,
            "Media Upload", draggedCopies, screen, append);
        ProgressDialog.createProgressDialog(model);
      }

      refreshView();
    });
  }

  private void refreshView() {
    if (tablesController != null) {
      tablesController.getTablesController().getAssetViewSideBarController().refreshTableMediaView();
    }
    if (dialogController != null) {
      dialogController.refreshTableMediaView();
    }
  }
}
