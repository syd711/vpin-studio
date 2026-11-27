package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.wovp.WOVPSettings;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationItem;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.mania.VPinManiaScoreSynchronizeProgressModel;
import de.mephisto.vpin.ui.mania.util.ManiaUrlFactory;
import de.mephisto.vpin.ui.tables.dialogs.TableMoveCopyController;
import de.mephisto.vpin.ui.tables.panels.UploadsButtonController;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.Features;
import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.util.ContextMenus.*;

/**
 * Context menu of the table overview, built with {@link de.mephisto.vpin.ui.util.ContextMenus}:
 * caption, launch, edit, online (VPS, achievements, Mania), files (uploads, move, backup), maintenance, delete.
 */
public class TableOverviewContextMenu {

  private final TableOverviewController tableOverviewController;
  private ContextMenu ctxMenu;

  public TableOverviewContextMenu(TableOverviewController tableOverviewController) {
    this.tableOverviewController = tableOverviewController;
  }

  public void refreshContextMenu(TableView<GameRepresentationModel> tableView, ContextMenu ctxMenu, List<GameRepresentationModel> games) {
    this.ctxMenu = ctxMenu;
    this.ctxMenu.getItems().clear();

    if (games.isEmpty()) {
      return;
    }
    boolean multiSelection = tableOverviewController.getSelections().size() > 1;
    GameRepresentation game = games.getFirst().getGame();
    boolean vpx = client.getEmulatorService().isVpxGame(game);
    boolean vpxOrFp = client.getEmulatorService().isVpxOrFpGame(game);
    boolean hasVps = !StringUtils.isEmpty(game.getExtTableId());

    ctxMenu.getItems().add(caption(multiSelection
        ? Messages.get("tables.context_menu.selection_count", games.size())
        : StringUtils.defaultString(game.getGameDisplayName(), game.getGameFileName())));

    // --- launch
    if (vpx) {
      MenuItem launchItem = primary(Messages.get("tables.context_menu.launch"), "mdi2p-play",
          key(KeyCode.L, KeyCombination.CONTROL_DOWN), tableOverviewController::onPlay);
      launchItem.setDisable(multiSelection);
      section(ctxMenu, launchItem);
    }

    // --- edit
    MenuItem dataItem = item(Messages.get("tables.context_menu.edit_table_data"), "mdi2l-lead-pencil",
        key(KeyCode.O, KeyCombination.CONTROL_DOWN), tableOverviewController::onTableEdit);
    dataItem.setDisable(multiSelection);

    MenuItem assetsItem = null;
    MenuItem assetsViewItem = null;
    if (Features.MEDIA_ENABLED) {
      assetsItem = item(Messages.get("tables.context_menu.edit_table_assets"), "mdi2i-image-edit-outline",
          key(KeyCode.I, KeyCombination.CONTROL_DOWN), tableOverviewController::onMediaEdit);
      assetsItem.setDisable(multiSelection);

      assetsViewItem = item(Messages.get("tables.context_menu.toggle_asset_management_view"), "mdi2v-view-dashboard-edit-outline",
          key(KeyCode.M, KeyCombination.CONTROL_DOWN), tableOverviewController::onAssetView);
    }

    MenuItem enableItem = null;
    if (Features.FIELDS_STANDARD) {
      boolean isDisabled = game.isDisabled();
      enableItem = item(Messages.get(isDisabled ? "tables.context_menu.enable_tables" : "tables.context_menu.disable_tables"),
          isDisabled ? "mdi2c-checkbox-marked-outline" : "mdi2c-checkbox-blank-off-outline",
          key(KeyCode.E, KeyCombination.ALT_DOWN), tableOverviewController::onTableStatusToggle);
    }

    MenuItem tagItem = null;
    if (vpx) {
      tagItem = item(Messages.get("tables.context_menu.tag_tables"), "mdi2t-tag-multiple-outline",
          key(KeyCode.T, KeyCombination.CONTROL_DOWN), tableOverviewController::onTagging);
    }

    MenuItem pinVolItem = null;
    if (vpxOrFp) {
      pinVolItem = item(Messages.get("tables.context_menu.pinvol_settings"), "mdi2v-volume-high",
          () -> TableDialogs.openPinVolSettings(tableView.getSelectionModel().getSelectedItems().stream().map(GameRepresentationModel::getGame).collect(Collectors.toList())));
    }
    section(ctxMenu, dataItem, assetsItem, assetsViewItem, enableItem, tagItem, pinVolItem);

    // --- online
    MenuItem vpsItem = item(Messages.get("tables.context_menu.open_vps_entry"), "mdi2o-open-in-new",
        key(KeyCode.D, KeyCombination.CONTROL_DOWN), tableOverviewController::onVps);
    vpsItem.setDisable(multiSelection || !hasVps);

    MenuItem vpsResetItem = item(Messages.get("tables.context_menu.reset_vps_updates"), "mdi2b-bell-check-outline",
        key(KeyCode.W, KeyCombination.CONTROL_DOWN), tableOverviewController::onVpsResetUpdates);
    vpsResetItem.setDisable(!multiSelection && game.getVpsUpdates().isEmpty());

    MenuItem achievementsItem = item(Messages.get("tables.context_menu.show_achievements"), "mdi2t-trophy-outline",
        () -> NavigationController.navigateTo(NavigationItem.Achievements, new NavigationOptions(game.getRom())));
    achievementsItem.setDisable(multiSelection || StringUtils.isEmpty(game.getRom()));

    Menu maniaMenu = null;
    if (Features.MANIA_ENABLED && vpx) {
      MenuItem maniaEntry = item(Messages.get("tables.context_menu.open_vpin_mania_entry"), "mdi2o-open-in-new",
          () -> Studio.browse(ManiaUrlFactory.createTableUrl(game.getExtTableId(), game.getExtTableVersionId())));
      maniaEntry.setDisable(multiSelection || !hasVps);

      MenuItem maniaSyncEntry = item(Messages.get("tables.context_menu.synchronize_scores_with_vpin_mania"), "mdi2c-cloud-sync-outline",
          this::onManiaSync);
      maniaSyncEntry.setDisable(games.stream().allMatch(g -> StringUtils.isEmpty(g.getGame().getExtTableId())));

      maniaMenu = menu(Messages.get("tables.context_menu.vpin_mania"), "mdi2e-earth", maniaEntry, maniaSyncEntry);
    }
    section(ctxMenu, vpsItem, vpsResetItem, achievementsItem, maniaMenu);

    // --- files
    if (vpx) {
      section(ctxMenu, createUploadMenu(tableView, game));

      List<GameEmulatorRepresentation> vpxEmulators = client.getEmulatorService().getVpxGameEmulators();
      if (vpxEmulators.size() > 1) {
        MenuItem moveCopyTableItem = item(Messages.get(multiSelection ? "tables.context_menu.move_copy_tables" : "tables.context_menu.move_copy_table"),
            "mdi2f-file-move-outline", key(KeyCode.F6, KeyCombination.SHIFT_DOWN),
            () -> TableDialogs.openTableMoveCopyDialog(tableOverviewController, tableOverviewController.getSelections()));
        moveCopyTableItem.setDisable(!TableMoveCopyController.isSingleSourceEmulator(tableOverviewController.getSelections()));
        ctxMenu.getItems().add(moveCopyTableItem);
      }

      if (Features.BACKUPS_ENABLED) {
        ctxMenu.getItems().add(item(Messages.get("tables.context_menu.backup_tables"), "mdi2a-archive-outline",
            key(KeyCode.B, KeyCombination.CONTROL_DOWN), tableOverviewController::onBackup));
      }
    }

    // --- maintenance
    List<MenuItem> maintenance = new ArrayList<>();
    if (vpx) {
      maintenance.add(item(Messages.get("tables.context_menu.scan"), "mdi2m-map-search-outline",
          key(KeyCode.J, KeyCombination.CONTROL_DOWN), tableOverviewController::onTablesScan));
      maintenance.add(item(Messages.get("tables.context_menu.validate"), "mdi2c-check-bold", tableOverviewController::onValidate));
    }
    if (vpxOrFp) {
      MenuItem eventLogItem = item(Messages.get("tables.context_menu.event_log"), "mdi2m-message-text-clock-outline",
          key(KeyCode.E, KeyCombination.CONTROL_DOWN), () -> TableDialogs.openEventLogDialog(game));
      eventLogItem.setDisable(multiSelection || !game.isEventLogAvailable());
      maintenance.add(eventLogItem);
    }
    maintenance.add(item(Messages.get("tables.context_menu.reset_ratings"), "mdi2u-undo-variant", tableOverviewController::onResetRatings));
    if (vpx) {
      WOVPSettings wovpSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.WOVP_SETTINGS, WOVPSettings.class);
      if (wovpSettings != null && wovpSettings.isEnabled()) {
        maintenance.add(new SeparatorMenuItem());
        maintenance.add(item(Messages.get("tables.context_menu.wovp_patch"), "mdi2c-checkbox-marked-circle-plus-outline",
            () -> WovpPatchProgressModel.run(vpxSelections(), true)));
        maintenance.add(item(Messages.get("tables.context_menu.wovp_unpatch"), "mdi2c-checkbox-marked-circle-minus-outline",
            () -> WovpPatchProgressModel.run(vpxSelections(), false)));
      }
    }
    MenuItem reloadItem = item(Messages.get("tables.context_menu.reload"), "mdi2r-refresh",
        key(KeyCode.R, KeyCombination.CONTROL_DOWN), tableOverviewController::onTableReload);
    section(ctxMenu, menu(Messages.get("tables.context_menu.maintenance"), "mdi2w-wrench-outline", maintenance.toArray(new MenuItem[0])), reloadItem);

