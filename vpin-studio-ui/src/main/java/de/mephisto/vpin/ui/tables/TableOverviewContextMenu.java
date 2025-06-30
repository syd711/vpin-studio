package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.VPinManiaScoreSynchronizeProgressModel;
import de.mephisto.vpin.ui.mania.util.ManiaUrlFactory;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class TableOverviewContextMenu {
  private final static Logger LOG = LoggerFactory.getLogger(TableOverviewContextMenu.class);

  private final TableOverviewController tableOverviewController;
  private ContextMenu ctxMenu;

  private static ImageView iconMedia;
  private static ImageView iconAssetView;
  private static ImageView iconBackglassManager;
  private static ImageView iconMania;
  private static ImageView iconManiaSync;
  private static ImageView iconVps;
  private static ImageView iconVpbm;
  private static ImageView iconVpsReset;

  static {
    Image image3 = new Image(Studio.class.getResourceAsStream("popper-media.png"));
    iconMedia = new ImageView(image3);
    iconMedia.setFitWidth(18);
    iconMedia.setFitHeight(18);

    Image image6 = new Image(Studio.class.getResourceAsStream("popper-assets.png"));
    iconAssetView = new ImageView(image6);
    iconAssetView.setFitWidth(18);
    iconAssetView.setFitHeight(18);


    Image image5 = new Image(Studio.class.getResourceAsStream("b2s.png"));
    iconBackglassManager = new ImageView(image5);
    iconBackglassManager.setFitWidth(18);
    iconBackglassManager.setFitHeight(18);


    Image imageMania = new Image(Studio.class.getResourceAsStream("mania.png"));
    iconMania = new ImageView(imageMania);
    iconMania.setFitWidth(18);
    iconMania.setFitHeight(18);

    Image imageManiaSync = new Image(Studio.class.getResourceAsStream("logo-m.png"));
    iconManiaSync = new ImageView(imageManiaSync);
    iconManiaSync.setFitWidth(18);
    iconManiaSync.setFitHeight(18);


    Image image = new Image(Studio.class.getResourceAsStream("vps.png"));
    iconVps = new ImageView(image);
    iconVps.setFitWidth(18);
    iconVps.setFitHeight(18);

    Image imageVpbm = new Image(Studio.class.getResourceAsStream("vpbm-128.png"));
    iconVpbm = new ImageView(imageVpbm);
    iconVpbm.setFitWidth(18);
    iconVpbm.setFitHeight(18);

    Image image2 = new Image(Studio.class.getResourceAsStream("vps-checked.png"));
    iconVpsReset = new ImageView(image2);
    iconVpsReset.setFitWidth(18);
    iconVpsReset.setFitHeight(18);
  }

  public TableOverviewContextMenu(TableOverviewController tableOverviewController) {
    this.tableOverviewController = tableOverviewController;
  }

  public void refreshContextMenu(TableView<GameRepresentationModel> tableView, ContextMenu ctxMenu, List<GameRepresentationModel> games) {
    this.ctxMenu = ctxMenu;
    this.ctxMenu.getItems().clear();

    boolean multiSelection = tableOverviewController.getSelections().size() > 1;
    if (games.isEmpty()) {
      return;
    }
    GameRepresentationModel gameModel = games.get(0);
    GameRepresentation game = gameModel.getGame();

    MenuItem dataItem = new MenuItem("Edit Table Data");
    KeyCombination dataItemKey = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
    dataItem.setAccelerator(dataItemKey);
    dataItem.setDisable(multiSelection);
    dataItem.setOnAction(actionEvent -> tableOverviewController.onTableEdit());
    ctxMenu.getItems().add(dataItem);

    if (Features.FIELDS_STANDARD) {
      boolean isDisabled = game.isDisabled();
      String txt = isDisabled ? "Enable Table(s)" : "Disable Table(s)";
      String icon = isDisabled ? "mdi2c-checkbox-marked-outline" : "mdi2c-checkbox-blank-off-outline";
      //icon = isDisabled ? "mdi2m-microsoft-xbox-controller" : "mdi2m-microsoft-xbox-controller-off";
      MenuItem enableItem = new MenuItem(txt, WidgetFactory.createIcon(icon));
      KeyCombination enableItemKey = new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN);
      enableItem.setAccelerator(enableItemKey);
      enableItem.setOnAction(actionEvent -> tableOverviewController.onTableStatusToggle());
      ctxMenu.getItems().add(enableItem);
    }

    if (Features.MEDIA_ENABLED) {
      MenuItem assetsItem = new MenuItem("Edit Table Assets");
      assetsItem.setGraphic(iconMedia);
      assetsItem.setDisable(multiSelection);
      KeyCombination assetsItemKey = new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN);
      assetsItem.setAccelerator(assetsItemKey);
      assetsItem.setOnAction(actionEvent -> tableOverviewController.onMediaEdit());
      ctxMenu.getItems().add(assetsItem);

      MenuItem assetsViewItem = new MenuItem("Toggle Asset Management View");
      KeyCombination assetsViewItemKey = new KeyCodeCombination(KeyCode.M, KeyCombination.CONTROL_DOWN);
      assetsViewItem.setAccelerator(assetsViewItemKey);
      assetsViewItem.setGraphic(iconAssetView);
      assetsViewItem.setOnAction(actionEvent -> tableOverviewController.onAssetView());
      ctxMenu.getItems().add(assetsViewItem);
    }

    ctxMenu.getItems().add(new SeparatorMenuItem());

