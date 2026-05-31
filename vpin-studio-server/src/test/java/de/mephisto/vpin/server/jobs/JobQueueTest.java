package de.mephisto.vpin.server.jobs;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.jobs.JobType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class JobQueueTest {

  private JobQueue jobQueue;

  @BeforeEach
  void setUp() throws Exception {
    // Use reflection to bypass the private constructor
    java.lang.reflect.Constructor<JobQueue> ctor = JobQueue.class.getDeclaredConstructor();
    ctor.setAccessible(true);
    jobQueue = ctor.newInstance();
    jobQueue.afterPropertiesSet();
  }

  private JobDescriptor makeDescriptor(String uuid) {
    Job job = mock(Job.class);
    when(job.isCancelable()).thenReturn(true);
    JobDescriptor d = new JobDescriptor(JobType.ALTCOLOR_INSTALL);
    d.setJob(job);
    d.setUuid(uuid);
    return d;
  }

  // ---- isEmpty / size ----

  @Test
  void isEmpty_returnsTrue_initially() {
    assertTrue(jobQueue.isEmpty());
  }

  @Test
  void size_isZero_initially() {
    assertEquals(0, jobQueue.size());
  }

  @Test
  void getJobs_returnsEmptyList_initially() {
    assertTrue(jobQueue.getJobs().isEmpty());
  }

  // ---- submit ----

  @Test
  void submit_addsDescriptorToQueue() {
    JobDescriptor d = makeDescriptor("uuid-1");

    jobQueue.submit(d);

    assertEquals(1, jobQueue.size());
    assertFalse(jobQueue.isEmpty());
  }

  @Test
  void getJobs_returnsSubmittedDescriptor() {
    JobDescriptor d = makeDescriptor("uuid-2");

    jobQueue.submit(d);

    assertTrue(jobQueue.getJobs().contains(d));
  }

  // ---- dismiss ----

  @Test
  void dismiss_removesDescriptorFromQueue() {
    JobDescriptor d = makeDescriptor("uuid-3");
    jobQueue.submit(d);

    jobQueue.dismiss(d);

    assertFalse(jobQueue.getJobs().contains(d));
    assertEquals(0, jobQueue.size());
  }

  @Test
  void dismiss_setsProgressToOne() {
    JobDescriptor d = makeDescriptor("uuid-4");
    jobQueue.submit(d);

    jobQueue.dismiss(d);

    assertEquals(1, d.getProgress(), 0.001);
  }

  // ---- cancel ----

  @Test
  void cancel_marksCancelledAndCallsJobCancel() {
    Job job = mock(Job.class);
    when(job.isCancelable()).thenReturn(true);
    JobDescriptor d = new JobDescriptor(JobType.ALTCOLOR_INSTALL);
    d.setJob(job);
    d.setUuid("uuid-5");
    jobQueue.submit(d);

    jobQueue.cancel(d);

    assertTrue(d.isCancelled());
    verify(job).cancel(d);
  }

  @Test
  void cancel_setsProgressToOne() {
    JobDescriptor d = makeDescriptor("uuid-6");
    jobQueue.submit(d);

    jobQueue.cancel(d);

    assertEquals(1, d.getProgress(), 0.001);
  }

  // ---- multiple items ----

  @Test
  void submit_multipleDescriptors_allAppearInQueue() {
    JobDescriptor d1 = makeDescriptor("a");
    JobDescriptor d2 = makeDescriptor("b");

    jobQueue.submit(d1);
    jobQueue.submit(d2);

    assertEquals(2, jobQueue.size());
    assertTrue(jobQueue.getJobs().containsAll(java.util.List.of(d1, d2)));
  }

  @Test
  void getJobs_returnsSnapshot_notLiveQueue() {
    JobDescriptor d = makeDescriptor("snap");
    jobQueue.submit(d);

    java.util.List<JobDescriptor> snapshot = jobQueue.getJobs();
    jobQueue.dismiss(d);

    // snapshot taken before dismiss should still contain d
    assertTrue(snapshot.contains(d));
  }
}
