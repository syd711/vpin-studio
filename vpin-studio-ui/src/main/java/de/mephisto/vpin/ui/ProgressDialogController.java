package de.mephisto.vpin.ui;

import de.mephisto.vpin.commons.fx.DialogController;
import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

public class ProgressDialogController implements Initializable, DialogController {
  private final static Logger LOG = LoggerFactory.getLogger(ProgressDialogController.class);

  @FXML
  private Label titleLabel;

  @FXML
  private Label progressBarLabel;

  @FXML
  private ProgressBar progressBar;

  @FXML
  private Button cancelButton;

  @FXML
  private Button backgroundButton;

  private boolean runsInBackground = false;

  private Service service;
  private ProgressResultModel progressResultModel;
  private ProgressModel model;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    backgroundButton.setVisible(false);
    cancelButton.setOnAction(event -> {
      this.onDialogCancel();

      Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
      stage.close();
    });

    backgroundButton.setOnAction(event -> {
      runsInBackground = true;
      Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
      stage.close();
    });
  }

  public void setProgressModel(Stage stage, ProgressModel model) {
    this.model = model;
    titleLabel.setText(model.getTitle());
    cancelButton.setVisible(model.isCancelable());

    if (model.isIndeterminate()) {
      progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
    }

    progressResultModel = new ProgressResultModel(progressBar, progressBarLabel);
    progressBarLabel.setText("");
    service = new Service() {
      @Override
      protected Task createTask() {
        return new Task() {
          @Override
          protected Object call() throws Exception {
            int index = 1;

            try {
              while (model.hasNext() && !this.isCancelled()) {
                Object next = model.getNext();
                final int uiIndex = index;

                if (!runsInBackground) {
                  Platform.runLater(() -> {
                    String label = model.nextToString(next);
                    if (label != null) {
                      String progressText = model.nextToString(next);
                      if(!StringUtils.isEmpty(progressText)) {
                        progressBarLabel.setText(progressText);
                      }
                      else {
                        progressBarLabel.setText("");
                      }
                    }
                    String title = model.getTitle();
                    if (model.getMax() > 1 && model.isShowSteps()) {
                      title = title + " (" + uiIndex + "/" + model.getMax() + ")";
                    }
                    titleLabel.setText(title);
                  });
                }

                model.processNext(progressResultModel, next);
                if (!model.isIndeterminate()) {
                  long percent = index * 100 / model.getMax();
                  updateProgress(percent, 100);
                }

                if (!runsInBackground) {
                  Platform.runLater(() -> {
                    if (!model.isIndeterminate()) {
                      double p = Double.valueOf(uiIndex) / model.getMax();
                      progressBar.setProgress(p);
                    }
                  });
                }
                index++;
              }

              Platform.runLater(() -> {
                if (!runsInBackground) {
                  stage.close();
                }
                model.finalizeModel(progressResultModel);

                if (model.isShowSummary()) {
                  Platform.runLater(() -> {
                    String msg = model.getTitle() + " finished.";
                    WidgetFactory.showInformation(Studio.stage, msg, "Processed " + progressResultModel.getProcessed() + " of " + model.getMax() + " elements.");
                  });
                }
              });
            } catch (Exception e) {
              LOG.error("Error in Progress Dialog model: " + e.getMessage(), e);
              model.finalizeModel(progressResultModel);
              if (!runsInBackground) {
                Platform.runLater(() -> {
                  stage.close();
                });
              }
              Platform.runLater(() -> {
                WidgetFactory.showAlert(Studio.stage, "Error", "Error in progressing: " + e.getMessage());
              });
            }
            return null;
          }
        };
      }
    };
    service.start();
  }

  @Override
  public void onDialogCancel() {
    service.cancel();
    progressResultModel.setCancelled(true);

    if(this.model != null) {
      model.cancel();
    }
  }

  public ProgressResultModel getProgressResult() {
    return this.progressResultModel;
  }
}