//    MenuItem notesItem = new MenuItem("Edit Comment");
//    FontIcon icon = WidgetFactory.createIcon("mdi2c-comment");
//    icon.setIconSize(16);
//    KeyCombination notesItemKey = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
//    notesItem.setAccelerator(notesItemKey);
//    notesItem.setOnAction(actionEvent -> TableDialogs.openCommentDialog(tableOverviewController, game));
//    notesItem.setDisable(StringUtils.isEmpty(game.getExtTableId()));
//    notesItem.setGraphic(icon);
//    ctxMenu.getItems().add(notesItem);
//
//    ctxMenu.getItems().add(new SeparatorMenuItem());

    MenuItem vpsItem = new MenuItem("Open VPS Entry");
    KeyCombination vpsItemKey = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
    vpsItem.setAccelerator(vpsItemKey);
    vpsItem.setDisable(multiSelection);
    vpsItem.setOnAction(actionEvent -> tableOverviewController.onVps());
    vpsItem.setDisable(StringUtils.isEmpty(game.getExtTableId()));
    vpsItem.setGraphic(iconVps);
    ctxMenu.getItems().add(vpsItem);

    MenuItem vpsResetItem = new MenuItem("Reset VPS Updates");
    KeyCombination vpsResetKey = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
    vpsResetItem.setAccelerator(vpsResetKey);
    vpsResetItem.setOnAction(actionEvent -> tableOverviewController.onVpsResetUpdates());
    vpsResetItem.setDisable(game.getVpsUpdates().isEmpty());
    vpsResetItem.setGraphic(iconVpsReset);
    ctxMenu.getItems().add(vpsResetItem);

    ctxMenu.getItems().add(new SeparatorMenuItem());

    MenuItem resetRatingsItem = new MenuItem("Reset Ratings");
    resetRatingsItem.setOnAction(actionEvent -> tableOverviewController.onResetRatings());
    resetRatingsItem.setGraphic(WidgetFactory.createIcon("mdi2u-undo-variant"));
    ctxMenu.getItems().add(resetRatingsItem);

    if (Features.MANIA_ENABLED) {
      ctxMenu.getItems().add(new SeparatorMenuItem());
      MenuItem maniaEntry = new MenuItem("Open VPin Mania Entry");
      maniaEntry.setDisable(StringUtils.isEmpty(game.getExtTableId()) || multiSelection);
      maniaEntry.setOnAction(actionEvent -> Studio.browse(ManiaUrlFactory.createTableUrl(game.getExtTableId(), game.getExtTableVersionId())));
      maniaEntry.setGraphic(iconMania);
      ctxMenu.getItems().add(maniaEntry);

      MenuItem maniaSyncEntry = new MenuItem("Synchronize Scores with VPin Mania");
      maniaSyncEntry.setDisable(multiSelection || games.stream().anyMatch(gameRepresentation -> StringUtils.isEmpty(gameRepresentation.getGame().getExtTableId())));
      maniaSyncEntry.setOnAction(actionEvent -> {
        List<VpsTable> tables = new ArrayList<>();
        for (GameRepresentation gameRepresentation : tableOverviewController.getSelections()) {
          VpsTable vpsTable = client.getVpsService().getTableById(gameRepresentation.getExtTableId());
          if (vpsTable != null) {
            tables.add(vpsTable);
          }
        }
        if (!tables.isEmpty()) {
          ProgressDialog.createProgressDialog(new VPinManiaScoreSynchronizeProgressModel(tables));
        }
      });
      maniaSyncEntry.setDisable(StringUtils.isEmpty(game.getExtTableId()));
      maniaSyncEntry.setGraphic(iconManiaSync);
      ctxMenu.getItems().add(maniaSyncEntry);
    }


    ctxMenu.getItems().add(new SeparatorMenuItem());

    MenuItem eventLogItem = new MenuItem("Event Log");
    KeyCombination eventLogItemKey = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);
    eventLogItem.setAccelerator(eventLogItemKey);
    eventLogItem.setDisable(multiSelection);
    eventLogItem.setOnAction(actionEvent -> TableDialogs.openEventLogDialog(game));
    eventLogItem.setDisable(!game.isEventLogAvailable());
    eventLogItem.setGraphic(WidgetFactory.createIcon("mdi2m-message-text-clock-outline"));
    ctxMenu.getItems().add(eventLogItem);

    ctxMenu.getItems().add(new SeparatorMenuItem());

    MenuItem pinVolItem = new MenuItem("PinVol Settings");
    pinVolItem.setOnAction(actionEvent -> TableDialogs.openPinVolSettings(tableView.getSelectionModel().getSelectedItems().stream().map(m -> m.getGame()).collect(Collectors.toList())));
