package de.mephisto.vpin.restclient.util;

/**
 * Generic Object to return a status and associated message
 * ex when saving an object
 */
public class ReturnMessage {
  
  private ReturnCodes status;

  private String message;

  public ReturnMessage() {
  }

  public static ReturnMessage OK() {
    ReturnMessage msg = new ReturnMessage();
    msg.setStatus(ReturnCodes.OK);
    return msg;
  }

  public static ReturnMessage error(Exception e) {
    return error(e.getMessage());
  }

  public static ReturnMessage error(String error) {
    ReturnMessage msg = new ReturnMessage();
    msg.setStatus(ReturnCodes.ERROR);
    msg.setMessage(error);
    return msg;
  }

  public ReturnCodes getStatus() {
    return status;
  }
  public void setStatus(ReturnCodes status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }

  public boolean isOk() {
    return status==null || status.isOk();
  }

  @Override
  public String toString() {
    return message;
  }
}
