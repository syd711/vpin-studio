package de.mephisto.vpin.ui.jobs;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.events.JobFinishedEvent;
import de.mephisto.vpin.ui.events.StudioEventListener;
import edu.umd.cs.findbugs.annotations.NonNull;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static de.mephisto.vpin.ui.Studio.client;

public class JobPoller implements StudioEventListener {
  private final static Logger LOG = LoggerFactory.getLogger(JobPoller.class);
  private final static String ACTIVE_STYLE = "-fx-border-style: solid;-fx-border-color: #6666FFAA;-fx-border-width: 1;";

  private static JobPoller instance;

  private final MenuButton jobMenu;
  private final ProgressIndicator jobProgress;

  private final List<JobDescriptor> clientJobs = Collections.synchronizedList(new ArrayList<>());
  private final Service service;

  private final AtomicBoolean polling = new AtomicBoolean(false);
  private JobsMenuHeaderController headerController;

  private final List<JobUpdatesListener> listeners = new ArrayList<>();

  public void addListener(JobUpdatesListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(JobUpdatesListener listener) {
    this.listeners.remove(listener);
  }

  private final Queue<JobDescriptor> finishedJobs = new ConcurrentLinkedQueue<>();

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

    try {
      FXMLLoader loader = new FXMLLoader(JobsMenuHeaderController.class.getResource("jobs-header.fxml"));
      BorderPane root = loader.load();
      root.getStyleClass().add("dropin-menu-item");
      headerController = loader.getController();
      headerController.setData(this);

      CustomMenuItem item = new CustomMenuItem();
      item.setContent(root);
      jobMenu.getItems().add(item);
    }
    catch (IOException e) {
      LOG.error("Failed to load job container: " + e.getMessage(), e);
    }


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
              Thread.sleep(600);
              List<JobDescriptor> allJobs = getAllJobs();
              List<JobDescriptor> activeJobs = allJobs.stream().filter(j -> (!j.isFinished() && !j.isCancelled())).collect(Collectors.toList());
              LOG.info("JobPoller is waiting for " + activeJobs.size() + " running jobs.");
              refreshJobsUI();
              poll = !activeJobs.isEmpty();
              notifyListeners(activeJobs, allJobs);
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

  private void notifyListeners(List<JobDescriptor> activeJobs, List<JobDescriptor> allJobs) {
    for (JobUpdatesListener listener : listeners) {
      listener.jobsRefreshed(activeJobs);
    }

    for (JobDescriptor job : allJobs) {
      if ((job.isCancelled() || job.isFinished()) && !finishedJobs.contains(job)) {
        finishedJobs.add(job);
        EventManager.getInstance().notifyJobFinished(job);
      }
    }
  }

  public static JobPoller getInstance() {
    return instance;
  }

  public boolean isPolling() {
    return polling.get();
  }

  public void setPolling() {
    if (!polling.get()) {
      jobProgress.setProgress(-1);
      jobProgress.setVisible(true);
      jobProgress.setDisable(false);
      jobMenu.setStyle(ACTIVE_STYLE);

      if (!service.isRunning()) {
        service.restart();
      }
      polling.set(true);
    }
  }

  public void refreshJobsUI() {
    List<JobDescriptor> allJobs = getAllJobs();
    List<JobDescriptor> activeJobList = allJobs.stream().filter(j -> (!j.isFinished() && !j.isCancelled())).collect(Collectors.toList());
    polling.set(!activeJobList.isEmpty());
    jobMenu.setDisable(allJobs.isEmpty());
    headerController.setVisible(!allJobs.isEmpty());

    Platform.runLater(() -> {
      jobProgress.setProgress(activeJobList.isEmpty() ? 0 : -1);
      jobProgress.setVisible(!activeJobList.isEmpty());
      jobProgress.setDisable(activeJobList.isEmpty());
      if (activeJobList.isEmpty()) {
        jobMenu.setStyle(null);
      }


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
        if (item.getUserData() == null) {
          continue;
        }

        JobDescriptor descriptor = ((JobsContainerController) item.getUserData()).getDescriptor();
        if (!allJobs.contains(descriptor)) {
          jobMenu.getItems().remove(item);
          jobMenu.requestLayout();
        }
      }

      //update or add jobs
      for (JobDescriptor descriptor : allJobs) {
        Optional<MenuItem> menuItem = items.stream().filter(c -> c.getUserData() != null && ((JobsContainerController) c.getUserData()).getDescriptor().equals(descriptor)).findFirst();
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

  public void dismissAll() {
    client.getJobsService().dismissAll();
    for (JobDescriptor clientJob : new ArrayList<>(clientJobs)) {
      if (clientJob.isFinished() || clientJob.isCancelled()) {
        clientJobs.remove(clientJob);
        finishedJobs.remove(clientJob);
      }
    }
  }


  public void dismiss(JobDescriptor job) {
    clientJobs.remove(job);
  }

  public void queueJob(JobDescriptor jobDescriptor) {
    clientJobs.add(jobDescriptor);

    new Thread(() -> {
      jobDescriptor.execute(client);
    }).start();

    JobPoller.getInstance().setPolling();
  }

  @Override
  public void jobFinished(@NonNull JobFinishedEvent event) {
    refreshJobsUI();
  }
}
