package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.fx.Features;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.frontend.FrontendType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.util.ManiaUrlFactory;
import de.mephisto.vpin.ui.preferences.VPBMPreferencesController;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class TableOverviewContextMenu {
  private final static Logger LOG = LoggerFactory.getLogger(TableOverviewContextMenu.class);

  private final TableOverviewController tableOverviewController;
  private ContextMenu ctxMenu;

  public TableOverviewContextMenu(TableOverviewController tableOverviewController) {
    this.tableOverviewController = tableOverviewController;
  }

  public void refreshContextMenu(TableView<GameRepresentationModel> tableView, ContextMenu ctxMenu, GameRepresentation game) {
    this.ctxMenu = ctxMenu;
    this.ctxMenu.getItems().clear();
    FrontendType frontendType = client.getFrontendService().getFrontendType();

    List<GameRepresentation> games = tableView.getSelectionModel().getSelectedItems().stream().map(m -> m.getGame()).collect(Collectors.toList());

    Image image3 = new Image(Studio.class.getResourceAsStream("popper-media.png"));
    ImageView iconMedia = new ImageView(image3);
    iconMedia.setFitWidth(18);
    iconMedia.setFitHeight(18);

    Image image6 = new Image(Studio.class.getResourceAsStream("popper-assets.png"));
    ImageView iconAssetView = new ImageView(image6);
    iconAssetView.setFitWidth(18);
    iconAssetView.setFitHeight(18);


    Image image5 = new Image(Studio.class.getResourceAsStream("b2s.png"));
    ImageView iconBackglassManager = new ImageView(image5);
    iconBackglassManager.setFitWidth(18);
    iconBackglassManager.setFitHeight(18);


    Image imageMania = new Image(Studio.class.getResourceAsStream("mania.png"));
    ImageView iconMania = new ImageView(imageMania);
    iconMania.setFitWidth(18);
    iconMania.setFitHeight(18);


    Image image = new Image(Studio.class.getResourceAsStream("vps.png"));
    ImageView iconVps = new ImageView(image);
    iconVps.setFitWidth(18);
    iconVps.setFitHeight(18);

    Image imageVpbm = new Image(Studio.class.getResourceAsStream("vpbm-128.png"));
    ImageView iconVpbm = new ImageView(imageVpbm);
    iconVpbm.setFitWidth(18);
    iconVpbm.setFitHeight(18);

    Image image2 = new Image(Studio.class.getResourceAsStream("vps-checked.png"));
    ImageView iconVpsReset = new ImageView(image2);
    iconVpsReset.setFitWidth(18);
    iconVpsReset.setFitHeight(18);

    MenuItem dataItem = new MenuItem("Edit Table Data");
    KeyCombination dataItemKey = new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN);
    dataItem.setAccelerator(dataItemKey);
    dataItem.setOnAction(actionEvent -> tableOverviewController.onTableEdit());
    ctxMenu.getItems().add(dataItem);

    if (frontendType.supportStandardFields()) {
      boolean isDisabled = game.isDisabled();
      String txt = isDisabled ? "Enable Table" : "Disable Table";
      String icon = isDisabled ? "mdi2c-checkbox-marked-outline" : "mdi2c-checkbox-blank-off-outline";
      //icon = isDisabled ? "mdi2m-microsoft-xbox-controller" : "mdi2m-microsoft-xbox-controller-off";
      MenuItem enableItem = new MenuItem(txt, WidgetFactory.createIcon(icon));
      KeyCombination enableItemKey = new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN);
      enableItem.setAccelerator(enableItemKey);
      enableItem.setOnAction(actionEvent -> tableOverviewController.onTableStatusToggle());
      ctxMenu.getItems().add(enableItem);
    }

    if (frontendType.supportMedias()) {
      MenuItem assetsItem = new MenuItem("Edit Table Assets");
      assetsItem.setGraphic(iconMedia);
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

    MenuItem notesItem = new MenuItem("Edit Comment");
    FontIcon icon = WidgetFactory.createIcon("mdi2c-comment");
    icon.setIconSize(16);
    KeyCombination notesItemKey = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
    notesItem.setAccelerator(notesItemKey);
    notesItem.setOnAction(actionEvent -> TableDialogs.openCommentDialog(game));
    notesItem.setDisable(StringUtils.isEmpty(game.getExtTableId()));
    notesItem.setGraphic(icon);
    ctxMenu.getItems().add(notesItem);

    ctxMenu.getItems().add(new SeparatorMenuItem());

    MenuItem vpsItem = new MenuItem("Open VPS Entry");
    KeyCombination vpsItemKey = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
    vpsItem.setAccelerator(vpsItemKey);
    vpsItem.setOnAction(actionEvent -> tableOverviewController.onVps());
    vpsItem.setDisable(StringUtils.isEmpty(game.getExtTableId()));
    vpsItem.setGraphic(iconVps);
    ctxMenu.getItems().add(vpsItem);

    MenuItem vpsUpdateItem = new MenuItem("Reset VPS Updates");
    vpsUpdateItem.setOnAction(actionEvent -> tableOverviewController.onVpsResetUpdates());
    vpsUpdateItem.setDisable(game.getVpsUpdates().isEmpty());
    vpsUpdateItem.setGraphic(iconVpsReset);
    ctxMenu.getItems().add(vpsUpdateItem);

    if (Features.MANIA_ENABLED) {
      ctxMenu.getItems().add(new SeparatorMenuItem());
      MenuItem maniaEntry = new MenuItem("Open VPin Mania Entry");
      maniaEntry.setDisable(StringUtils.isEmpty(game.getExtTableId()));
      maniaEntry.setOnAction(actionEvent -> Studio.browse(ManiaUrlFactory.createTableUrl(game.getExtTableId())));
      maniaEntry.setDisable(StringUtils.isEmpty(game.getExtTableId()));
      maniaEntry.setGraphic(iconMania);
      ctxMenu.getItems().add(maniaEntry);
    }


    ctxMenu.getItems().add(new SeparatorMenuItem());

    MenuItem eventLogItem = new MenuItem("Event Log");
    KeyCombination eventLogItemKey = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);
    eventLogItem.setAccelerator(eventLogItemKey);
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

    ctxMenu.getItems().add(new SeparatorMenuItem());

    if (game.isVpxGame()) {
      MenuItem scanItem = new MenuItem("Scan");
      KeyCombination scanItemKey = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN);
      scanItem.setAccelerator(scanItemKey);
      scanItem.setGraphic(WidgetFactory.createIcon("mdi2m-map-search-outline"));
      scanItem.setOnAction(actionEvent -> tableOverviewController.onTablesScan());
      ctxMenu.getItems().add(scanItem);

      MenuItem scanAllItem = new MenuItem("Scan All");
