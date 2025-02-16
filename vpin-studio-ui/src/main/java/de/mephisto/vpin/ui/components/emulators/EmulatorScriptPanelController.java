package de.mephisto.vpin.ui.components.emulators;

import de.mephisto.vpin.commons.fx.Debouncer;
import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
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

public class EmulatorScriptPanelController implements Initializable, IEmulatorScriptPanel {
  private final static Logger LOG = LoggerFactory.getLogger(EmulatorScriptPanelController.class);

  private final static List<String> KEYWORDS = Arrays.asList("[DIREMU]", "[DIRGAME]", "[DIRROM]", "[GAMEFULLNAME]", "[GAMENAME]", "[GAMEEXT]", "[STARTDIR]", "[CUSTOM1]", "[CUSTOM2]", "[CUSTOM3]", "[ALTEXE]", "[ALTMODE]", "[MEDIADIR]", "[PLAYLISTID]", "[TOURID]");

  @FXML
  private Node root;

  @FXML
  private ListView<String> keywordList;

  @FXML
  private Button insertBtn;

  @FXML
  private TextArea scriptText;
  private Optional<GameEmulatorScript> script;

  @FXML
  private void onInsert() {
    String selectedItem = keywordList.getSelectionModel().getSelectedItem();
    if (selectedItem != null) {
      scriptText.insertText(scriptText.getCaretPosition(), selectedItem);
    }
  }

  public void setData(Optional<GameEmulatorRepresentation> emulator, Optional<GameEmulatorScript> script) {
    this.script = script;
    this.keywordList.setDisable(script.isEmpty());
    this.scriptText.setDisable(script.isEmpty());
    this.insertBtn.setDisable(script.isEmpty());

    this.scriptText.setText("");
    if (script.isPresent()) {
      scriptText.setText(script.get().getScript());
    }
  }

  @Override
  public void applyValues() {
    this.script.get().setScript(scriptText.getText());
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    keywordList.setItems(FXCollections.observableList(KEYWORDS));

  }
}
