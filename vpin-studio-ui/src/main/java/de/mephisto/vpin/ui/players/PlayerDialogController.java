package de.mephisto.vpin.ui.players;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.representations.PreferenceEntryRepresentation;
import de.mephisto.vpin.ui.DashboardController;
import de.mephisto.vpin.ui.players.dialogs.PlayerSaveProgressModel;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class PlayerDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(PlayerDialogController.class);
  private static File lastFolderSelection;

  @FXML
  private Button saveBtn;

  @FXML
  private TextField nameField;

  @FXML
  private TextField initialsField;

  @FXML
  private TextField discordIdText;

  @FXML
  private CheckBox adminRoleCheckbox;

  @FXML
  private Label initialsOverlayLabel;

  @FXML
  private BorderPane avatarPane;

  @FXML
  private StackPane avatarStack;

  @FXML
  private VBox tournamentGroup;

  private PlayerRepresentation player;

  private Tile avatar;

  private File avatarFile;

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.player = null;
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(stage, new PlayerSaveProgressModel(this.player, this.avatarFile, this.avatarStack));
    this.player = (PlayerRepresentation) progressDialog.getResults().get(0);
    stage.close();
  }

  @FXML
  private void onDelete() {
    this.avatarFile = null;
    this.avatar = null;
    this.player.setAvatar(null);
    refreshAvatar();
    this.validateInput();
  }

  @FXML
  private void onFileSelect(ActionEvent event) {
    Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

    FileChooser fileChooser = new FileChooser();
    if (PlayerDialogController.lastFolderSelection != null) {
      fileChooser.setInitialDirectory(PlayerDialogController.lastFolderSelection);
    }
    fileChooser.setTitle("Select Image");
    fileChooser.getExtensionFilters().addAll(
        new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg"));

    this.avatarFile = fileChooser.showOpenDialog(stage);
    if (this.avatarFile != null) {
      PlayerDialogController.lastFolderSelection = this.avatarFile.getParentFile();
    }
    refreshAvatar();
  }

  private void refreshAvatar() {
    if (this.avatarFile != null) {
      try {
        client.clearCache();
        this.initialsOverlayLabel.setText("");
        FileInputStream fileInputStream = new FileInputStream(this.avatarFile);
        Image image = new Image(fileInputStream);
        avatar.setImage(image);
        fileInputStream.close();
      } catch (IOException e) {
        LOG.error("Failed to preview avatar: " + e.getMessage(), e);
      }
      return;
    }

    if (this.avatar == null) {
      Image image = new Image(DashboardController.class.getResourceAsStream("avatar-blank.png"));
      avatar = TileBuilder.create()
          .skinType(Tile.SkinType.IMAGE)
          .maxSize(200, 200)
          .backgroundColor(Color.TRANSPARENT)
          .image(image)
          .imageMask(Tile.ImageMask.ROUND)
          .textSize(Tile.TextSize.BIGGER)
          .textAlignment(TextAlignment.CENTER)
          .build();
      avatarPane.setCenter(avatar);
    }


    if (this.player.getAvatar() != null) {
      this.initialsOverlayLabel.setText("");
      Image image = new Image(client.getAsset(AssetType.AVATAR, this.player.getAvatar().getUuid()));
      avatar.setImage(image);
    }
  }

  private void validateInput() {
    String name = nameField.getText();
    String initials = initialsField.getText();

    boolean valid = !StringUtils.isEmpty(name) && !StringUtils.isEmpty(initials) && initials.length() == 3;
    this.saveBtn.setDisable(!valid);

    if (this.avatarFile == null && this.player.getAvatar() == null && !StringUtils.isEmpty(initials)) {
      if (initials.length() > 3) {
        initials = initials.substring(0, 3);
      }
      this.initialsOverlayLabel.setText(initials.toUpperCase());
    }
    else if (!StringUtils.isEmpty(name) && StringUtils.isEmpty(initials) && (this.avatarFile == null && this.player.getAvatar() == null)) {
      if (name.length() > 3) {
        name = name.substring(0, 3);
      }
      this.initialsOverlayLabel.setText(name.toUpperCase());
    }
  }

  @Override
  public void onDialogCancel() {
    this.player = null;
  }

  public PlayerRepresentation getPlayer() {
    return player;
  }

  public void setPlayer(PlayerRepresentation p) {
    if (p != null) {
      this.player = p;
      nameField.setText(this.player.getName());
      initialsField.setText(this.player.getInitials());
      discordIdText.setText(this.player.getExternalId());
      adminRoleCheckbox.setSelected(player.isAdministrative());
      refreshAvatar();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    tournamentGroup.managedProperty().bindBidirectional(tournamentGroup.visibleProperty());

    this.player = new PlayerRepresentation();
    nameField.setText(player.getName());
    nameField.textProperty().addListener((observableValue, s, t1) -> {
      player.setName(t1);
      validateInput();
    });

    initialsField.setText(player.getInitials());
    initialsField.textProperty().addListener((observableValue, s, t1) -> {
      String initials = t1.toUpperCase();
      if (initials.length() > 3) {
        initials = initials.substring(0, 3);
        initialsField.setText(initials);
      }
      player.setInitials(initials);
      validateInput();
    });

    Font font = Font.font("Impact", FontPosture.findByName("regular"), 60);
    this.initialsOverlayLabel.setFont(font);

    refreshAvatar();
    this.validateInput();

    this.adminRoleCheckbox.setSelected(player.isAdministrative());
    this.adminRoleCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> player.setAdministrative(newValue));

    this.discordIdText.setText(player.getExternalId());
    this.discordIdText.textProperty().addListener((observable, oldValue, newValue) -> player.setExternalId(newValue));

    PreferenceEntryRepresentation preference = client.getPreference(PreferenceNames.TOURNAMENTS_ENABLED);
    this.tournamentGroup.setVisible(preference.getBooleanValue());

    this.nameField.requestFocus();
  }
}
