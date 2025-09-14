package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.textedit.MonitoredTextFile;
import de.mephisto.vpin.ui.util.RichText;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DateFormat;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TextEditorController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TextEditorController.class);

  @FXML
  private GridPane dataGrid;

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

  @FXML
  private Label size;

  @FXML
  private Label lastModified;

  private RichText richText;
  private MonitoredTextFile file;

  private boolean saved = false;

  @FXML
  private void onSave(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    saved = true;
    this.saveBtn.setDisable(true);
    this.saveAndCloseBtn.setDisable(true);
    this.closeBtn.setDisable(true);

    try {
      file.setContent(this.richText.getCodeArea().getText());
      MonitoredTextFile save = client.getTextEditorService().save(file);
      lastModified.setText(DateFormat.getDateTimeInstance().format(save.getLastModified()));
      size.setText(FileUtils.readableFileSize(save.getSize()));
    }
    catch (Exception ex) {
      LOG.error("Failed to save file: {} ", ex.getMessage(), ex);
      WidgetFactory.showAlert(stage, "Error", "Error saving file: " + ex.getMessage());
    }

    this.saveBtn.setDisable(false);
    this.saveAndCloseBtn.setDisable(false);
    this.closeBtn.setDisable(false);
  }

  @FXML
  private void onSaveAndClose(ActionEvent e) {
    onSave(e);
    onCancelClick(e);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    this.saveBtn.setDisable(true);
    this.saveAndCloseBtn.setDisable(true);
    this.saveBtn.managedProperty().bindBidirectional(saveBtn.visibleProperty());
    this.saveAndCloseBtn.managedProperty().bindBidirectional(saveAndCloseBtn.visibleProperty());
  }

  @FXML
  private void onSearchKeyPressed(KeyEvent e) {
    String term = textfieldSearch.getText();
    if (e.getCode().equals(KeyCode.ENTER)) {
      if (!StringUtils.isEmpty(term)) {
        CodeArea codeArea = richText.getCodeArea();
        String text = codeArea.getText();

        int pos = codeArea.getCaretPosition();
        int length = pos + term.length();
        if (length < text.length()) {
          String relativeText = text.substring(length);
          int i = relativeText.indexOf(term);
          if (i != -1) {
            codeArea.moveTo(pos + i);
          }
        }
      }
    }
  }

  @FXML
  private void onCancelClick(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();
    stage.close();
  }

  public void load(MonitoredTextFile file) throws Exception {
    this.file = file;
    if(file.getvPinFile() == null) {
      richText = new RichText(file.getContent());
      richText.getCodeArea().setEditable(false);
      size.setVisible(false);
      saveBtn.setVisible(false);
      saveAndCloseBtn.setVisible(false);
      dataGrid.setVisible(false);
    }
    else {
      MonitoredTextFile value = client.getTextEditorService().getText(file);
      lastModified.setText(DateFormat.getDateTimeInstance().format(value.getLastModified()));
      size.setText(FileUtils.readableFileSize(value.getSize()));
      richText = new RichText(value.getContent());

      this.saveBtn.setDisable(false);
      this.saveAndCloseBtn.setDisable(false);
    }

    VirtualizedScrollPane scrollPane = new VirtualizedScrollPane(richText.getCodeArea());
    centerPane.setCenter(scrollPane);
  }

  @Override
  public void onDialogCancel() {

  }

  public boolean isSaved() {
    return saved;
  }
}
