package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.JFXFuture;
import de.mephisto.vpin.connectors.iscored.GameRoom;
import de.mephisto.vpin.connectors.iscored.IScored;
import de.mephisto.vpin.restclient.iscored.IScoredGameRoom;
import de.mephisto.vpin.restclient.iscored.IScoredSettings;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class IScoredGameRoomDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(IScoredGameRoomDialogController.class);

  @FXML
  private Button saveBtn;

  @FXML
  private CheckBox synchronizationCheckbox;

  @FXML
  private CheckBox resetCheckbox;

  @FXML
  private CheckBox scoreApiCheckbox;

  @FXML
  private CheckBox readApiCheckbox;

  @FXML
  private Label errorTitle;

  @FXML
  private Label errorMessage;

  @FXML
  private TextField urlField;

  @FXML
  private Button validateBtn;

  @FXML
  private Pane errorPane;

  private IScoredSettings iScoredSettings;
  private IScoredGameRoom gameRoom;

  @FXML
  private void onValidate() {
    String url = urlField.getText().trim();
    if (StringUtils.isEmpty(url)) {
      return;
    }

    scoreApiCheckbox.setSelected(false);
    readApiCheckbox.setSelected(false);
    urlField.setDisable(true);
    validateBtn.setDisable(true);
    errorPane.setVisible(false);
    setDisabled(true);

    try {
      JFXFuture.supplyAsync(() -> {
        return IScored.loadGameRoom(url);
      }).thenAcceptLater((gr) -> {
        urlField.setDisable(false);
        validateBtn.setDisable(false);

        if(gr == null) {
          errorPane.setVisible(true);
          errorTitle.setText("Invalid Game Room URL");
          errorMessage.setText("No game room could be read for the given URL.");
          return;
        }

        if (!gr.getSettings().isApiReadingEnabled()) {
          errorPane.setVisible(true);
          errorTitle.setText("Read API Not Enabled");
          errorMessage.setText("The game room must have the read API enabled in the iScored settings.");
          return;
        }
        if (!gr.getSettings().isPublicScoresReadingEnabled()) {
          errorPane.setVisible(true);
          errorTitle.setText("Public Score Reading Not Enabled");
          errorMessage.setText("The game room must have the public score reading API enabled in the iScored settings.");
          return;
        }

        scoreApiCheckbox.setSelected(true);
        readApiCheckbox.setSelected(true);

        errorPane.setVisible(false);
        urlField.setDisable(false);
        validateBtn.setDisable(false);
        setDisabled(false);
        saveBtn.setDisable(false);
      });
    }
    catch (Exception e) {
      setDisabled(true);
      errorTitle.setText("Invalid Game Room URL");
      errorMessage.setText("No game room could be read for the given URL.");
      saveBtn.setDisable(true);
    }
  }

  private void setDisabled(boolean error) {
    resetCheckbox.setDisable(error);
    synchronizationCheckbox.setDisable(error);
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @FXML
  private void onSaveClick(ActionEvent e) {
    gameRoom.setUrl(urlField.getText());
    gameRoom.setScoreReset(resetCheckbox.isSelected());
    gameRoom.setSynchronize(synchronizationCheckbox.isSelected());

    List<IScoredGameRoom> collect = new ArrayList<>(iScoredSettings.getGameRooms().stream().filter(s -> !s.getUuid().equals(gameRoom.getUuid())).collect(Collectors.toList()));
    collect.add(gameRoom);

    iScoredSettings.setGameRooms(collect);
    client.getPreferenceService().setJsonPreference(iScoredSettings);

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void onDialogCancel() {

  }

  public void setData(@NonNull IScoredSettings iScoredSettings, @Nullable IScoredGameRoom gr) {
    this.iScoredSettings = iScoredSettings;
    this.gameRoom = gr;
    if (this.gameRoom == null) {
      this.gameRoom = new IScoredGameRoom();
      this.gameRoom.setUuid(UUID.randomUUID().toString());
    }

    this.resetCheckbox.setSelected(gameRoom.isScoreReset());
    this.synchronizationCheckbox.setSelected(gameRoom.isSynchronize());

    saveBtn.setDisable(StringUtils.isEmpty(gameRoom.getUrl()));
  }


  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    errorPane.managedProperty().bindBidirectional(errorPane.visibleProperty());
    errorPane.setVisible(false);

    saveBtn.setDisable(true);

    urlField.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        saveBtn.setDisable(true);
        setDisabled(true);
      }
    });

    synchronizationCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

      }
    });
  }
}
