package de.mephisto.vpin.ui.vpxz.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.VPXZExportDescriptor;
import de.mephisto.vpin.restclient.preferences.VPXZSettings;
import de.mephisto.vpin.restclient.vpxz.VPXZSourceRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.jobs.JobPoller;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class TablesVPXZDialogController implements Initializable, DialogController {

  @FXML
  private Label titleLabel;

  @FXML
  private CheckBox overwriteCheckbox;

  @FXML
  private ComboBox<VPXZSourceRepresentation> sourceCombo;


  private List<GameRepresentation> games;

  @FXML
  private void onExportClick(ActionEvent e) throws Exception {
    VPXZSourceRepresentation source = sourceCombo.getValue();

    VPXZExportDescriptor descriptor = new VPXZExportDescriptor();
    descriptor.setSourceId(source.getId());
    descriptor.getGameIds().addAll(games.stream().map(GameRepresentation::getId).collect(Collectors.toList()));
    Studio.client.getVpxzService().createVpxzFile(descriptor);

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();

    new Thread(() -> {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ex) {
        //ignore
      }
      Platform.runLater(() -> {
        JobPoller.getInstance().setPolling();
      });
    }).start();
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  private void refreshImportsSelection(VPXZSettings vpxzSettings) {
    overwriteCheckbox.setSelected(vpxzSettings.isOverwriteFile());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    List<VPXZSourceRepresentation> repositories = new ArrayList<>(client.getVpxzService().getVPXZSources());
    sourceCombo.setItems(FXCollections.observableList(repositories));
    sourceCombo.getSelectionModel().select(0);

    VPXZSettings vpxzSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.VPXZ_SETTINGS, VPXZSettings.class);
    refreshImportsSelection(vpxzSettings);

    overwriteCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      vpxzSettings.setOverwriteFile(newValue);
      client.getPreferenceService().setJsonPreference(vpxzSettings);
    });
  }

  @Override
  public void onDialogCancel() {

  }

  public void setGames(List<GameRepresentation> games) {
    this.games = games;
    if(games.size() == 1) {
      this.titleLabel.setText(games.get(0).getGameDisplayName());
    }
    else {
      this.titleLabel.setText("Creating .vpxz package of " + games.size() + " tables");
    }
  }
}
