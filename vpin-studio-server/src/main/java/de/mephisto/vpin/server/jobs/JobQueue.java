package de.mephisto.vpin.server.jobs;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class JobQueue implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(JobQueue.class);

  private ExecutorService executor;

  private final Queue<JobDescriptor> queue = new ConcurrentLinkedQueue<>();

  private JobQueue() {

  }

  private void pollQueue() {
    if (!isEmpty()) {
      JobDescriptor descriptor = queue.poll();
      Callable<JobDescriptor> exec = () -> {
        Thread.currentThread().setName(descriptor.toString());
        Job job = descriptor.getJob();
        if (job == null) {
          LOG.error("No job found for " + descriptor);
        }
        else {
          descriptor.getJob().execute(descriptor);
          descriptor.setProgress(1);
          LOG.info("Finished " + descriptor + ", queue size is " + queue.size());
        }
        pollQueue();
        return descriptor;
      };
      executor.submit(exec);
    }
  }

  public void submit(JobDescriptor descriptor) {
    queue.offer(descriptor);
    pollQueue();
  }

  public int size() {
    return queue.size();
  }

  public void cancel(JobDescriptor descriptor) {
    this.queue.remove(descriptor);
    descriptor.setCancelled(true);
    descriptor.getJob().cancel(descriptor);
  }

  public boolean isEmpty() {
    return this.queue.isEmpty();
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    executor = Executors.newSingleThreadExecutor();
    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }
}
