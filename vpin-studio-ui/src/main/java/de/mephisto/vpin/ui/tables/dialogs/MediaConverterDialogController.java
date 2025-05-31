package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.converter.MediaConversionCommand;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class MediaConverterDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MediaConverterDialogController.class);

  @FXML
  private ComboBox<String> screensCombo;

  @FXML
  private Button saveBtn;

  @FXML
  private ComboBox<CommandModel> conversionsCombo;

  private List<GameRepresentation> games;

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    String value = screensCombo.getValue();
    MediaConversionCommand command = conversionsCombo.getValue().command;

    stage.close();

    Platform.runLater(() -> {
      MediaConversionBulkProgressModel conversion = new MediaConversionBulkProgressModel("Conversion", this.games, VPinScreen.valueOf(value), command);
      ProgressDialog.createProgressDialog(conversion);
    });
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    ObservableList<String> screenNames = FXCollections.observableList(new ArrayList<>());
    screenNames.addAll("", VPinScreen.Other2.name(), VPinScreen.GameInfo.name(), VPinScreen.GameHelp.name(), VPinScreen.PlayField.name(), VPinScreen.BackGlass.name(), VPinScreen.Loading.name(), VPinScreen.Topper.name(), VPinScreen.DMD.name(), VPinScreen.Menu.name());
    screensCombo.setItems(screenNames);
    saveBtn.setDisable(true);

    List<CommandModel> collect = client.getMediaConversionService().getCommandList().stream().map(c -> new CommandModel(c)).collect(Collectors.toList());
    collect.add(0, null);
    conversionsCombo.setItems(FXCollections.observableList(collect));

    screensCombo.valueProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        saveBtn.setDisable(StringUtils.isEmpty(newValue) || conversionsCombo.getValue() == null);
      }
    });
    conversionsCombo.valueProperty().addListener(new ChangeListener<CommandModel>() {
      @Override
      public void changed(ObservableValue<? extends CommandModel> observable, CommandModel oldValue, CommandModel newValue) {
        saveBtn.setDisable(newValue == null || StringUtils.isEmpty(screensCombo.getValue()));
      }
    });
  }

  @Override
  public void onDialogCancel() {

  }

  public void setGames(List<GameRepresentation> games) {
    this.games = games;
  }

  class CommandModel {
    private final MediaConversionCommand command;
    private String suffix;

    CommandModel(MediaConversionCommand command) {
      this.command = command;
      this.suffix = "for videos";
      if(command.getType() == MediaConversionCommand.TYPE_IMAGE) {
        this.suffix = "for images";
      }
    }

    @Override
    public String toString() {
      return command.getName() + " (" + suffix + ")";
    }
  }
}
