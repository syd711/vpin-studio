package de.mephisto.vpin.server.components;

import de.mephisto.vpin.connectors.github.ReleaseArtifactProgressListener.Phase;
import de.mephisto.vpin.restclient.components.ComponentProgressRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import org.junit.jupiter.api.Test;

import static de.mephisto.vpin.restclient.components.ComponentProgressRepresentation.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class ComponentProgressTrackerTest {

  @Test
  void inactiveWithoutOperation() {
    ComponentProgressTracker tracker = new ComponentProgressTracker();
    assertThat(tracker.getProgress(ComponentType.vpinball).isActive()).isFalse();
  }

  @Test
  void weightsPhases() {
    ComponentProgressTracker tracker = new ComponentProgressTracker();
    ComponentProgressTracker.Operation op = tracker.start(ComponentType.vpinball, PHASE_DOWNLOAD, PHASE_EXTRACT);

    op.onProgress(Phase.DOWNLOAD, 50, 100);
    ComponentProgressRepresentation progress = tracker.getProgress(ComponentType.vpinball);
    assertThat(progress.isActive()).isTrue();
    assertThat(progress.getPhase()).isEqualTo(PHASE_DOWNLOAD);
    assertThat(progress.getProgress()).isCloseTo(3d / 9, within(0.001));

    op.onProgress(Phase.EXTRACT, 50, 100);
    assertThat(tracker.getProgress(ComponentType.vpinball).getProgress()).isCloseTo(7.5 / 9, within(0.001));

    // late download events do not move the progress backwards
    op.onProgress(Phase.DOWNLOAD, 10, 100);
    assertThat(tracker.getProgress(ComponentType.vpinball).getPhase()).isEqualTo(PHASE_EXTRACT);
    assertThat(tracker.getProgress(ComponentType.vpinball).getDone()).isEqualTo(50);

    tracker.finish(ComponentType.vpinball, op);
    assertThat(tracker.getProgress(ComponentType.vpinball).isActive()).isFalse();
  }

  @Test
  void cachedDownloadCountsAsFinished() {
    ComponentProgressTracker tracker = new ComponentProgressTracker();
    ComponentProgressTracker.Operation op = tracker.start(ComponentType.dof, PHASE_FETCH, PHASE_DOWNLOAD, PHASE_COMPARE);
    op.phase(PHASE_FETCH);
    assertThat(tracker.getProgress(ComponentType.dof).getProgress()).isZero();

    op.onProgress(Phase.COMPARE, 0, 100);
    assertThat(tracker.getProgress(ComponentType.dof).getProgress()).isCloseTo(7d / 10, within(0.001));
  }
}
