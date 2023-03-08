package de.mephisto.vpin.tablemanager;

import de.mephisto.vpin.restclient.JobDescriptor;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableManagerJobPoller {
  private final static Logger LOG = LoggerFactory.getLogger(TableManagerJobPoller.class);

  private static TableManagerJobPoller instance;
  private final Service service;
  private boolean polling = false;

  private final List<JobDescriptor> activeJobs = Collections.synchronizedList(new ArrayList<>());
  private List<JobListener> listeners = new ArrayList<>();

  public void addJobListener(JobListener listener) {
    this.listeners.add(listener);
  }

  public static TableManagerJobPoller getInstance() {
    if (instance == null) {
      instance = new TableManagerJobPoller();
    }
    return instance;
  }

  private TableManagerJobPoller() {
    service = new Service() {
      @Override
      protected Task createTask() {
        return new Task() {
          @Override
          protected Object call() throws Exception {
            Thread.currentThread().setName("Job Polling Thread");
            LOG.info("Started JobPoller service.");
            polling = true;

            //give the init some time
            refreshJobList();
            if (activeJobs.isEmpty()) {
              Thread.sleep(1000);
            }

            while (polling) {
              refreshJobList();
              Thread.sleep(2000);
              polling = !activeJobs.isEmpty();
              LOG.info("JobPoller is waiting for " + activeJobs.size() + " running jobs.");
            }
            LOG.info("JobPoller finished all jobs");
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

  public boolean isPolling() {
    return polling;
  }

  public void setPolling() {
    if (!service.isRunning()) {
      polling = true;
      service.restart();
    }
  }

  private void refreshJobList() {
    List<JobDescriptor> jobs = Menu.client.getJobs();
    for (JobDescriptor activeJob : activeJobs) {
      notifyJobUpdate(activeJob);
    }


    for (JobDescriptor activeJob : activeJobs) {
      if (!jobs.contains(activeJob)) {
        LOG.info(activeJob + " finished.");
        notifyJobFinished(activeJob);
      }
    }
    activeJobs.clear();
    activeJobs.addAll(jobs);
  }

  private void notifyJobFinished(JobDescriptor descriptor) {
    new Thread(() -> {
      for (JobListener listener : this.listeners) {
        listener.finished(descriptor);
      }
    }).start();
  }

  private void notifyJobUpdate(JobDescriptor descriptor) {
    new Thread(() -> {
      for (JobListener listener : this.listeners) {
        listener.updated(descriptor);
      }
    }).start();
  }
}
