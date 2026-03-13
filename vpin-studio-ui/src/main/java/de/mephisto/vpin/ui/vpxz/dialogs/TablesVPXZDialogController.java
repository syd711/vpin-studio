package de.mephisto.vpin.ui.vpxz.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.VPXZExportDescriptor;
import de.mephisto.vpin.restclient.preferences.VPXZSettings;
import de.mephisto.vpin.restclient.vpxz.VPXZSourceRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.jobs.JobPoller;
import de.mephisto.vpin.ui.util.ProgressDialog;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

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

  @FXML
  private ComboBox<String> filesCombo;

  private GameRepresentation game;
  private List<String> vpxStandaloneFiles;
  private List<String> entries;

  @FXML
  private void onExportClick(ActionEvent e) throws Exception {
    VPXZSourceRepresentation source = sourceCombo.getValue();

    VPXZExportDescriptor descriptor = new VPXZExportDescriptor();
    descriptor.setSourceId(source.getId());
    descriptor.setGameId(game.getId());
    descriptor.setVpxStandaloneFile(filesCombo.getSelectionModel().getSelectedItem());
    Studio.client.getVpxzService().createVpxzFile(descriptor);

    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();

    new Thread(() -> {
      try {
        Thread.sleep(1000);
      }
      catch (InterruptedException ex) {
        //ignore
      }
      Platform.runLater(() -> {
        JobPoller.getInstance().setPolling();
      });
    }).start();
  }


  @FXML
  private void onSynchronize(ActionEvent event) {
    ProgressDialog.createProgressDialog(new VPXZSyncProgressModel("VPX Standalone File Synchronization"));
    vpxStandaloneFiles = client.getVpxzService().getVpxStandaloneFiles(false);
    List<String> entries = new ArrayList<>(vpxStandaloneFiles);
    entries.add(0, "");
    this.filesCombo.setItems(FXCollections.observableList(entries));
  }

  @FXML
  private void onHyperlink(ActionEvent e) {
    Hyperlink link = (Hyperlink) e.getSource();
    String linkText = link.getText();
    Studio.browse(linkText);
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

    vpxStandaloneFiles = client.getVpxzService().getVpxStandaloneFiles(false);
    if (vpxStandaloneFiles.isEmpty()) {
      ProgressDialog.createProgressDialog(new VPXZSyncProgressModel("VPX Standalone File Synchronization"));
      vpxStandaloneFiles = client.getVpxzService().getVpxStandaloneFiles(false);
    }

    entries = new ArrayList<>(vpxStandaloneFiles);
    entries.add(0, "");
    this.filesCombo.setItems(FXCollections.observableList(entries));


    filesCombo.getEditor().textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if(!StringUtils.isEmpty(newValue)) {
          List<String> filtered = entries.stream().filter(e -> e.toLowerCase().contains(newValue.toLowerCase())).collect(Collectors.toList());
          filesCombo.setItems(FXCollections.observableList(filtered));
        }
        else {
          filesCombo.setItems(FXCollections.observableList(entries));
        }
      }
    });
  }

  @Override
  public void onDialogCancel() {

  }

  public void setGames(GameRepresentation game) {
    this.game = game;
    this.titleLabel.setText(game.getGameDisplayName());
  }
}
