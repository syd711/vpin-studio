package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.FileUtils;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.textedit.TextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
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
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class TextEditorController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(TextEditorController.class);

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

  @FXML
  private Label size;

  @FXML
  private Label lastModified;

  private RichText richText;
  private VPinFile file;

  private boolean saved = false;

  @FXML
  private void onSave(ActionEvent e) {
    Stage stage = (Stage) ((Button) e.getSource()).getScene().getWindow();

    saved = true;
    this.saveBtn.setDisable(true);
    this.saveAndCloseBtn.setDisable(true);
    this.closeBtn.setDisable(true);

    try {
      TextFile save = client.getTextEditorService().save(file, this.richText.getCodeArea().getText());
      lastModified.setText(DateFormat.getDateTimeInstance().format(save.getLastModified()));
      size.setText(FileUtils.readableFileSize(save.getSize()));
    } catch (Exception ex) {
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

  public void load(VPinFile file) {
    this.file = file;
    TextFile value = client.getTextEditorService().getText(file);
    lastModified.setText(DateFormat.getDateTimeInstance().format(value.getLastModified()));
    size.setText(FileUtils.readableFileSize(value.getSize()));

    richText = new RichText(value.getContent());

    VirtualizedScrollPane scrollPane = new VirtualizedScrollPane(richText.getCodeArea());
    centerPane.setCenter(scrollPane);


    this.saveBtn.setDisable(false);
    this.saveAndCloseBtn.setDisable(false);
  }

  @Override
  public void onDialogCancel() {

  }

  public boolean isSaved() {
    return saved;
  }
}