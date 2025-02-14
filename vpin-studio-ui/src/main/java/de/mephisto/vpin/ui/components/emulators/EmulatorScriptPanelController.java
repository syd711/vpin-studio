package de.mephisto.vpin.ui.components.emulators;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.restclient.games.GameEmulatorRepresentation;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class EmulatorScriptPanelController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(EmulatorScriptPanelController.class);
  private final Debouncer debouncer = new Debouncer();

  private final static List<String> KEYWORDS = Arrays.asList("[DIREMU]", "[DIRGAME]", "[DIRROM]", "[GAMEFULLNAME]", "[GAMENAME]", "[GAMEEXT]", "[STARTDIR]", "[CUSTOM1]", "[CUSTOM2]", "[CUSTOM3]", "[ALTEXE]", "[ALTMODE]", "[MEDIADIR]", "[PLAYLISTID]", "[TOURID]");

  @FXML
  private Node root;

  @FXML
  private ListView<String> keywordList;

  @FXML
  private Button insertBtn;

  @FXML
  private TextArea scriptText;

  private Consumer<String> callback;

  private Optional<GameEmulatorRepresentation> model;

  private boolean editingEnabled = false;

  @FXML
  private void onInsert() {
    String selectedItem = keywordList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      scriptText.insertText(scriptText.getCaretPosition(), selectedItem);
    }
  }

  public void setData(Optional<GameEmulatorRepresentation> model, String value) {
    editingEnabled = false;
    this.model = model;
    this.keywordList.setDisable(model.isEmpty());
    this.scriptText.setDisable(model.isEmpty());
    this.insertBtn.setDisable(model.isEmpty());

    this.scriptText.setText("");
    if (model.isPresent()) {
      scriptText.setText(value);
    }
    editingEnabled = true;
  }

  public void setCallback(Consumer<String> callback) {
    this.callback = callback;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    keywordList.setItems(FXCollections.observableList(KEYWORDS));

    scriptText.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (!editingEnabled) {
          return;
        }
        debouncer.debounce("script", () -> {
          callback.accept(newValue);
        }, 500);
      }
    });
  }
}
