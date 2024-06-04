package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.SystemCommandExecutor;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.TableUploadType;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TableOverviewController.GameRepresentationModel;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class TableOverviewContextMenu {
  private final static Logger LOG = LoggerFactory.getLogger(TableOverviewContextMenu.class);

  private final TableOverviewController tableOverviewController;

  public TableOverviewContextMenu(TableOverviewController tableOverviewController) {
    this.tableOverviewController = tableOverviewController;
  }

  public void refreshContextMenu(TableView<GameRepresentationModel> tableView, ContextMenu ctxMenu, GameRepresentation game) {
    ctxMenu.getItems().clear();

    Image image3 = new Image(Studio.class.getResourceAsStream("popper-media.png"));
    ImageView iconPopperMedia = new ImageView(image3);
    iconPopperMedia.setFitWidth(18);
    iconPopperMedia.setFitHeight(18);

    Image image4 = new Image(Studio.class.getResourceAsStream("popper-edit.png"));
    ImageView iconPopperEdit = new ImageView(image4);
    iconPopperEdit.setFitWidth(18);
    iconPopperEdit.setFitHeight(18);

    Image image6 = new Image(Studio.class.getResourceAsStream("popper-assets.png"));
    ImageView iconPopperAssetView = new ImageView(image6);
    iconPopperAssetView.setFitWidth(18);
    iconPopperAssetView.setFitHeight(18);


    Image image5 = new Image(Studio.class.getResourceAsStream("b2s.png"));
    ImageView iconBackglassManager = new ImageView(image5);
    iconBackglassManager.setFitWidth(18);
    iconBackglassManager.setFitHeight(18);


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
    dataItem.setGraphic(iconPopperEdit);
    dataItem.setOnAction(actionEvent -> tableOverviewController.onTableEdit());
    ctxMenu.getItems().add(dataItem);

    MenuItem assetsItem = new MenuItem("Edit Table Assets");
    assetsItem.setGraphic(iconPopperMedia);
    assetsItem.setOnAction(actionEvent -> tableOverviewController.onMediaEdit());
    ctxMenu.getItems().add(assetsItem);

    MenuItem assetsViewItem = new MenuItem("Toggle Asset Management View");
    assetsViewItem.setGraphic(iconPopperAssetView);
    assetsViewItem.setOnAction(actionEvent -> tableOverviewController.onAssetView());
    ctxMenu.getItems().add(assetsViewItem);

    if (game.isVpxGame()) {
      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem vpsItem = new MenuItem("Open VPS Entry");
      vpsItem.setOnAction(actionEvent -> tableOverviewController.onVps());
      vpsItem.setDisable(StringUtils.isEmpty(game.getExtTableId()));
      vpsItem.setGraphic(iconVps);
      ctxMenu.getItems().add(vpsItem);

      MenuItem vpsUpdateItem = new MenuItem("Reset VPS Updates");
      vpsUpdateItem.setOnAction(actionEvent -> tableOverviewController.onVpsReset());
      vpsUpdateItem.setDisable(game.getVpsUpdates().isEmpty());
      vpsUpdateItem.setGraphic(iconVpsReset);
      ctxMenu.getItems().add(vpsUpdateItem);

      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem scanItem = new MenuItem("Scan");
      scanItem.setGraphic(WidgetFactory.createIcon("mdi2m-map-search-outline"));
      scanItem.setOnAction(actionEvent -> tableOverviewController.onTablesScan());
      ctxMenu.getItems().add(scanItem);

      MenuItem scanAllItem = new MenuItem("Scan All");
      scanAllItem.setGraphic(WidgetFactory.createIcon("mdi2m-map-search"));
      scanAllItem.setOnAction(actionEvent -> tableOverviewController.onTablesScanAll());
      ctxMenu.getItems().add(scanAllItem);

      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem importsItem = new MenuItem("Import Tables");
      importsItem.setGraphic(WidgetFactory.createIcon("mdi2d-database-import-outline"));
      importsItem.setOnAction(actionEvent -> tableOverviewController.onImport());
      ctxMenu.getItems().add(importsItem);

      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem b2sItem = new MenuItem("Open Backglass Manager");
      b2sItem.setGraphic(iconBackglassManager);
      b2sItem.setOnAction(actionEvent -> tableOverviewController.onBackglassManager());
      ctxMenu.getItems().add(b2sItem);

      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem uploadAndImportTableItem = new MenuItem("Upload and Import Table");
      uploadAndImportTableItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadAndImportTableItem.setOnAction(actionEvent -> tableOverviewController.openUploadDialogWithCheck(TableUploadType.uploadAndImport));
      ctxMenu.getItems().add(uploadAndImportTableItem);

      MenuItem uploadAndReplaceTableItem = new MenuItem("Upload and Replace Table");
      uploadAndReplaceTableItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadAndReplaceTableItem.setOnAction(actionEvent -> tableOverviewController.openUploadDialogWithCheck(TableUploadType.uploadAndReplace));
      ctxMenu.getItems().add(uploadAndReplaceTableItem);

      MenuItem uploadAndCloneTableItem = new MenuItem("Upload and Clone Table");
      uploadAndCloneTableItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      uploadAndCloneTableItem.setDisable(game.getGameFileName().contains("\\"));
      uploadAndCloneTableItem.setOnAction(actionEvent -> tableOverviewController.openUploadDialogWithCheck(TableUploadType.uploadAndClone));
      ctxMenu.getItems().add(uploadAndCloneTableItem);

      Menu uploadMenu = new Menu("Upload...");

      MenuItem altColorFilesItem = new MenuItem("Upload ALT Color Files");
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

      MenuItem mediaItem = new MenuItem("Upload Media Pack");
      mediaItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      mediaItem.setOnAction(actionEvent -> tableOverviewController.onMediaUpload());
      uploadMenu.getItems().add(mediaItem);

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

      MenuItem pupPackItem = new MenuItem("Upload PUP Pack");
      pupPackItem.setGraphic(WidgetFactory.createIcon("mdi2u-upload"));
      pupPackItem.setOnAction(actionEvent -> tableOverviewController.onPupPackUpload());
      uploadMenu.getItems().add(pupPackItem);

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
      launchItem.setGraphic(WidgetFactory.createGreenIcon("mdi2p-play"));
      launchItem.setOnAction(actionEvent -> tableOverviewController.onPlay());
      ctxMenu.getItems().add(launchItem);

      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem exportItem = new MenuItem("Export");
      exportItem.setGraphic(WidgetFactory.createIcon("mdi2e-export"));
      exportItem.setOnAction(actionEvent -> tableOverviewController.onBackup());
      ctxMenu.getItems().add(exportItem);

      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem vpbmItem = new MenuItem("Open Visual Pinball Backup Manager");
      vpbmItem.setGraphic(iconVpbm);
      vpbmItem.setOnAction(actionEvent -> {
        new Thread(() -> {
          List<String> commands = Arrays.asList("vPinBackupManager.exe");
          LOG.info("Executing vpbm: " + String.join(" ", commands));
          File dir = new File("./resources/", "vpbm");
          SystemCommandExecutor executor = new SystemCommandExecutor(commands);
          executor.setDir(dir);
          executor.executeCommandAsync();
        }).start();
      });
      ctxMenu.getItems().add(vpbmItem);

      ctxMenu.getItems().add(new SeparatorMenuItem());

      MenuItem removeItem = new MenuItem("Delete");
      removeItem.setOnAction(actionEvent -> tableOverviewController.onDelete());
      removeItem.setGraphic(WidgetFactory.createAlertIcon("mdi2d-delete-outline"));
      ctxMenu.getItems().add(removeItem);

    }
  }
}
