package de.mephisto.vpin.ui.cards.panels;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.cards.CardTemplate;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import de.mephisto.vpin.ui.util.UploadProgressModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class DialogTemplateEditorUploadProgressModel extends UploadProgressModel {
  private final static Logger LOG = LoggerFactory.getLogger(DialogTemplateEditorUploadProgressModel.class);

  private final String templateName;

  public DialogTemplateEditorUploadProgressModel(String title, File file, String templateName) {
    super(file, title);
    this.templateName = templateName;
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, File next) {
    try {
      CardTemplate newTemplate = Studio.client.getHighscoreCardTemplatesClient().uploadTemplate(templateName, next);
      if (newTemplate != null) {
        progressResultModel.addProcessed(newTemplate);
      }
      else {
        progressResultModel.addError();
      }
    }
    catch (Exception e) {
      LOG.error("template upload failed: " + e.getMessage(), e);
      Platform.runLater(() -> {
        WidgetFactory.showAlert(Studio.stage, "Error", "Cards Template upload failed: " + e.getMessage());
      });
    }
  }
}
