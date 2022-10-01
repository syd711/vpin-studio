package de.mephisto.vpin.server.dof;

public class DOFCommandResult {
  private String out;
  private String error;

  public DOFCommandResult(String out, String error) {
    this.out = out;
    this.error = error;
  }

  public String getOut() {
    return out;
  }

  public String getOutput() {
    return out;
  }

  public boolean isSuccessful() {
    return out != null && !out.contains("No such unit");
  }
}
