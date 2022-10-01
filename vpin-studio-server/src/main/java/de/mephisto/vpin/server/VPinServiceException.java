package de.mephisto.vpin.server;

public class VPinServiceException extends Exception {
  public VPinServiceException(Exception e) {
    super(e);
  }

  public VPinServiceException(String msg, Exception e) {
    super(msg, e);
  }
}
