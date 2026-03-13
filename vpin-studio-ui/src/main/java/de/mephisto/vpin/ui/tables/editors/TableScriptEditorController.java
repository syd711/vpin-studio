package de.mephisto.vpin.ui.tables.editors;

import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TablesController;
import de.mephisto.vpin.ui.util.RichText;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.net.URL;
import java.util.ResourceBundle;

public class TableScriptEditorController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @FXML
  private BorderPane root;

  @FXML
  private BorderPane centerPane;

  @FXML
  private TextField textfieldSearch;

  @FXML
  private Button saveBtn;

  @FXML
  private Button saveAndCloseBtn;

  @FXML
  private Button closeBtn;

  private TablesController tablesController;
  private GameRepresentation game;
  private RichText richText;

  @FXML
  private void onClose() {
    this.tablesController.getEditorRootStack().getChildren().remove(root);
  }

  @FXML
  private void onSave() {
    this.saveBtn.setDisable(true);
    this.saveAndCloseBtn.setDisable(true);
    this.closeBtn.setDisable(true);
    CodeArea codeArea = richText.getCodeArea();
    String text = codeArea.getText();
    Studio.client.getVpxService().saveTableSource(game, text);

    this.closeBtn.setDisable(false);
  }

  @FXML
  private void onSaveAndClose() {
    onSave();
    onClose();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.saveBtn.setDisable(true);
    this.saveAndCloseBtn.setDisable(true);
  }

  @FXML
  private void onSearchKeyPressed(KeyEvent e) {
    String term = textfieldSearch.getText();
    if (e.getCode().equals(KeyCode.ENTER)) {
      if (!StringUtils.isEmpty(term)) {
        CodeArea codeArea = richText.getCodeArea();
        String text = codeArea.getText();

        int pos = codeArea.getCaretPosition();
        String relativeText = text.substring(pos + term.length());
        int i = relativeText.indexOf(term);
        if (i != -1) {
          codeArea.moveTo(pos + i);
        }
      }
    }
  }

  public void setGame(@NonNull GameRepresentation game, String source) {
    this.game = game;
    richText = new RichText(source);

    VirtualizedScrollPane scrollPane = new VirtualizedScrollPane(richText.getCodeArea());
    centerPane.setCenter(scrollPane);
  }

  public void setTablesController(TablesController tablesController) {
    this.tablesController = tablesController;
  }
}