//      KeyCombination scanAllItemKey = new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN, KeyCombination.ALT_DOWN);
//      scanAllItem.setAccelerator(scanAllItemKey);
      scanAllItem.setGraphic(WidgetFactory.createIcon("mdi2m-map-search"));
      scanAllItem.setOnAction(actionEvent -> tableOverviewController.onTablesScanAll());
      ctxMenu.getItems().add(scanAllItem);
    }

    if (frontendType.isNotStandalone()) {
      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem importsItem = new MenuItem("Import Tables");
      importsItem.setGraphic(WidgetFactory.createIcon("mdi2d-database-import-outline"));
      importsItem.setOnAction(actionEvent -> tableOverviewController.onImport());
      ctxMenu.getItems().add(importsItem);
    }

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
      uploadAndImportTableItem.setOnAction(actionEvent -> tableOverviewController.openUploadDialogWithCheck(UploadType.uploadAndImport));
      ctxMenu.getItems().add(uploadAndImportTableItem);

      MenuItem uploadAndReplaceTableItem = new MenuItem("Upload and Replace Table");
      KeyCombination uploadAndReplaceTableItemKey = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
      uploadAndReplaceTableItem.setAccelerator(uploadAndReplaceTableItemKey);
      uploadAndReplaceTableItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadAndReplaceTableItem.setOnAction(actionEvent -> tableOverviewController.openUploadDialogWithCheck(UploadType.uploadAndReplace));
      ctxMenu.getItems().add(uploadAndReplaceTableItem);

      MenuItem uploadAndCloneTableItem = new MenuItem("Upload and Clone Table");
      uploadAndCloneTableItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadAndCloneTableItem.setDisable(game.getGameFileName().contains("\\"));
      uploadAndCloneTableItem.setOnAction(actionEvent -> tableOverviewController.openUploadDialogWithCheck(UploadType.uploadAndClone));
      ctxMenu.getItems().add(uploadAndCloneTableItem);

      Menu uploadMenu = new Menu("Upload...");

      MenuItem altColorFilesItem = new MenuItem("Upload ALT Color File");
      altColorFilesItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      altColorFilesItem.setOnAction(actionEvent -> tableOverviewController.onAltColorUpload());
      uploadMenu.getItems().add(altColorFilesItem);

      MenuItem altSoundItem = new MenuItem("Upload ALT Sound Pack");
      altSoundItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      altSoundItem.setOnAction(actionEvent -> tableOverviewController.onAltSoundUpload());
      uploadMenu.getItems().add(altSoundItem);

      MenuItem uploadB2SItem = new MenuItem("Upload Backglass");
      uploadB2SItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadB2SItem.setOnAction(actionEvent -> tableOverviewController.onBackglassUpload());
      uploadMenu.getItems().add(uploadB2SItem);

      MenuItem uploadCfgItem = new MenuItem("Upload .cfg File");
      uploadCfgItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadCfgItem.setOnAction(actionEvent -> tableOverviewController.onCfgUpload());
      uploadMenu.getItems().add(uploadCfgItem);

      MenuItem dmdItem = new MenuItem("Upload DMD Pack");
      dmdItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      dmdItem.setOnAction(actionEvent -> tableOverviewController.onDMDUpload());
      uploadMenu.getItems().add(dmdItem);

      MenuItem iniItem = new MenuItem("Upload .ini File");
      iniItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      iniItem.setOnAction(actionEvent -> tableOverviewController.onIniUpload());
      uploadMenu.getItems().add(iniItem);

      if (frontendType.supportMedias()) {
        MenuItem mediaItem = new MenuItem("Upload Media Pack");
        mediaItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
        mediaItem.setOnAction(actionEvent -> tableOverviewController.onMediaUpload());
        uploadMenu.getItems().add(mediaItem);
      }

      MenuItem musicItem = new MenuItem("Upload Music Pack");
      musicItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      musicItem.setOnAction(actionEvent -> tableOverviewController.onMusicUpload());
      uploadMenu.getItems().add(musicItem);

      MenuItem uploadNvItem = new MenuItem("Upload .nv File");
      uploadNvItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadNvItem.setOnAction(actionEvent -> tableOverviewController.onNvRamUpload());
      uploadMenu.getItems().add(uploadNvItem);

      MenuItem povItem = new MenuItem("Upload .pov File");
      povItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      povItem.setDisable(tableView.getSelectionModel().isEmpty());
      povItem.setOnAction(actionEvent -> tableOverviewController.onPOVUpload());
      uploadMenu.getItems().add(povItem);

      if (frontendType.supportPupPacks()) {
        MenuItem pupPackItem = new MenuItem("Upload PUP Pack");
        pupPackItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
        pupPackItem.setOnAction(actionEvent -> tableOverviewController.onPupPackUpload());
        uploadMenu.getItems().add(pupPackItem);
      }

      MenuItem resItem = new MenuItem("Upload .res File");
      resItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      resItem.setOnAction(actionEvent -> tableOverviewController.onResUpload());
      uploadMenu.getItems().add(resItem);

      MenuItem romsItem = new MenuItem("Upload ROMs");
      romsItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      romsItem.setOnAction(actionEvent -> tableOverviewController.onRomsUpload());
      uploadMenu.getItems().add(romsItem);

      ctxMenu.getItems().add(uploadMenu);
    }

    ctxMenu.getItems().add(new SeparatorMenuItem());

    MenuItem validateItem = new MenuItem("Validate");
    validateItem.setGraphic(WidgetFactory.createIcon("mdi2c-check-bold"));
    validateItem.setOnAction(actionEvent -> tableOverviewController.onValidate());
    ctxMenu.getItems().add(validateItem);

    MenuItem validateAllItem = new MenuItem("Validate All");
    validateAllItem.setGraphic(WidgetFactory.createIcon("mdi2c-check-bold"));
    validateAllItem.setOnAction(actionEvent -> tableOverviewController.onValidateAll());
    ctxMenu.getItems().add(validateAllItem);


    if (game.isVpxGame()) {
      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem launchItem = new MenuItem("Launch");
      KeyCombination launchKey = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);
      launchItem.setAccelerator(launchKey);

      launchItem.setGraphic(WidgetFactory.createGreenIcon("mdi2p-play"));
      launchItem.setOnAction(actionEvent -> tableOverviewController.onPlay());
      ctxMenu.getItems().add(launchItem);

      //decluttering
      if (frontendType.supportArchive()) {
        ctxMenu.getItems().add(new SeparatorMenuItem());

        MenuItem exportItem = new MenuItem("Backup Table");
        exportItem.setGraphic(WidgetFactory.createIcon("mdi2e-export"));
        exportItem.setOnAction(actionEvent -> tableOverviewController.onBackup());
        ctxMenu.getItems().add(exportItem);

        ctxMenu.getItems().add(new SeparatorMenuItem());

        MenuItem vpbmItem = new MenuItem("Open Visual Pinball Backup Manager");
        vpbmItem.setGraphic(iconVpbm);
        vpbmItem.setOnAction(actionEvent -> {
          VPBMPreferencesController.openVPBM();
        });
        ctxMenu.getItems().add(vpbmItem);
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
