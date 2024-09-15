package de.mephisto.vpin.ui.jobs;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.JobFinishedEvent;
import de.mephisto.vpin.ui.events.StudioEventListener;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class JobPoller implements StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(JobPoller.class);

  private static JobPoller instance;

  private final MenuButton jobMenu;
  private final ProgressIndicator jobProgress;

  private final List<JobDescriptor> clientJobs = Collections.synchronizedList(new ArrayList<>());
  private final Service service;

  private AtomicBoolean polling = new AtomicBoolean(false);

  public static void destroy() {
    if (instance != null) {
      instance.service.cancel();
      instance = null;
    }
  }

  public static void create(MenuButton jobMenu, ProgressIndicator jobProgress) {
    if (instance == null) {
      instance = new JobPoller(jobMenu, jobProgress);
      EventManager.getInstance().addListener(instance);
    }
  }

  private JobPoller(MenuButton jobMenu, ProgressIndicator jobProgress) {
    this.jobMenu = jobMenu;
    this.jobProgress = jobProgress;

    service = new Service() {
      @Override
      protected Task createTask() {
        return new Task() {
          @Override
          protected Object call() throws Exception {
            Thread.currentThread().setName("Job Polling Thread");
            LOG.info("Started JobPoller service.");
            boolean poll = true;

            while (poll) {
              Thread.sleep(1000);
              List<JobDescriptor> allJobs = getAllJobs();
              List<JobDescriptor> activeJobs = allJobs.stream().filter(j -> !j.isFinished()).collect(Collectors.toList());
              LOG.info("JobPoller is waiting for " + activeJobs.size() + " running jobs.");
              refreshJobsUI();
              poll = !activeJobs.isEmpty();
            }
            LOG.info("JobPoller finished all jobs");
            refreshJobsUI();
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

  public boolean isPolling() {
    return polling.get();
  }

  public void setPolling() {
    jobProgress.setProgress(-1);
    jobProgress.setVisible(true);
    jobProgress.setDisable(false);

    if (!service.isRunning()) {
      service.restart();
    }
    polling.set(true);
  }

  public void refreshJobsUI() {
    List<JobDescriptor> allJobs = getAllJobs();
    List<JobDescriptor> activeJobList = allJobs.stream().filter(j -> !j.isFinished()).collect(Collectors.toList());
    polling.set(!activeJobList.isEmpty());
    jobMenu.setDisable(allJobs.isEmpty());

    Platform.runLater(() -> {
      jobProgress.setProgress(activeJobList.isEmpty() ? 0 : -1);
      jobProgress.setVisible(!activeJobList.isEmpty());
      jobProgress.setDisable(activeJobList.isEmpty());

      if (activeJobList.size() == 1) {
        jobMenu.setText(activeJobList.size() + " active job");
      }
      else if (activeJobList.isEmpty()) {
        jobMenu.setText("No active jobs");
      }
      else {
        jobMenu.setText(activeJobList.size() + " active jobs");
      }

      //remove dismissed jobs
      List<MenuItem> items = new ArrayList<>(jobMenu.getItems());
      for (MenuItem item : items) {
        JobDescriptor descriptor = ((JobsContainerController) item.getUserData()).getDescriptor();
        if (!allJobs.contains(descriptor)) {
          jobMenu.getItems().remove(item);
          jobMenu.requestLayout();
        }
      }

      //update or add jobs
      for (JobDescriptor descriptor : allJobs) {
        Optional<MenuItem> menuItem = items.stream().filter(c -> ((JobsContainerController) c.getUserData()).getDescriptor().equals(descriptor)).findFirst();
        if (menuItem.isPresent()) {
          JobsContainerController controller = (JobsContainerController) menuItem.get().getUserData();
          controller.setData(this, descriptor);
          continue;
        }

        try {
          FXMLLoader loader = new FXMLLoader(JobsContainerController.class.getResource("jobs-container.fxml"));
          BorderPane root = loader.load();
          root.getStyleClass().add("dropin-menu-item");
          JobsContainerController containerController = loader.getController();
          containerController.setData(this, descriptor);
          CustomMenuItem item = new CustomMenuItem();
          item.setUserData(containerController);
          item.setContent(root);
          jobMenu.getItems().add(item);
        }
        catch (IOException e) {
          LOG.error("Failed to load job container: " + e.getMessage(), e);
        }
      }
    });
  }

  private List<JobDescriptor> getAllJobs() {
    List<JobDescriptor> jobs = new ArrayList<>();
    jobs.addAll(client.getJobsService().getJobs());
    jobs.addAll(clientJobs);
    return jobs;
  }

  public void queueJob(JobDescriptor jobDescriptor) {
    clientJobs.add(jobDescriptor);

    new Thread(() -> {
      jobDescriptor.execute(client);
    }).start();

    JobPoller.getInstance().setPolling();
  }

  @Override
  public void jobFinished(@NotNull JobFinishedEvent event) {
    refreshJobsUI();
  }
}
