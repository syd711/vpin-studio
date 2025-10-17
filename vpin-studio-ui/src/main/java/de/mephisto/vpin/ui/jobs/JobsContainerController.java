package de.mephisto.vpin.ui.jobs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.ui.NavigationController;
import de.mephisto.vpin.ui.NavigationItem;
import de.mephisto.vpin.ui.NavigationOptions;
import de.mephisto.vpin.ui.events.EventManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

import static de.mephisto.vpin.ui.Studio.client;

public class JobsContainerController implements Initializable {
  private final static Logger LOG = LoggerFactory.getLogger(JobsContainerController.class);

  @FXML
  private Label nameLabel;

  @FXML
  private Label infoLabel;

  @FXML
  private Button stopBtn;

  @FXML
  private Button removeBtn;

  @FXML
  private Button openBtn;

  @FXML
  private ProgressBar progressBar;

  @FXML
  private Label statusLabel;

  private JobDescriptor jobDescriptor;

  private boolean finalizedJob = false;
  private boolean cancelled = false;
  private JobPoller poller;

  @FXML
  private void onOpen() {
    if (jobDescriptor.getGameId() > 0) {
      NavigationOptions options = new NavigationOptions(jobDescriptor.getGameId());
      NavigationController.navigateTo(NavigationItem.Tables, options);
    }
  }

  @FXML
  private void onRemove() {
    if (jobDescriptor.isFinished()) {
      client.getJobsService().dismiss(jobDescriptor.getUuid());
      poller.dismiss(jobDescriptor);
      removeBtn.setDisable(true);
      poller.refreshJobsUI();
    }
  }

  @FXML
  private void onStop() {
    cancelled = true;
    client.getJobsService().cancel(jobDescriptor.getUuid());
    stopBtn.setDisable(true);
    setCancelled();

    poller.notifyJobFinished(jobDescriptor);
  }

  public void setCancelled() {
    this.progressBar.setProgress(1);
    statusLabel.setText("");
    statusLabel.setGraphic(WidgetFactory.createExclamationIcon());
    statusLabel.setText("The job has been cancelled.");
  }

  public void setData(JobPoller poller, JobDescriptor jobDescriptor) {
    this.poller = poller;
    if (finalizedJob) {
      return;
    }

    this.jobDescriptor = jobDescriptor;
    Platform.runLater(() -> {
      nameLabel.setText(this.jobDescriptor.getTitle());
      nameLabel.setStyle("-fx-font-size: 15px;-fx-font-weight: bold;");
      nameLabel.setTooltip(new Tooltip(jobDescriptor.getTitle()));
      infoLabel.setText(jobDescriptor.getStatus());
      infoLabel.setStyle("-fx-font-size: 13px");
      openBtn.setVisible(jobDescriptor.getGameId() > 0);

      if (!jobDescriptor.isCancelable()) {
        stopBtn.setDisable(true);
        stopBtn.setTooltip(new Tooltip("This job can not be canceled."));
      }


      if (jobDescriptor.getProgress() <= 0) {
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
      }
      else {
        progressBar.setProgress(jobDescriptor.getProgress());
        int progress = (int) (jobDescriptor.getProgress() * 100);
        progressBar.setTooltip(new Tooltip(progress + "%"));
      }

      if (jobDescriptor.getProgress() == 1 || jobDescriptor.getError() != null) {
        finalizedJob = true;

        boolean error = jobDescriptor.getError() != null;
        statusLabel.setVisible(true);
        if (error) {
          statusLabel.setGraphic(WidgetFactory.createExclamationIcon());
          statusLabel.setText(jobDescriptor.getError());
          statusLabel.setTooltip(new Tooltip(jobDescriptor.getError()));
        }
        else if (jobDescriptor.isCancelled() || cancelled) {
          setCancelled();
        }
        else {
          statusLabel.setGraphic(WidgetFactory.createCheckboxIcon(WidgetFactory.OK_COLOR));
          statusLabel.setText("Job successful.");
        }

        infoLabel.setVisible(false);
        progressBar.setVisible(false);
        openBtn.setVisible(jobDescriptor.getGameId() > 0);
        openBtn.setDisable(jobDescriptor.getGameId() <= 0);
        stopBtn.setDisable(true);
        removeBtn.setDisable(false);


        poller.notifyJobFinished(jobDescriptor);
      }
      else if (jobDescriptor.getProgress() > 0) {
        progressBar.setProgress(jobDescriptor.getProgress());
      }

      if (jobDescriptor.isCancelled()) {
        setCancelled();
      }
    });
  }

  public JobDescriptor getDescriptor() {
    return jobDescriptor;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    openBtn.managedProperty().bindBidirectional(openBtn.visibleProperty());
    statusLabel.managedProperty().bindBidirectional(statusLabel.visibleProperty());
    infoLabel.managedProperty().bindBidirectional(infoLabel.visibleProperty());
    progressBar.managedProperty().bindBidirectional(progressBar.visibleProperty());
    statusLabel.setVisible(false);
    openBtn.setDisable(true);
    removeBtn.setDisable(true);
  }
}
