package de.mephisto.vpin.server.jobs;

import de.mephisto.vpin.restclient.JobDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobQueue {
  private final static Logger LOG = LoggerFactory.getLogger(JobQueue.class);
  private final ExecutorService executor;

  private Queue<JobDescriptor> queue = new ConcurrentLinkedQueue();

  private static JobQueue instance = new JobQueue();

  private Thread jobThread;

  public static JobQueue getInstance() {
    return instance;
  }

  private JobQueue() {
    executor = Executors.newSingleThreadExecutor();


    jobThread = new Thread(() -> {
      try {
        while (true) {
          if (queue.isEmpty()) {
            synchronized (jobThread) {
              wait();
            }
          }

          if (!queue.isEmpty()) {
            JobDescriptor job = queue.poll();
            LOG.info("Polled " + job);
            job.getJob().execute();
          }
        }
      } catch (InterruptedException e) {
        LOG.error("Error in job thread: " + e.getMessage(), e);
      }
    });
    jobThread.start();
  }

  public void offer(JobDescriptor descriptor) {
    this.queue.offer(descriptor);

    synchronized (jobThread) {
      jobThread.notifyAll();
    }
  }

  public boolean isEmpty() {
    return this.queue.isEmpty();
  }

  public List<JobDescriptor> getElements() {
    return new ArrayList<>(queue);
  }
}
