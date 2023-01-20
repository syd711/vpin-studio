package de.mephisto.vpin.ui.jobs;

import de.mephisto.vpin.restclient.JobDescriptor;
import javafx.application.Platform;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class JobPoller {

  private static JobPoller instance;
  private MenuButton jobMenu;

  private List<CustomMenuItem> activeContainers = new ArrayList<>();

  public JobPoller(MenuButton jobMenu) {
    this.jobMenu = jobMenu;
    instance = this;
  }

  public static JobPoller getInstance() {
    return instance;
  }

  public void setPolling() {
    jobMenu.setVisible(true);

    new Thread(() -> {
      try {
        List<JobDescriptor> jobs = client.getJobs();
        boolean poll = !jobs.isEmpty();
        while(poll) {
          List<CustomMenuItem> updated = new ArrayList<>();
          for (JobDescriptor job : jobs) {
            JobContainer c = new JobContainer(job);
            CustomMenuItem item = new CustomMenuItem();
            item.setContent(c);
            updated.add(item);
          }
          Thread.sleep(2000);
          poll = !jobs.isEmpty();
          refreshUI(updated);
        }
        refreshUI(Collections.emptyList());
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }).start();
  }

  private void refreshUI(List<CustomMenuItem> updated) {
    Platform.runLater(() -> {
      jobMenu.getItems().removeAll(activeContainers);
      jobMenu.getItems().addAll(updated);
      jobMenu.setVisible(!updated.isEmpty());
    });
  }
}