    // --- delete
    GameEmulatorRepresentation emu = client.getEmulatorService().getGameEmulator(game.getEmulatorId());
    if (emu != null && (emu.isFpEmulator() || emu.isVpxEmulator())) {
      section(ctxMenu, danger(Messages.get("common.delete"), "mdi2d-delete-outline",
          new KeyCodeCombination(KeyCode.DELETE), () -> tableOverviewController.onDelete(null)));
    }
  }

  /**
   * Table uploads (import / replace / clone) first, then the assets of the selected table.
   */
  private Menu createUploadMenu(TableView<GameRepresentationModel> tableView, GameRepresentation game) {
    UploadsButtonController uploads = tableOverviewController.getUploadsButtonController();
    String icon = "mdi2u-upload";

    MenuItem importItem = item(Messages.get("tables.context_menu.upload_and_import_table"), "mdi2t-table-plus",
        key(KeyCode.U, KeyCombination.CONTROL_DOWN), () -> uploads.openUploadDialogWithCheck(UploadType.uploadAndImport));
    MenuItem replaceItem = item(Messages.get("tables.context_menu.upload_and_replace_table"), "mdi2t-table-refresh",
        () -> uploads.openUploadDialogWithCheck(UploadType.uploadAndReplace));
    MenuItem cloneItem = item(Messages.get("tables.context_menu.upload_and_clone_table"), "mdi2c-content-copy",
        () -> uploads.openUploadDialogWithCheck(UploadType.uploadAndClone));
    cloneItem.setDisable(game.getGameFileName().contains("\\"));

    boolean noSelection = tableView.getSelectionModel().isEmpty();
    MenuItem povItem = item(Messages.get("tables.uploads_btn.upload_pov_file"), icon, uploads::onPOVUpload);
    povItem.setDisable(noSelection);
    MenuItem vbsItem = item(Messages.get("tables.uploads_btn.upload_vbs_file"), icon, uploads::onVbsUpload);
    vbsItem.setDisable(noSelection);

    return menu(Messages.get("tables.context_menu.upload"), icon,
        importItem, replaceItem, cloneItem,
        new SeparatorMenuItem(),
        item(Messages.get("tables.uploads_btn.upload_alt_color_file"), icon, uploads::onAltColorUpload),
        item(Messages.get("tables.uploads_btn.upload_alt_sound_pack"), icon, uploads::onAltSoundUpload),
        item(Messages.get("tables.context_menu.upload_backglass"), icon, uploads::onBackglassUpload),
        item(Messages.get("tables.uploads_btn.upload_cfg_file"), icon, uploads::onCfgUpload),
        item(Messages.get("tables.uploads_btn.upload_dmd_pack"), icon, uploads::onDMDUpload),
        item(Messages.get("tables.uploads_btn.upload_fpl_file"), icon, uploads::onFplUpload),
        item(Messages.get("tables.uploads_btn.upload_ini_file"), icon, uploads::onIniUpload),
        Features.MEDIA_ENABLED ? item(Messages.get("tables.uploads_btn.upload_media_pack"), icon, uploads::onMediaUpload) : null,
        item(Messages.get("tables.uploads_btn.upload_music_pack"), icon, uploads::onMusicUpload),
        item(Messages.get("tables.uploads_btn.upload_nv_file"), icon, uploads::onNvRamUpload),
        povItem,
        Features.PUPPACKS_ENABLED ? item(Messages.get("tables.uploads_btn.upload_pup_pack"), icon, uploads::onPupPackUpload) : null,
        item(Messages.get("tables.uploads_btn.upload_res_file"), icon, uploads::onResUpload),
        item(Messages.get("tables.uploads_btn.upload_roms"), icon, uploads::onRomsUpload),
        vbsItem);
  }

  private void onManiaSync() {
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
  }

  public void handleKeyEvent(KeyEvent event) {
    fireAccelerator(ctxMenu, event);
  }

  private static KeyCombination key(KeyCode code, KeyCombination.Modifier modifier) {
    return new KeyCodeCombination(code, modifier);
  }

  private List<GameRepresentation> vpxSelections() {
    return tableOverviewController.getSelections().stream()
        .filter(g -> client.getEmulatorService().isVpxGame(g))
        .collect(Collectors.toList());
  }
}
