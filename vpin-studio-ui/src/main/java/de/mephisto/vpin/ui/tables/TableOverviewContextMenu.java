package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
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

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;

public class TableOverviewContextMenu {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

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
    GameRepresentationModel gameModel = games.getFirst();
    GameRepresentation game = gameModel.getGame();

    MenuItem dataItem = new MenuItem(Messages.get("tables.context_menu.edit_table_data"));
    KeyCombination dataItemKey = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
    dataItem.setAccelerator(dataItemKey);
    dataItem.setDisable(multiSelection);
    dataItem.setOnAction(actionEvent -> tableOverviewController.onTableEdit());
    ctxMenu.getItems().add(dataItem);

    if (Features.FIELDS_STANDARD) {
      boolean isDisabled = game.isDisabled();
      String txt = isDisabled ? Messages.get("tables.context_menu.enable_tables") : Messages.get("tables.context_menu.disable_tables");
      String icon = isDisabled ? "mdi2c-checkbox-marked-outline" : "mdi2c-checkbox-blank-off-outline";
      //icon = isDisabled ? "mdi2m-microsoft-xbox-controller" : "mdi2m-microsoft-xbox-controller-off";
      MenuItem enableItem = new MenuItem(txt, WidgetFactory.createIcon(icon));
      KeyCombination enableItemKey = new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN);
      enableItem.setAccelerator(enableItemKey);
      enableItem.setOnAction(actionEvent -> tableOverviewController.onTableStatusToggle());
      ctxMenu.getItems().add(enableItem);
    }

    if (Features.MEDIA_ENABLED) {
      MenuItem assetsItem = new MenuItem(Messages.get("tables.context_menu.edit_table_assets"));
      assetsItem.setGraphic(iconMedia);
      assetsItem.setDisable(multiSelection);
      KeyCombination assetsItemKey = new KeyCodeCombination(KeyCode.I, KeyCombination.CONTROL_DOWN);
      assetsItem.setAccelerator(assetsItemKey);
      assetsItem.setOnAction(actionEvent -> tableOverviewController.onMediaEdit());
      ctxMenu.getItems().add(assetsItem);

      MenuItem assetsViewItem = new MenuItem(Messages.get("tables.context_menu.toggle_asset_management_view"));
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

    MenuItem vpsItem = new MenuItem(Messages.get("tables.context_menu.open_vps_entry"));
    KeyCombination vpsItemKey = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
    vpsItem.setAccelerator(vpsItemKey);
    vpsItem.setDisable(multiSelection);
    vpsItem.setOnAction(actionEvent -> tableOverviewController.onVps());
    vpsItem.setDisable(StringUtils.isEmpty(game.getExtTableId()));
    vpsItem.setGraphic(iconVps);
    ctxMenu.getItems().add(vpsItem);

    MenuItem vpsResetItem = new MenuItem(Messages.get("tables.context_menu.reset_vps_updates"));
    KeyCombination vpsResetKey = new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN);
    vpsResetItem.setAccelerator(vpsResetKey);
    vpsResetItem.setOnAction(actionEvent -> tableOverviewController.onVpsResetUpdates());
    if (!multiSelection) {
      vpsResetItem.setDisable(game.getVpsUpdates().isEmpty());
    }
    else {
      vpsResetItem.setDisable(false);
    }
    vpsResetItem.setGraphic(iconVpsReset);
    ctxMenu.getItems().add(vpsResetItem);

    ctxMenu.getItems().add(new SeparatorMenuItem());

    MenuItem resetRatingsItem = new MenuItem(Messages.get("tables.context_menu.reset_ratings"));
    resetRatingsItem.setOnAction(actionEvent -> tableOverviewController.onResetRatings());
    resetRatingsItem.setGraphic(WidgetFactory.createIcon("mdi2u-undo-variant"));
    ctxMenu.getItems().add(resetRatingsItem);

    if (Features.MANIA_ENABLED && client.getEmulatorService().isVpxGame(game)) {
      ctxMenu.getItems().add(new SeparatorMenuItem());
      MenuItem maniaEntry = new MenuItem(Messages.get("tables.context_menu.open_vpin_mania_entry"));
      maniaEntry.setDisable(StringUtils.isEmpty(game.getExtTableId()) || multiSelection);
      maniaEntry.setOnAction(actionEvent -> Studio.browse(ManiaUrlFactory.createTableUrl(game.getExtTableId(), game.getExtTableVersionId())));
      maniaEntry.setGraphic(iconMania);
      ctxMenu.getItems().add(maniaEntry);

      MenuItem maniaSyncEntry = new MenuItem(Messages.get("tables.context_menu.synchronize_scores_with_vpin_mania"));
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

    if (client.getEmulatorService().isVpxOrFpGame(game)) {
      MenuItem eventLogItem = new MenuItem(Messages.get("tables.context_menu.event_log"));
      KeyCombination eventLogItemKey = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);
      eventLogItem.setAccelerator(eventLogItemKey);
      eventLogItem.setDisable(multiSelection);
      eventLogItem.setOnAction(actionEvent -> TableDialogs.openEventLogDialog(game));
      eventLogItem.setDisable(!game.isEventLogAvailable());
      eventLogItem.setGraphic(WidgetFactory.createIcon("mdi2m-message-text-clock-outline"));
      ctxMenu.getItems().add(eventLogItem);

      ctxMenu.getItems().add(new SeparatorMenuItem());
    }


    if (client.getEmulatorService().isVpxOrFpGame(game)) {
      MenuItem pinVolItem = new MenuItem(Messages.get("tables.context_menu.pinvol_settings"));
      pinVolItem.setOnAction(actionEvent -> TableDialogs.openPinVolSettings(tableView.getSelectionModel().getSelectedItems().stream().map(m -> m.getGame()).collect(Collectors.toList())));
//    pinVolItem.setDisable(games.isEmpty());
      pinVolItem.setGraphic(WidgetFactory.createIcon("mdi2v-volume-high"));
      ctxMenu.getItems().add(pinVolItem);
    }

    MenuItem reloadItem = new MenuItem(Messages.get("tables.context_menu.reload"));
    KeyCombination reloadItemKey = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
    reloadItem.setAccelerator(reloadItemKey);
    reloadItem.setGraphic(WidgetFactory.createIcon("mdi2r-refresh"));
    reloadItem.setOnAction(actionEvent -> tableOverviewController.onTableReload());
    ctxMenu.getItems().add(reloadItem);


    if (client.getEmulatorService().isVpxGame(game)) {
      ctxMenu.getItems().add(new SeparatorMenuItem());
      MenuItem scanItem = new MenuItem(Messages.get("tables.context_menu.scan"));
      KeyCombination scanItemKey = new KeyCodeCombination(KeyCode.J, KeyCombination.CONTROL_DOWN);
      scanItem.setAccelerator(scanItemKey);
      scanItem.setGraphic(WidgetFactory.createIcon("mdi2m-map-search-outline"));
      scanItem.setOnAction(actionEvent -> tableOverviewController.onTablesScan());
      ctxMenu.getItems().add(scanItem);

      MenuItem validateItem = new MenuItem(Messages.get("tables.context_menu.validate"));
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

    if (client.getEmulatorService().isVpxGame(game)) {
      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem uploadAndImportTableItem = new MenuItem(Messages.get("tables.context_menu.upload_and_import_table"));
      uploadAndImportTableItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      KeyCombination uploadAndImportTableItemKey = new KeyCodeCombination(KeyCode.U, KeyCombination.CONTROL_DOWN);
      uploadAndImportTableItem.setAccelerator(uploadAndImportTableItemKey);
      uploadAndImportTableItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().openUploadDialogWithCheck(UploadType.uploadAndImport));
      ctxMenu.getItems().add(uploadAndImportTableItem);

      MenuItem uploadAndReplaceTableItem = new MenuItem(Messages.get("tables.context_menu.upload_and_replace_table"));
      uploadAndReplaceTableItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadAndReplaceTableItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().openUploadDialogWithCheck(UploadType.uploadAndReplace));
      ctxMenu.getItems().add(uploadAndReplaceTableItem);

      MenuItem uploadAndCloneTableItem = new MenuItem(Messages.get("tables.context_menu.upload_and_clone_table"));
      uploadAndCloneTableItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadAndCloneTableItem.setDisable(game.getGameFileName().contains("\\"));
      uploadAndCloneTableItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().openUploadDialogWithCheck(UploadType.uploadAndClone));
      ctxMenu.getItems().add(uploadAndCloneTableItem);

      List<GameEmulatorRepresentation> vpxEmulators = client.getEmulatorService().getVpxGameEmulators();
      if (vpxEmulators.size() > 1) {
        ctxMenu.getItems().add(new SeparatorMenuItem());

        MenuItem moveTableItem = new MenuItem(multiSelection ? Messages.get("tables.context_menu.move_tables") : Messages.get("tables.context_menu.move_table"));
        KeyCombination moveTableItemKey = new KeyCodeCombination(KeyCode.F6, KeyCombination.SHIFT_DOWN);
        moveTableItem.setAccelerator(moveTableItemKey);
        moveTableItem.setGraphic(WidgetFactory.createIcon("mdi2f-file-move-outline"));
        moveTableItem.setOnAction(actionEvent -> TableDialogs.openTableMoveCloneDialog(tableOverviewController, tableOverviewController.getSelections(), true));
        ctxMenu.getItems().add(moveTableItem);

        MenuItem cloneTableItem = new MenuItem(multiSelection ? Messages.get("tables.context_menu.clone_tables") : Messages.get("tables.context_menu.clone_table"));
        KeyCombination cloneTableItemKey = new KeyCodeCombination(KeyCode.F7, KeyCombination.SHIFT_DOWN);
        cloneTableItem.setAccelerator(cloneTableItemKey);
        cloneTableItem.setGraphic(WidgetFactory.createIcon("mdi2c-content-copy"));
        cloneTableItem.setOnAction(actionEvent -> TableDialogs.openTableMoveCloneDialog(tableOverviewController, tableOverviewController.getSelections(), false));
        ctxMenu.getItems().add(cloneTableItem);
      }

      Menu uploadMenu = new Menu(Messages.get("tables.context_menu.upload"));

      MenuItem altColorFilesItem = new MenuItem(Messages.get("tables.uploads_btn.upload_alt_color_file"));
      altColorFilesItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      altColorFilesItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onAltColorUpload());
      uploadMenu.getItems().add(altColorFilesItem);

      MenuItem altSoundItem = new MenuItem(Messages.get("tables.uploads_btn.upload_alt_sound_pack"));
      altSoundItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      altSoundItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onAltSoundUpload());
      uploadMenu.getItems().add(altSoundItem);

      MenuItem uploadB2SItem = new MenuItem(Messages.get("tables.context_menu.upload_backglass"));
      uploadB2SItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadB2SItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onBackglassUpload());
      uploadMenu.getItems().add(uploadB2SItem);

      MenuItem uploadCfgItem = new MenuItem(Messages.get("tables.uploads_btn.upload_cfg_file"));
      uploadCfgItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadCfgItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onCfgUpload());
      uploadMenu.getItems().add(uploadCfgItem);

      MenuItem dmdItem = new MenuItem(Messages.get("tables.uploads_btn.upload_dmd_pack"));
      dmdItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      dmdItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onDMDUpload());
      uploadMenu.getItems().add(dmdItem);

      MenuItem fplItem = new MenuItem(Messages.get("tables.uploads_btn.upload_fpl_file"));
      fplItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      fplItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onFplUpload());
      uploadMenu.getItems().add(fplItem);

      MenuItem iniItem = new MenuItem(Messages.get("tables.uploads_btn.upload_ini_file"));
      iniItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      iniItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onIniUpload());
      uploadMenu.getItems().add(iniItem);

      if (Features.MEDIA_ENABLED) {
        MenuItem mediaItem = new MenuItem(Messages.get("tables.uploads_btn.upload_media_pack"));
        mediaItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
        mediaItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onMediaUpload());
        uploadMenu.getItems().add(mediaItem);
      }

      MenuItem musicItem = new MenuItem(Messages.get("tables.uploads_btn.upload_music_pack"));
      musicItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      musicItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onMusicUpload());
      uploadMenu.getItems().add(musicItem);

      MenuItem uploadNvItem = new MenuItem(Messages.get("tables.uploads_btn.upload_nv_file"));
      uploadNvItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadNvItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onNvRamUpload());
      uploadMenu.getItems().add(uploadNvItem);

      MenuItem povItem = new MenuItem(Messages.get("tables.uploads_btn.upload_pov_file"));
      povItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      povItem.setDisable(tableView.getSelectionModel().isEmpty());
      povItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onPOVUpload());
      uploadMenu.getItems().add(povItem);

      if (Features.PUPPACKS_ENABLED) {
        MenuItem pupPackItem = new MenuItem(Messages.get("tables.uploads_btn.upload_pup_pack"));
        pupPackItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
        pupPackItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onPupPackUpload());
        uploadMenu.getItems().add(pupPackItem);
      }

      MenuItem resItem = new MenuItem(Messages.get("tables.uploads_btn.upload_res_file"));
      resItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      resItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onResUpload());
      uploadMenu.getItems().add(resItem);

      MenuItem romsItem = new MenuItem(Messages.get("tables.uploads_btn.upload_roms"));
      romsItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      romsItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onRomsUpload());
      uploadMenu.getItems().add(romsItem);

      MenuItem vbsItem = new MenuItem(Messages.get("tables.uploads_btn.upload_vbs_file"));
      vbsItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      vbsItem.setDisable(tableView.getSelectionModel().isEmpty());
      vbsItem.setOnAction(actionEvent -> tableOverviewController.getUploadsButtonController().onVbsUpload());
      uploadMenu.getItems().add(vbsItem);

      ctxMenu.getItems().add(uploadMenu);
    }

