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

  private JobDescriptor job;

  private boolean finalizedJob = false;
  private JobPoller poller;

  @FXML
  private void onOpen() {
    if (job.getGameId() > 0) {
      NavigationOptions options = new NavigationOptions(job.getGameId());
      NavigationController.navigateTo(NavigationItem.Tables, options);
    }
  }

  @FXML
  private void onRemove() {
    if (job.isFinished()) {
      client.getJobsService().dismiss(job.getUuid());
      removeBtn.setDisable(true);
      poller.refreshJobsUI();
    }
  }

  @FXML
  private void onStop() {
    client.getJobsService().cancel(job.getUuid());
    stopBtn.setDisable(true);

    EventManager.getInstance().notifyJobFinished(job.getJobType(), job.getGameId());
  }

  public void setData(JobPoller poller, JobDescriptor job) {
    this.poller = poller;
    if (finalizedJob) {
      return;
    }

    this.job = job;
    Platform.runLater(() -> {
      nameLabel.setText(this.job.getTitle());
      nameLabel.setStyle("-fx-font-size: 15px;-fx-font-weight: bold;");
      nameLabel.setTooltip(new Tooltip(job.getTitle()));
      infoLabel.setText(job.getStatus());
      infoLabel.setStyle("-fx-font-size: 13px");
      openBtn.setVisible(job.getGameId() > 0);

      if (!job.isCancelable()) {
        stopBtn.setDisable(true);
        stopBtn.setTooltip(new Tooltip("This job can not be canceled."));
      }


      if (job.getProgress() <= 0) {
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
      }
      else {
        progressBar.setProgress(job.getProgress());
        int progress = (int) (job.getProgress() * 100);
        progressBar.setTooltip(new Tooltip(progress + "%"));
      }

      if (job.getProgress() == 1 || job.getError() != null) {
        LOG.info("Finalizing job container: " + job);
        finalizedJob = true;

        boolean error = job.getError() != null;
        statusLabel.setVisible(true);
        if (error) {
          statusLabel.setGraphic(WidgetFactory.createExclamationIcon());
          statusLabel.setText(job.getError());
          statusLabel.setTooltip(new Tooltip(job.getError()));
        }
        else if (job.isCancelled()) {
          statusLabel.setGraphic(WidgetFactory.createExclamationIcon());
          statusLabel.setText("The job has been cancelled.");
        }
        else {
          statusLabel.setGraphic(WidgetFactory.createCheckboxIcon(WidgetFactory.OK_COLOR));
          statusLabel.setText("Job successful.");
        }

        infoLabel.setVisible(false);
        progressBar.setVisible(false);
        openBtn.setVisible(job.getGameId() > 0);
        openBtn.setDisable(job.getGameId() <= 0);
        stopBtn.setDisable(true);
        removeBtn.setDisable(false);


        EventManager.getInstance().notifyJobFinished(job.getJobType(), job.getGameId());
      }
      else if (job.getProgress() > 0) {
        progressBar.setProgress(job.getProgress());
      }
    });
  }

  public JobDescriptor getDescriptor() {
    return job;
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
