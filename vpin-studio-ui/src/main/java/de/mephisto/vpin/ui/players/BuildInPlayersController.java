package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.StudioFXController;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.paint.Paint;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class BuildInPlayersController implements Initializable, StudioFXController {

  @FXML
  private Button editBtn;

  @FXML
  private Button deleteBtn;

  @FXML
  private TableView<PlayerRepresentation> tableView;

  @FXML
  private TableColumn<PlayerRepresentation, String> idColumn;

  @FXML
  private TableColumn<PlayerRepresentation, String> nameColumn;

  @FXML
  private TableColumn<PlayerRepresentation, String> initialsColumn;

  @FXML
  private TableColumn<PlayerRepresentation, String> avatarColumn;

  // Add a public no-args constructor
  public BuildInPlayersController() {
  }

  @FXML
  private void onReload() {

  }

  @FXML
  private void onAdd() {

  }

  @FXML
  private void onEdit() {

  }

  @FXML
  private void onDelete() {

  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    NavigationController.setBreadCrumb(Arrays.asList("Players", "Build-In Players"));
    tableView.setPlaceholder(new Label("            No one want's to play with you?\n" +
        "Add new players or connect a Discord server."));

    idColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      return new SimpleObjectProperty(String.valueOf(value.getId()));
    });
    nameColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      return new SimpleObjectProperty(value.getName());
    });
    avatarColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      if(!StringUtils.isEmpty(value.getAvatarUrl())) {
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconSize(18);
        fontIcon.setCursor(Cursor.HAND);
        fontIcon.setIconColor(Paint.valueOf("#FF3333"));
        fontIcon.setIconLiteral("bi-exclamation-circle");
        return new SimpleObjectProperty(fontIcon);
      }

      FontIcon fontIcon = new FontIcon();
      fontIcon.setIconSize(18);
      fontIcon.setIconColor(Paint.valueOf("#66FF66"));
      fontIcon.setIconLiteral("bi-check-circle");
      return new SimpleObjectProperty(fontIcon);
    });
    initialsColumn.setCellValueFactory(cellData -> {
      PlayerRepresentation value = cellData.getValue();
      if(!StringUtils.isEmpty(value.getInitials())) {
        FontIcon fontIcon = new FontIcon();
        fontIcon.setIconSize(18);
        fontIcon.setCursor(Cursor.HAND);
        fontIcon.setIconColor(Paint.valueOf("#FF3333"));
        fontIcon.setIconLiteral("bi-exclamation-circle");
        return new SimpleObjectProperty(fontIcon);
      }
      return new SimpleObjectProperty(value.getInitials());
    });
  }
}