//    ctxMenu.getItems().add(new SeparatorMenuItem());
//

//
//    MenuItem validateAllItem = new MenuItem("Validate All");
//    validateAllItem.setGraphic(WidgetFactory.createIcon("mdi2c-check-bold"));
//    validateAllItem.setOnAction(actionEvent -> tableOverviewController.onValidateAll());
//    ctxMenu.getItems().add(validateAllItem);


    if (client.getEmulatorService().isVpxGame(game)) {
      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem launchItem = new MenuItem(Messages.get("tables.context_menu.launch"));
      launchItem.setDisable(multiSelection);
      KeyCombination launchKey = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);
      launchItem.setAccelerator(launchKey);
      launchItem.setGraphic(WidgetFactory.createGreenIcon("mdi2p-play"));
      launchItem.setOnAction(actionEvent -> tableOverviewController.onPlay());
      ctxMenu.getItems().add(launchItem);

      //decluttering
      if (Features.BACKUPS_ENABLED) {
        ctxMenu.getItems().add(new SeparatorMenuItem());

        MenuItem exportItem = new MenuItem(Messages.get("tables.context_menu.backup_tables"));
        KeyCombination backupKey = new KeyCodeCombination(KeyCode.B, KeyCombination.CONTROL_DOWN);
        exportItem.setAccelerator(backupKey);
        exportItem.setGraphic(WidgetFactory.createIcon("mdi2a-archive-outline"));
        exportItem.setOnAction(actionEvent -> tableOverviewController.onBackup());
        ctxMenu.getItems().add(exportItem);
      }

      MenuItem tagItem = new MenuItem(Messages.get("tables.context_menu.tag_tables"));
      KeyCombination backupKey = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN);
      tagItem.setAccelerator(backupKey);
      tagItem.setGraphic(WidgetFactory.createIcon("mdi2t-tag-multiple-outline"));
      tagItem.setOnAction(actionEvent -> tableOverviewController.onTagging());
      ctxMenu.getItems().add(tagItem);
    }

    GameEmulatorRepresentation emu = client.getEmulatorService().getGameEmulator(game.getEmulatorId());
    if (emu.isFpEmulator() || emu.isVpxEmulator()) {
      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem removeItem = new MenuItem(Messages.get("common.delete"));
      KeyCombination removeItemKey = new KeyCodeCombination(KeyCode.DELETE);
      removeItem.setAccelerator(removeItemKey);
      removeItem.setOnAction(tableOverviewController::onDelete);
      removeItem.setGraphic(WidgetFactory.createAlertIcon("mdi2d-delete-outline"));
      ctxMenu.getItems().add(removeItem);
    }

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
