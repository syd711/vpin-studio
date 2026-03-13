package de.mephisto.vpin.ui.tables.alx.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressDialog;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class AlxDeleteStatsDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private Button deleteBtn;

  @FXML
  private HBox emulatorWrapper;

  @FXML
  private CheckBox timePlayedCheckbox;

  @FXML
  private CheckBox numberPlaysCheckbox;

  @FXML
  private CheckBox recordScoresCheckbox;

  @FXML
  private CheckBox confirmationCheckbox;

  @FXML
  private ComboBox<GameEmulatorRepresentation> emulatorCombo;
  private GameRepresentation gameRepresentation;

  @FXML
  private void onDeleteClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    boolean deleteTime = timePlayedCheckbox.isSelected();
    boolean deletePlays = numberPlaysCheckbox.isSelected();
    boolean deleteScores = numberPlaysCheckbox.isSelected();

    if (gameRepresentation != null) {
      Platform.runLater(() -> {
        AlxDeleteGameStatsProgressModel deletingTableStatistics = new AlxDeleteGameStatsProgressModel("Deleting Table Statistic", gameRepresentation, deleteTime, deletePlays, deleteScores);
        ProgressDialog.createProgressDialog(deletingTableStatistics);
      });
    }
    else {
      int emulatorId = this.emulatorCombo.getSelectionModel().getSelectedItem().getId();
      stage.close();

      Platform.runLater(() -> {
        AlxDeleteStatsProgressModel deletingTableStatistics = new AlxDeleteStatsProgressModel("Deleting Table Statistics", emulatorId, deleteTime, deletePlays, deleteScores);
        ProgressDialog.createProgressDialog(deletingTableStatistics);
        EventManager.getInstance().notifyAlxUpdate(gameRepresentation);
      });
    }
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    emulatorWrapper.managedProperty().bindBidirectional(emulatorWrapper.visibleProperty());

    deleteBtn.setDisable(true);

    UISettings uiSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.UI_SETTINGS, UISettings.class);
    List<GameEmulatorRepresentation> emulators = new ArrayList<>(client.getEmulatorService().getGameEmulatorsUncached());
    List<GameEmulatorRepresentation> filtered = emulators.stream().filter(e -> !uiSettings.getIgnoredEmulatorIds().contains(Integer.valueOf(e.getId()))).collect(Collectors.toList());

    GameEmulatorRepresentation allTables = new GameEmulatorRepresentation();
    allTables.setId(-1);
    allTables.setName("All Tables");
    filtered.add(0, allTables);

    this.emulatorCombo.setItems(FXCollections.observableList(filtered));
    this.emulatorCombo.getSelectionModel().select(0);

    numberPlaysCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        refresh();
      }
    });

    timePlayedCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        refresh();
      }
    });

    recordScoresCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        refresh();
      }
    });

    confirmationCheckbox.selectedProperty().addListener(new ChangeListener<Boolean>() {
      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        refresh();
      }
    });
  }


  public void setData(@Nullable GameRepresentation gameRepresentation) {
    this.gameRepresentation = gameRepresentation;
    this.emulatorWrapper.setVisible(gameRepresentation == null);
  }

  private void refresh() {
    this.deleteBtn.setDisable((!numberPlaysCheckbox.isSelected() && !timePlayedCheckbox.isSelected() && !recordScoresCheckbox.isSelected()) || !confirmationCheckbox.isSelected());
  }

  @Override
  public void onDialogCancel() {

  }
}
