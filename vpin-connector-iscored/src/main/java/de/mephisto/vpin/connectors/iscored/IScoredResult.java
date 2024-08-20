package de.mephisto.vpin.connectors.iscored;

public class IScoredResult {
  private boolean sent;
  private String message;

  private int returnCode;

  public boolean isSent() {
    return sent;
  }

  public void setSent(boolean sent) {
    this.sent = sent;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public int getReturnCode() {
    return returnCode;
  }

  public void setReturnCode(int returnCode) {
    this.returnCode = returnCode;
  }

  @Override
  public String toString() {
    if (returnCode > 0) {
      return "iScored result: " + message + " (" + returnCode + ")";
    }

    return "iScored result: " + message;
  }
}
