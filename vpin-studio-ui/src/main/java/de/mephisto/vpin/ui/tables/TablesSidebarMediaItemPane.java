package de.mephisto.vpin.ui.tables;

import org.kordamp.ikonli.javafx.FontIcon;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.ui.tables.dialogs.TableAssetManagerPane;
import de.mephisto.vpin.ui.tables.dialogs.TableAssetManagerPane.MediaPane;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class TablesSidebarMediaItemPane extends MediaPane {

  BorderPane top;
  Button btn_edit;
  Button btn_delete;
  Button btn_view;
  Button btn_openfolder;
  Button btn_dmdPos;

  public TablesSidebarMediaItemPane(TablesSidebarMediaController controller, TableAssetManagerPane<?> rootPane, 
                                    String text, VPinScreen screen, String[] suffixes) {
    super(rootPane, text, screen, suffixes);
    this.getStyleClass().add("media-container");

    Label label = new Label(text);
    label.setTextFill(Color.WHITE);
    BorderPane.setAlignment(label, Pos.CENTER);
    this.setBottom(label);

    top = new BorderPane();
    this.setTop(top);
    BorderPane.setAlignment(top, Pos.TOP_RIGHT);

    HBox righthbox = new HBox();
    righthbox.setSpacing(3);
    top.setRight(righthbox);
    BorderPane.setAlignment(righthbox, Pos.CENTER);

    btn_openfolder = createButton("mdi2f-folder-open", null, "folder-component", e -> controller.onMediaFolderOpenClick(screen));
    righthbox.getChildren().add(btn_openfolder);

    HBox lefthbox = new HBox();
    lefthbox.setSpacing(3);
    top.setLeft(lefthbox);
    BorderPane.setAlignment(lefthbox, Pos.CENTER);

    btn_edit = createButton("mdi2m-movie-edit-outline", null, null, e -> controller.onMediaEdit(screen));
    lefthbox.getChildren().add(btn_edit);
    btn_view = createButton("mdi2e-eye", null, null, e -> controller.onMediaViewClick(screen));
    lefthbox.getChildren().add(btn_view);
    btn_delete = createButton("mdi2d-delete-outline", "#ff3333", null, e -> controller.onMediaDeleteClick(screen));
    lefthbox.getChildren().add(btn_delete);

    if (hasDmdPos()) {
      btn_dmdPos = createButton("mdi2t-target-variant", null, null, e -> controller.onDMDPosition());
      btn_dmdPos.setTooltip(new Tooltip("Adjust DMD position"));
      lefthbox.getChildren().add(btn_dmdPos);
    }
  }

  private Button createButton(String icon, String color, String styleClass, EventHandler<ActionEvent> eventHandler) {
    Button btn = new Button();
    btn.setTextFill(color != null ? Color.valueOf(color) : Color.WHITE);
    btn.setOnAction(eventHandler);
    if (styleClass != null) {
      btn.getStyleClass().add(styleClass);
    }
    FontIcon font = new FontIcon(icon);
    font.setIconColor(color != null ? Color.valueOf(color) : Color.WHITE);
    btn.setGraphic(font);
    return btn;
  }

  public boolean hasDmdPos() {
    return VPinScreen.BackGlass.equals(screen) || VPinScreen.DMD.equals(screen) || VPinScreen.Menu.equals(screen);
  }
}