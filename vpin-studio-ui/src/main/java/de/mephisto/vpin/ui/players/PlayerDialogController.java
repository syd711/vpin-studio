package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.representations.PlayerRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.DashboardController;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class PlayerDialogController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PlayerDialogController.class);

  @FXML
  private Button saveBtn;

  @FXML
  private TextField nameField;

  @FXML
  private TextField initialsField;

  @FXML
  private BorderPane avatarPane;

  private PlayerRepresentation player;

  private Tile avatar;

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.player = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    refreshAvatar();
    validate();
  }

  private void refreshAvatar() {
    if(this.player != null) {
      PreferenceEntryRepresentation avatarEntry = client.getPreference(PreferenceNames.AVATAR);
      Image image = new Image(DashboardController.class.getResourceAsStream("avatar-default.png"));
      if (!StringUtils.isEmpty(avatarEntry.getValue())) {
        image = new Image(client.getAsset(avatarEntry.getValue()));
      }

      if (avatar == null) {
        avatar = TileBuilder.create()
            .skinType(Tile.SkinType.IMAGE)
            .prefSize(300, 300)
            .backgroundColor(Color.TRANSPARENT)
            .image(image)
            .imageMask(Tile.ImageMask.ROUND)
            .textSize(Tile.TextSize.BIGGER)
            .textAlignment(TextAlignment.CENTER)
            .build();
        avatarPane.setCenter(avatar);
      }
      avatar.setImage(image);
    }
  }

  private void validate() {
    boolean valid = !StringUtils.isEmpty(nameField.getText()) && !StringUtils.isEmpty(initialsField.getText());
    this.saveBtn.setDisable(!valid);
  }

  public PlayerRepresentation getPlayer() {
    return player;
  }

  public void setPlayer(PlayerRepresentation p) {
    if (p != null) {
      this.player = p;
      nameField.setText(this.player.getName());
      initialsField.setText(this.player.getInitials());
      refreshAvatar();
    }
  }
}
