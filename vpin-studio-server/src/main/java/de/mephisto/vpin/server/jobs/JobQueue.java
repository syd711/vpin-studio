package de.mephisto.vpin.server.jobs;

import de.mephisto.vpin.restclient.JobExecutionResult;
import de.mephisto.vpin.restclient.descriptors.JobDescriptor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class JobQueue implements InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(JobQueue.class);

  private ExecutorService executor;

  private final Queue<JobDescriptor> queue = new ConcurrentLinkedQueue();
  private final List<JobDescriptor> statusQueue = new ArrayList<>();

  @Autowired
  private JobService jobService;

  private JobQueue() {

  }

  public void offer(JobDescriptor descriptor) {
    queue.offer(descriptor);
    statusQueue.add(descriptor);
    LOG.info("Queue size: " + queue.size());
    pollQueue();
  }

  private void pollQueue() {
    if (!isEmpty()) {
      JobDescriptor descriptor = queue.poll();
      Callable<JobExecutionResult> exec = () -> {
        JobExecutionResult result = descriptor.getJob().execute();
        statusQueue.remove(descriptor);
        LOG.info("Finished " + descriptor + ", queue size is " + queue.size());
        pollQueue();
        if(!StringUtils.isEmpty(result.getError())) {
          jobService.addResult(result);
        }
        return result;
      };
      executor.submit(exec);
    }
  }

  public boolean isEmpty() {
    return this.queue.isEmpty();
  }

  public List<JobDescriptor> status() {
    List<JobDescriptor> elements = new ArrayList<>(statusQueue);
    for (JobDescriptor descriptor : elements) {
      descriptor.setStatus(descriptor.getJob().getStatus());
      descriptor.setProgress(descriptor.getJob().getProgress());
    }
    return elements;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    executor = Executors.newSingleThreadExecutor();
  }
}
