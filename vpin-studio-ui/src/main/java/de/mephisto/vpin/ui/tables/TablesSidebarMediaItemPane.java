package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.ui.tables.dialogs.ScreenTile;
import de.mephisto.vpin.ui.tables.dialogs.TableAssetManagerPane;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import org.kordamp.ikonli.javafx.FontIcon;

/**
 * Screen tile of the media panel of the tables sidebar: preview of the default media, "Name · N files" below and the
 * screen actions laid over the top of the preview while the tile is hovered.
 */
public class TablesSidebarMediaItemPane extends ScreenTile {

  /**
   * The hover action bar, shown by a {@link de.mephisto.vpin.ui.util.VisibilityHoverListener}
   */
  FlowPane top;
  Button btn_edit;
  Button btn_delete;
  Button btn_view;
  Button btn_openfolder;
  Button btn_dmdPos;

  public TablesSidebarMediaItemPane(TablesSidebarMediaController controller, TableAssetManagerPane<?> rootPane,
                                    String text, VPinScreen screen, String[] suffixes) {
    super(rootPane, text, screen, true, suffixes);

    btn_edit = createButton("mdi2m-movie-edit-outline", "sm-button", e -> controller.onMediaEdit(screen));
    btn_edit.setTooltip(new Tooltip(Messages.get("dialog.asset_manager")));
    btn_view = createButton("mdi2e-eye-outline", "sm-button", e -> controller.onMediaViewClick(screen));
    btn_delete = createButton("mdi2d-delete-outline", "sm-danger-button", e -> controller.onMediaDeleteClick(screen));
    btn_openfolder = createButton("mdi2f-folder-open", "sm-button", e -> controller.onMediaFolderOpenClick(screen));
    btn_openfolder.getStyleClass().add("folder-component");
    btn_openfolder.managedProperty().bind(btn_openfolder.visibleProperty());

    // small buttons that wrap into a second row on narrow tiles (Apron / Full DMD has five of them)
    top = new FlowPane(4, 4, btn_edit, btn_view, btn_delete);
    if (hasDmdPos()) {
      btn_dmdPos = createButton("mdi2t-target-variant", "sm-button", e -> controller.onDMDPosition());
      btn_dmdPos.setTooltip(new Tooltip(Messages.get("backglass.directb2s_admin.adjust_dmd_position")));
      top.getChildren().add(btn_dmdPos);
    }
    top.getChildren().add(btn_openfolder);
    top.setAlignment(Pos.TOP_LEFT);
    top.setPadding(new Insets(8, 8, 0, 8));
    top.setMinSize(0, 0);
    // the transparent area between the buttons passes clicks and hover to the tile
    top.setPickOnBounds(false);
    addOverlay(top, Pos.TOP_LEFT);
  }

  private Button createButton(String icon, String styleClass, EventHandler<ActionEvent> eventHandler) {
    Button btn = new Button();
    btn.getStyleClass().addAll(styleClass, "sm-button-small", "sm-button-icon", "am-tile-action");
    btn.setOnAction(eventHandler);
    FontIcon font = new FontIcon(icon);
    font.setIconSize(16);
    font.getStyleClass().add("sm-danger-button".equals(styleClass) ? "c-icon-error" : "c-icon-white");
    btn.setGraphic(font);
    return btn;
  }

  public boolean hasDmdPos() {
    return VPinScreen.BackGlass.equals(screen) || VPinScreen.DMD.equals(screen) || VPinScreen.Menu.equals(screen);
  }
}
