package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.popper.PopperScreen;
import de.mephisto.vpin.restclient.representations.GameMediaItemRepresentation;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.MediaView;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


public class TablePopperMediaSelectionController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TablePopperMediaSelectionController.class);

  @FXML
  private BorderPane mediaPane;

  @FXML
  private Button searchBtn;

  @FXML
  private Button previewBtn;

  @FXML
  private Button downloadBtn;

  @FXML
  private ProgressBar progressBar;

  @FXML
  private TextField searchField;

  @FXML
  private VBox helpBox;


  @FXML
  private ListView<GameMediaItemRepresentation> assetList;

  private GameRepresentation game;
  private PopperScreen screen;
  private TablesSidebarController tablesSidebarController;

  @FXML
  private void onSearch() {
    GameMediaItemRepresentation selectedItem = assetList.getSelectionModel().getSelectedItem();

  }

  @FXML
  private void onPreview() {
    GameMediaItemRepresentation selectedItem = assetList.getSelectionModel().getSelectedItem();

  }

  @FXML
  private void onDownload() {
  }

  @FXML
  private void onCancel(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    previewBtn.setDisable(true);
    downloadBtn.setDisable(true);
    progressBar.setDisable(true);
  }

  private void disposeAll() {
    Node center = mediaPane.getCenter();
    if (center instanceof MediaView) {
      MediaView mediaView = (MediaView) center;
      mediaView.getMediaPlayer().stop();
      mediaView.getMediaPlayer().dispose();
    }
    else if (center instanceof VBox) {
      MediaView mediaView = (MediaView) ((VBox) center).getChildren().get(1);
      mediaView.getMediaPlayer().stop();
      mediaView.getMediaPlayer().dispose();
    }

    mediaPane.setCenter(null);
  }

  @Override
  public void onDialogCancel() {

  }


  public void setGame(GameRepresentation game, PopperScreen screen) {
    this.game = game;
    this.screen = screen;

    String term = game.getGameDisplayName();
    term = term.replaceAll("the", "");
    term = term.replaceAll("The", "");
    term = term.replaceAll(", ", "");
    term = term.replaceAll("-", "");
    term = term.replaceAll("'", "");
    term = term.replaceAll("\\(", "");
    term = term.replaceAll("\\)", "");
    term = term.replaceAll("\\[", "");
    term = term.replaceAll("\\]", "");
    term = term.replaceAll("MOD", "");
    term = term.replaceAll("VOW", "");
    term = term.replaceAll("VR ", "");
    term = term.replaceAll("Room ", "");

    String[] terms = term.split(" ");

    List<String> sanitizedTerms = new ArrayList<>();
    for (String s : terms) {
      if (!StringUtils.isEmpty(s)) {
        String value = s.trim();
        try {
          if(value.length() == 4) {
            Integer.parseInt(value);
            continue;
          }
        } catch (NumberFormatException e) {
        }

        sanitizedTerms.add(s.trim());
      }

      if (sanitizedTerms.size() == 2) {
        break;
      }
    }

    if (sanitizedTerms.isEmpty()) {
      this.searchField.setText(game.getGameDisplayName());
    }
    else {
      this.searchField.setText(String.join(" ", sanitizedTerms));
    }

    onSearch();
  }

  public void setTableSidebarController(TablesSidebarController tablesSidebarController) {
    this.tablesSidebarController = tablesSidebarController;
  }
}
