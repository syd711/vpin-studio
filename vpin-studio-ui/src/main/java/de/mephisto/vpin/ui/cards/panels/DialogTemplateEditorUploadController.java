package de.mephisto.vpin.ui.cards.panels;

import org.apache.commons.lang3.StringUtils;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.ui.tables.dialogs.BaseUploadController;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DialogTemplateEditorUploadController extends BaseUploadController {

  @FXML
  private TextField templateNameField;

  private TemplateEditorController controller;

  public DialogTemplateEditorUploadController() {
    super(AssetType.CARD_ASSET, true, false, "json", "zip", "7z", "rar");
  }

  public void setData(Stage stage, TemplateEditorController controller, String gameName) {
    super.stage = stage;
    this.controller = controller;
    templateNameField.setText(gameName);
  }


  @Override
  protected UploadProgressModel createUploadModel() {
    String templateName = templateNameField.getText();
    if (StringUtils.isEmpty(templateName) || controller.checkDuplicate(templateName)) {
      return null;
    }
    // else
    return new DialogTemplateEditorUploadProgressModel("Config File Upload", getSelection(), templateName);
  }

  @Override
  protected void onUploadDone(ProgressResultModel result) {
    if (result.isSuccess()) {
      CardTemplate newTemplate = result.getFirstTypedResult();
      controller.doOnCreate(newTemplate);
    }
  }

}
