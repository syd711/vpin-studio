package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Lets the user pick a table from the list of all tables for a bulk import row.
 */
public class BulkImporterTableSearchController implements Initializable, DialogController {

  @FXML
  private TextField filterField;
  @FXML
  private ListView<GameRepresentation> listView;
  @FXML
  private Button okBtn;

  private FilteredList<GameRepresentation> filtered;
  private GameRepresentation result;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    listView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> okBtn.setDisable(n == null));
    listView.setOnMouseClicked(e -> {
      if (e.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
        onOkClick(null);
      }
    });
    filterField.textProperty().addListener((obs, o, n) -> {
      if (filtered == null) {
        return;
      }
      String q = n == null ? "" : n.toLowerCase().trim();
      filtered.setPredicate(g -> q.isEmpty()
          || StringUtils.defaultString(g.getGameDisplayName()).toLowerCase().contains(q)
          || StringUtils.defaultString(g.getGameFileName()).toLowerCase().contains(q));
    });
    Platform.runLater(() -> filterField.requestFocus());
  }

  public void setGames(List<GameRepresentation> games) {
    filtered = new FilteredList<>(FXCollections.observableArrayList(games));
    listView.setItems(filtered);
  }

  public GameRepresentation getResult() {
    return result;
  }

  @FXML
  private void onOkClick(ActionEvent e) {
    result = listView.getSelectionModel().getSelectedItem();
    close();
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    result = null;
    close();
  }

  private void close() {
    ((Stage) listView.getScene().getWindow()).close();
  }

  @Override
  public void onDialogCancel() {
    result = null;
  }
}
