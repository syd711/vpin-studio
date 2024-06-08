package de.mephisto.vpin.ui.jobs;

import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.messaging.MessageContainer;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class JobPoller {
  private final static Logger LOG = LoggerFactory.getLogger(JobPoller.class);

  private static JobPoller instance;

  private final MenuButton jobMenu;
  private final ProgressIndicator jobProgress;
  private final MenuButton messagesMenu;

  private final List<JobDescriptor> activeJobs = Collections.synchronizedList(new ArrayList<>());
  private final List<JobDescriptor> clientJobs = Collections.synchronizedList(new ArrayList<>());
  private final Service service;

  public static void destroy() {
    if (instance != null) {
      instance.service.cancel();
      instance = null;
    }
  }

  public static void create(MenuButton jobMenu, ProgressIndicator jobProgress, MenuButton messagesBtn) {
    if (instance == null) {
      instance = new JobPoller(jobMenu, jobProgress, messagesBtn);
    }
  }

  //TODO throw UI out!
  private JobPoller(MenuButton jobMenu, ProgressIndicator jobProgress, MenuButton messagesMenu) {
    this.jobMenu = jobMenu;
    this.jobProgress = jobProgress;
    this.messagesMenu = messagesMenu;
    this.jobMenu.setStyle("-fx-background-color: #111111;");
    this.messagesMenu.setStyle("-fx-background-color: #111111;");

    service = new Service() {
      @Override
      protected Task createTask() {
        return new Task() {
          @Override
          protected Object call() throws Exception {
            Thread.currentThread().setName("Job Polling Thread");
            LOG.info("Started JobPoller service.");
            boolean poll = true;

            //give the init some time
            List<JobDescriptor> jobs = new ArrayList<>(getActiveJobs());
            if (jobs.isEmpty()) {
              Thread.sleep(1000);
            }

            while (poll) {
              jobs = new ArrayList<>(getActiveJobs());
              refreshJobsUI(jobs);
              refreshMessagesUI(false);
              Thread.sleep(2000);
              poll = !jobs.isEmpty();
              LOG.info("JobPoller is waiting for " + jobs.size() + " running jobs.");
            }
            LOG.info("JobPoller finished all jobs");
            refreshJobsUI(Collections.emptyList());
            return true;
          }
        };
      }
    };

    service.onSucceededProperty().addListener((observable, oldValue, newValue) -> {
      service.reset();
      LOG.info("JobPoller has been resetted after success.");
    });

    setPolling();
  }

  public static JobPoller getInstance() {
    return instance;
  }

  //TODO throw UI out!
  public boolean isPolling() {
    return !jobMenu.isDisabled();
  }

  public void setPolling() {
    jobMenu.setDisable(false);
    jobProgress.setProgress(-1);

    if (!service.isRunning()) {
      service.restart();
    }
  }

  private void refreshJobsUI(List<JobDescriptor> updatedJobList) {
    Platform.runLater(() -> {
      boolean disable = updatedJobList.isEmpty();
      jobMenu.setDisable(disable);
      jobProgress.setProgress(disable ? 0 : -1);
      jobProgress.setVisible(!disable);

      if (jobMenu.isDisabled()) {
        jobMenu.getStyleClass().remove("action-selected");
        jobMenu.setText("No active jobs");
        jobMenu.setDisable(true);
      }
      else {
        if (!jobMenu.getStyleClass().contains("action-selected")) {
          jobMenu.getStyleClass().add("action-selected");
        }
        if (updatedJobList.size() == 1) {
          jobMenu.setText(updatedJobList.size() + " active job");
        }
        else {
          jobMenu.setText(updatedJobList.size() + " active jobs");
        }
      }

      List<MenuItem> items = new ArrayList<>(jobMenu.getItems());
      for (MenuItem item : items) {
        JobDescriptor descriptor = (JobDescriptor) item.getUserData();
        if (!updatedJobList.contains(descriptor)) {
          jobMenu.getItems().remove(item);
          EventManager.getInstance().notifyJobFinished(descriptor);
        }
      }

      activeJobs.clear();
      activeJobs.addAll(updatedJobList);

      for (JobDescriptor descriptor : updatedJobList) {
        if (items.stream().anyMatch(c -> c.getUserData().equals(descriptor))) {
          continue;
        }

        JobContainer c = new JobContainer(descriptor);
        CustomMenuItem item = new CustomMenuItem();
        item.setContent(c);
        item.setUserData(descriptor);
        jobMenu.getItems().add(item);
      }
    });
  }

  public void refreshMessagesUI(boolean reopen) {
    Platform.runLater(() -> {
      boolean showing = messagesMenu.isShowing();
      List<JobExecutionResult> messages = new ArrayList<>(getResults());
      messagesMenu.setDisable(messages.isEmpty());
      messagesMenu.getItems().removeAll(messagesMenu.getItems());

      if (!messages.isEmpty()) {
        VBox vbox = new VBox();
        vbox.setStyle("-fx-padding: 6 6 6 6;");
        Button dismissBtn = new Button("Dismiss All");
        dismissBtn.setOnAction(new EventHandler<>() {
          @Override
          public void handle(ActionEvent event) {
            Studio.client.getJobsService().dismissAll();
            JobPoller.getInstance().refreshMessagesUI(true);
          }
        });
        vbox.getChildren().add(dismissBtn);

        CustomMenuItem btnItem = new CustomMenuItem();
        btnItem.setContent(vbox);
        messagesMenu.getItems().add(btnItem);


        for (JobExecutionResult message : messages) {
          MessageContainer c = new MessageContainer(message);
          CustomMenuItem item = new CustomMenuItem();
          item.setContent(c);
          item.setUserData(message);
          messagesMenu.getItems().add(item);
        }

        if (reopen || showing) {
          messagesMenu.show();
        }
      }
    });
  }

  private List<JobDescriptor> getActiveJobs() {
    List<JobDescriptor> jobs = new ArrayList<>();
    jobs.addAll(client.getJobsService().getJobs());
    jobs.addAll(clientJobs);
    return jobs;
  }

  private List<JobExecutionResult> getResults() {
    List<JobExecutionResult> results = new ArrayList<>();
    results.addAll(client.getJobsService().getResults());
    return results;
  }

  public void queueJob(JobDescriptor jobDescriptor) {
    clientJobs.add(jobDescriptor);

    new Thread(() -> {
      jobDescriptor.execute(client);
      clientJobs.remove(jobDescriptor);
    }).start();

    JobPoller.getInstance().setPolling();
  }
}
