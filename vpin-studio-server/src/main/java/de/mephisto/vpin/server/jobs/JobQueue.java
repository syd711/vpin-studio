package de.mephisto.vpin.server.jobs;

import de.mephisto.vpin.restclient.JobDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobQueue {
  private final static Logger LOG = LoggerFactory.getLogger(JobQueue.class);
  private final ExecutorService executor;

  private Queue<JobDescriptor> queue = new ConcurrentLinkedQueue();
  private List<JobDescriptor> statusQueue = new ArrayList<>();

  private static JobQueue instance = new JobQueue();

  public static JobQueue getInstance() {
    return instance;
  }

  private JobQueue() {
    executor = Executors.newSingleThreadExecutor();
  }

  public void offer(JobDescriptor descriptor) {
    queue.offer(descriptor);
    statusQueue.add(descriptor);
    LOG.info("Queue size: " + queue.size());
    pollQueue();
  }

  private void pollQueue() {
    if (!getInstance().isEmpty()) {
      JobDescriptor descriptor = queue.poll();
      Callable<Boolean> exec = () -> {
        boolean execute = descriptor.getJob().execute();
        statusQueue.remove(descriptor);
        LOG.info("Finished " + descriptor + ", queue size is " + queue.size());
        pollQueue();
        return execute;
      };
      executor.submit(exec);
    }
  }

  public boolean isEmpty() {
    return this.queue.isEmpty();
  }

  public List<JobDescriptor> getElements() {
    return new ArrayList<>(statusQueue);
  }
}
