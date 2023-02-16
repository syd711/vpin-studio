package de.mephisto.vpin.ui.jobs;

import de.mephisto.vpin.restclient.JobDescriptor;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class JobPoller {
  private final static Logger LOG = LoggerFactory.getLogger(JobPoller.class);

  private static JobPoller instance;
  private MenuButton jobMenu;

  private List<JobDescriptor> activeJobs = new ArrayList<>();
  private Service service;

  public static void create(MenuButton jobMenu) {
    if (instance == null) {
      instance = new JobPoller(jobMenu);
    }
  }

  private JobPoller(MenuButton jobMenu) {
    this.jobMenu = jobMenu;
    this.jobMenu.setStyle("-fx-background-color: #111111;");

    service = new Service() {
      @Override
      protected Task createTask() {
        return new Task() {
          @Override
          protected Object call() throws Exception {
            LOG.info("Started JobPoller service.");
            boolean poll = true;

            //give the init some time
            List<JobDescriptor> jobs = new ArrayList<>(client.getJobs());
            if (jobs.isEmpty()) {
              Thread.sleep(1000);
            }

            while (poll) {
              jobs = new ArrayList<>(client.getJobs());
              refreshUI(jobs);
              Thread.sleep(2000);
              poll = !jobs.isEmpty();
              LOG.info("JobPoller is waiting for " + jobs.size() + " running jobs.");
            }
            LOG.info("JobPoller finished all jobs");
            refreshUI(Collections.emptyList());
            return true;
          }
        };
      }
    };

    service.onSucceededProperty().addListener((observable, oldValue, newValue) -> {
      service.reset();
      LOG.info("JobPoller has been resetted after success.");
    });
  }

  public static JobPoller getInstance() {
    return instance;
  }

  public boolean isPolling() {
    return !jobMenu.isDisabled();
  }

  public void setPolling() {
    jobMenu.setDisable(false);
    if (!service.isRunning()) {
      service.restart();
    }
  }

  private void refreshUI(List<JobDescriptor> updatedJobList) {
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
}