//    pinVolItem.setDisable(games.isEmpty());
    pinVolItem.setGraphic(WidgetFactory.createIcon("mdi2v-volume-high"));
    ctxMenu.getItems().add(pinVolItem);

    if (game.isVpxGame()) {
      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem reloadItem = new MenuItem("Reload");
      KeyCombination reloadItemKey = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
      reloadItem.setAccelerator(reloadItemKey);
      reloadItem.setGraphic(WidgetFactory.createIcon("mdi2r-refresh"));
      reloadItem.setOnAction(actionEvent -> tableOverviewController.onTableReload());
      ctxMenu.getItems().add(reloadItem);

      MenuItem scanItem = new MenuItem("Scan");
      KeyCombination scanItemKey = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN);
      scanItem.setAccelerator(scanItemKey);
      scanItem.setGraphic(WidgetFactory.createIcon("mdi2m-map-search-outline"));
      scanItem.setOnAction(actionEvent -> tableOverviewController.onTablesScan());
      ctxMenu.getItems().add(scanItem);

      MenuItem validateItem = new MenuItem("Validate");
      validateItem.setGraphic(WidgetFactory.createIcon("mdi2c-check-bold"));
      validateItem.setOnAction(actionEvent -> tableOverviewController.onValidate());
      ctxMenu.getItems().add(validateItem);
    }

//    if (!Features.IS_STANDALONE) {
//      ctxMenu.getItems().add(new SeparatorMenuItem());
//      MenuItem importsItem = new MenuItem("Import Tables");
//      importsItem.setGraphic(WidgetFactory.createIcon("mdi2d-database-import-outline"));
//      importsItem.setOnAction(actionEvent -> tableOverviewController.onImport());
//      ctxMenu.getItems().add(importsItem);
//    }

    //Declutter
