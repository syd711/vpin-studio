package de.mephisto.vpin.ui.jobs;

import de.mephisto.vpin.restclient.jobs.JobExecutionResult;
import de.mephisto.vpin.restclient.tables.descriptors.JobDescriptor;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.messaging.MessageContainer;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
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

  public static void create(MenuButton jobMenu, MenuButton messagesBtn) {
    if (instance == null) {
      instance = new JobPoller(jobMenu, messagesBtn);
    }
  }

  //TODO throw UI out!
  private JobPoller(MenuButton jobMenu, MenuButton messagesMenu) {
    this.jobMenu = jobMenu;
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
    if (!service.isRunning()) {
      service.restart();
    }
  }

  private void refreshJobsUI(List<JobDescriptor> updatedJobList) {
    Platform.runLater(() -> {
      jobMenu.setDisable(updatedJobList.isEmpty());
      if (jobMenu.isDisabled()) {
        jobMenu.setText("No active jobs");
      }
      else {
        jobMenu.setText(updatedJobList.size() + " active job(s)");
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
