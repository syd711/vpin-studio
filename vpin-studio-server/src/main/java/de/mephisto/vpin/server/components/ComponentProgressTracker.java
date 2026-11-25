package de.mephisto.vpin.server.components;

import de.mephisto.vpin.connectors.github.ReleaseArtifactProgressListener;
import de.mephisto.vpin.restclient.components.ComponentProgressRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static de.mephisto.vpin.restclient.components.ComponentProgressRepresentation.*;

/**
 * Tracks the progress of the running component actions, so that clients can poll it during the blocking REST call.
 * An action consists of weighted phases, a skipped phase (e.g. the download of a cached archive) counts as finished.
 */
public class ComponentProgressTracker {

  private final Map<ComponentType, Operation> operations = new ConcurrentHashMap<>();

  @NonNull
  public Operation start(@NonNull ComponentType type, @NonNull String... phases) {
    Operation operation = new Operation(List.of(phases));
    operations.put(type, operation);
    return operation;
  }

  public void finish(@NonNull ComponentType type, @NonNull Operation operation) {
    operations.remove(type, operation);
  }

  @NonNull
  public ComponentProgressRepresentation getProgress(@NonNull ComponentType type) {
    Operation operation = operations.get(type);
    if (operation == null) {
      return new ComponentProgressRepresentation();
    }
    return operation.toRepresentation();
  }

  static int weight(String phase) {
    return switch (phase) {
      case PHASE_FETCH -> 1;
      case PHASE_DOWNLOAD -> 6;
      default -> 3;
    };
  }

  public static class Operation implements ReleaseArtifactProgressListener {
    private final List<String> phases;
    private int phaseIndex = -1;
    private long done = 0;
    private long total = -1;

    Operation(List<String> phases) {
      this.phases = phases;
    }

    /**
     * Switches to the given phase, phases never go backwards.
     */
    public synchronized void phase(@NonNull String phase) {
      int index = phases.indexOf(phase);
      if (index > phaseIndex) {
        phaseIndex = index;
        done = 0;
        total = -1;
      }
    }

    @Override
    public synchronized void onProgress(Phase phase, long done, long total) {
      phase(phase.name());
      if (phases.indexOf(phase.name()) == phaseIndex) {
        this.done = done;
        this.total = total;
      }
    }

    synchronized ComponentProgressRepresentation toRepresentation() {
      ComponentProgressRepresentation representation = new ComponentProgressRepresentation();
      representation.setActive(true);
      representation.setProgress(getProgress());
      representation.setDone(done);
      representation.setTotal(total);
      if (phaseIndex >= 0) {
        representation.setPhase(phases.get(phaseIndex));
      }
      return representation;
    }

    synchronized double getProgress() {
      int sum = phases.stream().mapToInt(ComponentProgressTracker::weight).sum();
      if (sum == 0 || phaseIndex < 0) {
        return 0;
      }

      double finished = 0;
      for (int i = 0; i < phaseIndex; i++) {
        finished += weight(phases.get(i));
      }
      double fraction = total > 0 ? Math.min(1d, (double) done / total) : 0;
      finished += weight(phases.get(phaseIndex)) * fraction;
      return finished / sum;
    }
  }
}