//    ctxMenu.getItems().add(new SeparatorMenuItem());
//    MenuItem b2sItem = new MenuItem("Open Backglass Manager");
//    b2sItem.setGraphic(iconBackglassManager);
//    b2sItem.setOnAction(actionEvent -> tableOverviewController.onBackglassManager(game));
//    ctxMenu.getItems().add(b2sItem);

    if (game.isVpxGame()) {

      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem uploadAndImportTableItem = new MenuItem("Upload and Import Table");
      uploadAndImportTableItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      KeyCombination uploadAndImportTableItemKey = new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN);
      uploadAndImportTableItem.setAccelerator(uploadAndImportTableItemKey);
      uploadAndImportTableItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().openUploadDialogWithCheck(UploadType.uploadAndImport));
      ctxMenu.getItems().add(uploadAndImportTableItem);

      MenuItem uploadAndReplaceTableItem = new MenuItem("Upload and Replace Table");
      uploadAndReplaceTableItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadAndReplaceTableItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().openUploadDialogWithCheck(UploadType.uploadAndReplace));
      ctxMenu.getItems().add(uploadAndReplaceTableItem);

      MenuItem uploadAndCloneTableItem = new MenuItem("Upload and Clone Table");
      uploadAndCloneTableItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadAndCloneTableItem.setDisable(game.getGameFileName().contains("\\"));
      uploadAndCloneTableItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().openUploadDialogWithCheck(UploadType.uploadAndClone));
      ctxMenu.getItems().add(uploadAndCloneTableItem);

      Menu uploadMenu = new Menu("Upload...");

      MenuItem altColorFilesItem = new MenuItem("Upload ALT Color File");
      altColorFilesItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      altColorFilesItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onAltColorUpload());
      uploadMenu.getItems().add(altColorFilesItem);

      MenuItem altSoundItem = new MenuItem("Upload ALT Sound Pack");
      altSoundItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      altSoundItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onAltSoundUpload());
      uploadMenu.getItems().add(altSoundItem);

      MenuItem uploadB2SItem = new MenuItem("Upload Backglass");
      uploadB2SItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadB2SItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onBackglassUpload());
      uploadMenu.getItems().add(uploadB2SItem);

      MenuItem uploadCfgItem = new MenuItem("Upload .cfg File");
      uploadCfgItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadCfgItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onCfgUpload());
      uploadMenu.getItems().add(uploadCfgItem);

      MenuItem dmdItem = new MenuItem("Upload DMD Pack");
      dmdItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      dmdItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onDMDUpload());
      uploadMenu.getItems().add(dmdItem);

      MenuItem iniItem = new MenuItem("Upload .ini File");
      iniItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      iniItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onIniUpload());
      uploadMenu.getItems().add(iniItem);

      if (Features.MEDIA_ENABLED) {
        MenuItem mediaItem = new MenuItem("Upload Media Pack");
        mediaItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
        mediaItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onMediaUpload());
        uploadMenu.getItems().add(mediaItem);
      }

      MenuItem musicItem = new MenuItem("Upload Music Pack");
      musicItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      musicItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onMusicUpload());
      uploadMenu.getItems().add(musicItem);

      MenuItem uploadNvItem = new MenuItem("Upload .nv File");
      uploadNvItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadNvItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onNvRamUpload());
      uploadMenu.getItems().add(uploadNvItem);

      MenuItem povItem = new MenuItem("Upload .pov File");
      povItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      povItem.setDisable(tableView.getSelectionModel().isEmpty());
      povItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onPOVUpload());
      uploadMenu.getItems().add(povItem);

      if (Features.PUPPACKS_ENABLED) {
        MenuItem pupPackItem = new MenuItem("Upload PUP Pack");
        pupPackItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
        pupPackItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onPupPackUpload());
        uploadMenu.getItems().add(pupPackItem);
      }

      MenuItem resItem = new MenuItem("Upload .res File");
      resItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      resItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onResUpload());
      uploadMenu.getItems().add(resItem);

      MenuItem romsItem = new MenuItem("Upload ROMs");
      romsItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      romsItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onRomsUpload());
      uploadMenu.getItems().add(romsItem);

      ctxMenu.getItems().add(uploadMenu);
    }

//    ctxMenu.getItems().add(new SeparatorMenuItem());
//

//
//    MenuItem validateAllItem = new MenuItem("Validate All");
//    validateAllItem.setGraphic(WidgetFactory.createIcon("mdi2c-check-bold"));
//    validateAllItem.setOnAction(actionEvent -> tableOverviewController.onValidateAll());
//    ctxMenu.getItems().add(validateAllItem);


    if (game.isVpxGame()) {
      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem launchItem = new MenuItem("Launch");
      launchItem.setDisable(multiSelection);
      KeyCombination launchKey = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);
      launchItem.setAccelerator(launchKey);
      launchItem.setGraphic(WidgetFactory.createGreenIcon("mdi2p-play"));
      launchItem.setOnAction(actionEvent -> tableOverviewController.onPlay());
      ctxMenu.getItems().add(launchItem);

      //decluttering
      if (Features.BACKUPS_ENABLED) {
        ctxMenu.getItems().add(new SeparatorMenuItem());

        MenuItem exportItem = new MenuItem("Backup Table");
        exportItem.setGraphic(WidgetFactory.createIcon("mdi2e-export"));
        exportItem.setOnAction(actionEvent -> tableOverviewController.onBackup());
        ctxMenu.getItems().add(exportItem);
      }
    }

    ctxMenu.getItems().add(new SeparatorMenuItem());

    MenuItem removeItem = new MenuItem("Delete");
    KeyCombination removeItemKey = new KeyCodeCombination(KeyCode.DELETE);
    removeItem.setAccelerator(removeItemKey);
    removeItem.setOnAction(tableOverviewController::onDelete);
    removeItem.setGraphic(WidgetFactory.createAlertIcon("mdi2d-delete-outline"));
    ctxMenu.getItems().add(removeItem);
  }

  public void handleKeyEvent(KeyEvent event) {
    if (!event.isConsumed() && ctxMenu != null) {
      List<MenuItem> items = new ArrayList<>(ctxMenu.getItems());
      for (MenuItem item : items) {
        if (item.getAccelerator() != null && item.getAccelerator().match(event)) {
          ActionEvent ae = new ActionEvent(event.getSource(), event.getTarget());
          item.getOnAction().handle(ae);
          event.consume();
        }
      }
    }
  }
}
