package de.mephisto.vpin.restclient.components;

/**
 * Progress of the running check, simulation or installation of a component.
 */
public class ComponentProgressRepresentation {

  public static final String PHASE_FETCH = "FETCH";
  public static final String PHASE_DOWNLOAD = "DOWNLOAD";
  public static final String PHASE_COMPARE = "COMPARE";
  public static final String PHASE_SIMULATE = "SIMULATE";
  public static final String PHASE_EXTRACT = "EXTRACT";

  private boolean active;
  private String phase;
  /**
   * Overall progress of the action, 0..1
   */
  private double progress;
  /**
   * Bytes processed in the current phase
   */
  private long done;
  /**
   * Total bytes of the current phase, -1 when unknown
   */
  private long total = -1;

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getPhase() {
    return phase;
  }

  public void setPhase(String phase) {
    this.phase = phase;
  }

  public double getProgress() {
    return progress;
  }

  public void setProgress(double progress) {
    this.progress = progress;
  }

  public long getDone() {
    return done;
  }

  public void setDone(long done) {
    this.done = done;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }
}
