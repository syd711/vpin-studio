package de.mephisto.vpin.ui.jobs;

import de.mephisto.vpin.restclient.JobDescriptor;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class JobPoller {

  private static JobPoller instance;
  private MenuButton jobMenu;

  private List<JobDescriptor> activeJobs = new ArrayList<>();
  private Service service;

  public JobPoller(MenuButton jobMenu) {
    this.jobMenu = jobMenu;
    this.jobMenu.setStyle("-fx-background-color: #111111;");
    instance = this;

    service = new Service() {
      @Override
      protected Task createTask() {
        return new Task() {
          @Override
          protected Object call() throws Exception {
            boolean poll = true;
            while (poll) {
              List<JobDescriptor> jobs = new ArrayList<>(client.getJobs());
              refreshUI(jobs);
              Thread.sleep(2000);
              poll = !jobs.isEmpty();
            }
            refreshUI(Collections.emptyList());
            return null;
          }
        };
      }
    };

    service.onSucceededProperty().addListener((observable, oldValue, newValue) -> service.reset());
  }

  public static JobPoller getInstance() {
    return instance;
  }

  public void setPolling() {
    jobMenu.setVisible(true);
    if (!service.isRunning()) {
      service.restart();
    }
  }

  private void refreshUI(List<JobDescriptor> updatedJobList) {
    Platform.runLater(() -> {
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
        if(items.stream().anyMatch(c -> c.getUserData().equals(descriptor))) {
          continue;
        }

        JobContainer c = new JobContainer(descriptor);
        CustomMenuItem item = new CustomMenuItem();
        item.setContent(c);
        item.setUserData(descriptor);
        jobMenu.getItems().add(item);
      }
      jobMenu.setVisible(!updatedJobList.isEmpty());
    });
  }
}
