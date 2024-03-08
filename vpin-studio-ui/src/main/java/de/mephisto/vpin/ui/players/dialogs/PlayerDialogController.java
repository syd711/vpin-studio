package de.mephisto.vpin.ui.players.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.players.PlayerRepresentation;
import de.mephisto.vpin.restclient.tournaments.TournamentSettings;
import de.mephisto.vpin.ui.DashboardController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.StudioFileChooser;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class PlayerDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(PlayerDialogController.class);

  @FXML
  private Button saveBtn;

  @FXML
  private TextField nameField;

  @FXML
  private TextField initialsField;

  @FXML
  private CheckBox adminRoleCheckbox;

  @FXML
  private CheckBox tournamentPlayerCheckbox;

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

    Platform.runLater(() -> {
      if (!StringUtils.isEmpty(this.player.getTournamentUserUuid()) && !this.tournamentPlayerCheckbox.isSelected()) {
        Optional<ButtonType> result2 = WidgetFactory.showConfirmation(Studio.stage, "Tournament Player", "The player \"" + this.player.getName() + "\" is a registered tournament player.", "This will delete the online account and all related highscores and subscribed tournaments too.");
        if (!result2.isPresent() || !result2.get().equals(ButtonType.OK)) {
          return;
        }
      }

      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(stage, new PlayerSaveProgressModel(this.player, this.tournamentPlayerCheckbox.isSelected(), this.avatarFile, this.avatarStack));
      if (!progressDialog.getResults().isEmpty()) {
        Object o = progressDialog.getResults().get(0);
        if (o instanceof PlayerRepresentation) {
          this.player = (PlayerRepresentation) o;
        }
        else {
          WidgetFactory.showAlert(Studio.stage, String.valueOf(o));
        }
      }
      else {
        this.player = null;
      }
      stage.close();
    });
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

    StudioFileChooser fileChooser = new StudioFileChooser();
    fileChooser.setTitle("Select Image");
    fileChooser.getExtensionFilters().addAll(
      new FileChooser.ExtensionFilter("Image", "*.png", "*.jpg", "*.jpeg"));

    this.avatarFile = fileChooser.showOpenDialog(stage);
    refreshAvatar();
  }

  private void refreshAvatar() {
    if (this.avatarFile != null) {
      ProgressResultModel progressDialog = ProgressDialog.createProgressDialog(new AvatarGeneratorProgressModel(avatar, this.avatarFile));
      this.avatarFile = (File) progressDialog.getResults().get(0);
      this.initialsOverlayLabel.setText("");
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

  public void setPlayer(PlayerRepresentation p, List<PlayerRepresentation> players) {
    if (p != null) {
      this.player = p;
      nameField.setText(this.player.getName());
      initialsField.setText(this.player.getInitials());
      adminRoleCheckbox.setSelected(player.isAdministrative());
      tournamentPlayerCheckbox.setSelected(!StringUtils.isEmpty(player.getTournamentUserUuid()));
      refreshAvatar();
    }
    else {
      this.adminRoleCheckbox.setSelected(players.stream().filter(PlayerRepresentation::isAdministrative).findFirst().isEmpty());
    }

    for (PlayerRepresentation other : players) {
      if (other.getId() != this.player.getId() && other.isAdministrative()) {
        this.adminRoleCheckbox.setDisable(true);
        break;
      }
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

    this.tournamentPlayerCheckbox.setSelected(!StringUtils.isEmpty(player.getTournamentUserUuid()));

    TournamentSettings settings = client.getTournamentsService().getSettings();
    this.tournamentGroup.setVisible(settings.isEnabled());

    this.nameField.requestFocus();
  }
}
