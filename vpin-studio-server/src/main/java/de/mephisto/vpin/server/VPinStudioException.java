package de.mephisto.vpin.server;

public class VPinStudioException extends Exception {
  public VPinStudioException(Exception e) {
    super(e);
  }

  public VPinStudioException(String msg, Exception e) {
    super(msg, e);
  }
}
