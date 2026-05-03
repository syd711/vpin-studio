package de.mephisto.vpin.server.jobs;

import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.jobs.Job;
import de.mephisto.vpin.restclient.jobs.JobType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobServiceTest {

  @Mock
  private JobQueue jobQueue;

  @InjectMocks
  private JobService jobService;

  private JobDescriptor finishedDescriptor(String uuid) {
    Job job = mock(Job.class);
    when(job.isCancelable()).thenReturn(false);
    JobDescriptor d = new JobDescriptor(JobType.ALTCOLOR_INSTALL);
    d.setJob(job);
    d.setUuid(uuid);
    d.setProgress(1.0); // isFinished() returns true when progress >= 1
    return d;
  }

  private JobDescriptor activeDescriptor(String uuid) {
    Job job = mock(Job.class);
    when(job.isCancelable()).thenReturn(true);
    JobDescriptor d = new JobDescriptor(JobType.ALTCOLOR_INSTALL);
    d.setJob(job);
    d.setUuid(uuid);
    return d;
  }

  // ---- getJobs ----

  @Test
  void getJobs_returnsJobsFromQueue() {
    JobDescriptor d = activeDescriptor("j1");
    when(jobQueue.getJobs()).thenReturn(List.of(d));

    List<JobDescriptor> result = jobService.getJobs();

    assertEquals(1, result.size());
    assertSame(d, result.get(0));
  }

  @Test
  void getJobs_propagatesCancelableFlag() {
    Job job = mock(Job.class);
    when(job.isCancelable()).thenReturn(true);
    JobDescriptor d = new JobDescriptor(JobType.ALTCOLOR_INSTALL);
    d.setJob(job);
    d.setUuid("cx");
    when(jobQueue.getJobs()).thenReturn(List.of(d));

    jobService.getJobs();

    assertTrue(d.isCancelable());
  }

  // ---- getJob ----

  @Test
  void getJob_returnsDescriptor_whenFound() {
    JobDescriptor d = activeDescriptor("find-me");
    when(jobQueue.getJobs()).thenReturn(List.of(d));

    JobDescriptor result = jobService.getJob("find-me");

    assertNotNull(result);
    assertEquals("find-me", result.getUuid());
  }

  @Test
  void getJob_returnsNull_whenNotFound() {
    when(jobQueue.getJobs()).thenReturn(Collections.emptyList());

    JobDescriptor result = jobService.getJob("missing");

    assertNull(result);
  }

  // ---- offer ----

  @Test
  void offer_submitsToQueue() {
    JobDescriptor d = activeDescriptor("offer-1");

    jobService.offer(d);

    verify(jobQueue).submit(d);
  }

  // ---- dismiss ----

  @Test
  void dismiss_cancelsAndDismissesFinishedJob() {
    JobDescriptor d = finishedDescriptor("done-1");
    when(jobQueue.getJobs()).thenReturn(List.of(d));

    jobService.dismiss("done-1");

    verify(jobQueue).cancel(d);
    verify(jobQueue).dismiss(d);
  }

  @Test
  void dismiss_doesNothing_whenJobNotFound() {
    when(jobQueue.getJobs()).thenReturn(Collections.emptyList());

    jobService.dismiss("ghost");

    verify(jobQueue, never()).cancel(any());
    verify(jobQueue, never()).dismiss(any());
  }

  // ---- dismissAll ----

  @Test
  void dismissAll_dismissesOnlyFinishedOrCancelledJobs() {
    JobDescriptor active = activeDescriptor("active");
    JobDescriptor finished = finishedDescriptor("finished");
    JobDescriptor cancelled = activeDescriptor("cancelled");
    cancelled.setCancelled(true);

    when(jobQueue.getJobs()).thenReturn(List.of(active, finished, cancelled));

    jobService.dismissAll();

    verify(jobQueue, never()).dismiss(active);
    verify(jobQueue).dismiss(finished);
    verify(jobQueue).dismiss(cancelled);
  }

  // ---- cancel(uuid) ----

  @Test
  void cancelByUuid_cancelsMatchingJob() {
    JobDescriptor d = activeDescriptor("to-cancel");
    when(jobQueue.getJobs()).thenReturn(List.of(d));

    jobService.cancel("to-cancel");

    verify(jobQueue).cancel(d);
  }

  @Test
  void cancelByUuid_doesNothing_whenNotFound() {
    when(jobQueue.getJobs()).thenReturn(Collections.emptyList());

    jobService.cancel("ghost");

    verify(jobQueue, never()).cancel(any());
  }

  // ---- cancelAll ----

  @Test
  void cancelAll_cancelsEveryJob() {
    JobDescriptor d1 = activeDescriptor("c1");
    JobDescriptor d2 = activeDescriptor("c2");
    when(jobQueue.getJobs()).thenReturn(List.of(d1, d2));

    jobService.cancelAll();

    verify(jobQueue).cancel(d1);
    verify(jobQueue).cancel(d2);
  }

  // ---- cancel(JobType) ----

  @Test
  void cancelByJobType_cancelsOnlyMatchingNonFinished() {
    JobType type = JobType.ALTCOLOR_INSTALL;

    Job activeJob = mock(Job.class);
    when(activeJob.isCancelable()).thenReturn(true);
    JobDescriptor active = new JobDescriptor(JobType.ALTCOLOR_INSTALL);
    active.setJob(activeJob);
    active.setUuid("type-match");
    active.setJobType(type);

    Job otherJob = mock(Job.class);
    when(otherJob.isCancelable()).thenReturn(true);
    JobDescriptor other = new JobDescriptor(JobType.ALTCOLOR_INSTALL);
    other.setJob(otherJob);
    other.setUuid("other-type");
    other.setJobType(type);

    when(jobQueue.getJobs()).thenReturn(List.of(active, other));

    jobService.cancel(type);

    verify(jobQueue).cancel(active);
    verify(jobQueue, never()).cancel(other);
  }

  @Test
  void cancelByJobType_skipsAlreadyCancelledOrFinished() {
    Job job = mock(Job.class);
    when(job.isCancelable()).thenReturn(true);
    JobDescriptor d = new JobDescriptor(JobType.ALTCOLOR_INSTALL);
    d.setJob(job);
    d.setUuid("already");
    d.setJobType(JobType.ALTCOLOR_INSTALL);
    d.setCancelled(true);

    when(jobQueue.getJobs()).thenReturn(List.of(d));

    jobService.cancel(JobType.ALTCOLOR_INSTALL);

    verify(jobQueue, never()).cancel(d);
  }
}
