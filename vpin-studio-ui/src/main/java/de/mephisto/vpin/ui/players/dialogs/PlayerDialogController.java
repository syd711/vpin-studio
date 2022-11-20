package de.mephisto.vpin.ui.players.dialogs;

import de.mephisto.vpin.restclient.PopperScreen;
import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.VPinStudioClient;
import de.mephisto.vpin.restclient.representations.CompetitionRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameMediaRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class PlayerDialogController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(PlayerDialogController.class);

  @FXML
  private ImageView iconPreview;

  @FXML
  private ImageView badgePreview;

  @FXML
  private ComboBox<String> competitionIconCombo;

  @FXML
  private ComboBox<GameRepresentation> tableCombo;

  @FXML
  private CheckBox badgeCheckbox;

  @FXML
  private Button saveBtn;

  @FXML
  private TextField nameField;

  @FXML
  private Label durationLabel;

  @FXML
  private DatePicker startDatePicker;

  @FXML
  private DatePicker endDatePicker;

  private CompetitionRepresentation competition;

  @FXML
  private void onBadgeCheck() {
    competition.setCustomizeMedia(this.badgeCheckbox.isSelected());
    refreshPreview(tableCombo.getValue(), competitionIconCombo.getValue());
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    this.competition = null;
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
    competition = new CompetitionRepresentation();
    competition.setType("offline");
    competition.setName("My next competition");

    Date end = Date.from(LocalDate.now().plus(7, ChronoUnit.DAYS).atStartOfDay(ZoneId.systemDefault()).toInstant());
    competition.setStartDate(Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant()));
    competition.setEndDate(end);

    badgeCheckbox.setSelected(true);
    competition.setCustomizeMedia(true);
    saveBtn.setDisable(true);

    nameField.setText(competition.getName());
    nameField.textProperty().addListener((observableValue, s, t1) -> {
      competition.setName(t1);
      validate();
    });


    startDatePicker.setValue(LocalDate.now());
    startDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
      if (t1 != null) {
        Date date = Date.from(t1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        competition.setStartDate(date);
      }
      else {
        competition.setStartDate(null);
      }
      validate();
    });

    endDatePicker.setValue(LocalDate.now().plus(7, ChronoUnit.DAYS));
    endDatePicker.valueProperty().addListener((observableValue, localDate, t1) -> {
      if (t1 != null) {
        Date date = Date.from(t1.atStartOfDay(ZoneId.systemDefault()).toInstant());
        competition.setEndDate(date);
      }
      else {
        competition.setEndDate(null);
      }

      long diff = ChronoUnit.DAYS.between(LocalDate.now(), t1);
      if (diff > 0) {
        competition.setActive(true);
      }

      validate();
    });

    List<GameRepresentation> games = client.getGames();
    List<GameRepresentation> filtered = new ArrayList<>();
    for (GameRepresentation game : games) {
      filtered.add(game);
    }

    ObservableList<GameRepresentation> gameRepresentations = FXCollections.observableArrayList(filtered);
    tableCombo.getItems().addAll(gameRepresentations);
    tableCombo.valueProperty().addListener((observableValue, gameRepresentation, t1) -> {
      competition.setGameId(t1.getId());
      refreshPreview(t1, competitionIconCombo.getValue());
      validate();
    });


    ObservableList<String> imageList = FXCollections.observableList(new ArrayList<>(client.getCompetitionBadges()));
    competitionIconCombo.setItems(imageList);
    competitionIconCombo.setCellFactory(c -> new CompetitionImageListCell(client));
    competitionIconCombo.valueProperty().addListener((observableValue, s, t1) -> {
      competition.setBadge(t1);
      refreshPreview(tableCombo.getValue(), t1);
      validate();
    });

    validate();
  }

  private void refreshPreview(GameRepresentation game, String badge) {
    if (game != null) {
      GameMediaRepresentation gameMedia = client.getGameMedia(game.getId());
      GameMediaItemRepresentation mediaItem = gameMedia.getMedia().get(PopperScreen.Wheel.name());
      if (mediaItem != null) {
        String url1 = mediaItem.getUri();
        byte[] bytes = RestClient.getInstance().readBinary(url1);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Image image = new Image(byteArrayInputStream);
        iconPreview.setImage(image);

        if (badge != null && badgeCheckbox.isSelected()) {
          Image badgeIcon = new Image(client.getCompetitionBadge(badge));
          badgePreview.setImage(badgeIcon);
        }
        else {
          badgePreview.setImage(null);
        }
      }
    }
    else {
      iconPreview.setImage(null);
    }
  }

  private void validate() {
    LocalDate start = startDatePicker.getValue();
    LocalDate value = endDatePicker.getValue();

    long diff = ChronoUnit.DAYS.between(start, value);
    this.durationLabel.setText(diff + " days");

    boolean valid = !StringUtils.isEmpty(competition.getName())
        && competition.getName().length() > 3
        && competition.getGameId() > 0
        && competition.getStartDate() != null
        && competition.getEndDate() != null
        && competition.getStartDate().getTime() < competition.getEndDate().getTime();
    this.saveBtn.setDisable(!valid);
  }

  public CompetitionRepresentation getCompetition() {
    return competition;
  }

  public void setCompetition(CompetitionRepresentation c) {
    if (c != null) {
      this.competition = c;
      GameRepresentation game = client.getGame(c.getGameId());

      nameField.setText(this.competition.getName());
      this.startDatePicker.setValue(this.competition.getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
      this.endDatePicker.setValue(this.competition.getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
      this.tableCombo.setValue(game);

      this.badgeCheckbox.setSelected(c.isCustomizeMedia());
      this.competitionIconCombo.setValue(c.getBadge());
      String badge = c.getBadge();
      refreshPreview(game, badge);
    }
  }


  public static class CompetitionImageListCell extends ListCell<String> {
    private final VPinStudioClient client;

    public CompetitionImageListCell(VPinStudioClient client) {
      this.client = client;
    }

    protected void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(null);
      setText(null);
      if (item != null) {
        Image image = new Image(client.getCompetitionBadge(item));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(80);

        int percentageWidth = (int) (80 * 100 / image.getWidth());
        int height = (int) (image.getHeight() * percentageWidth / 100);
        imageView.setFitHeight(height);
        setGraphic(imageView);
        setText(item);
      }
    }
  }
}
