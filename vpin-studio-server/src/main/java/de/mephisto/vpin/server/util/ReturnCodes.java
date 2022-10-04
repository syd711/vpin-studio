package de.mephisto.vpin.server.util;

/**
 *
 */
public enum ReturnCodes {
  OK;

  @Override
  public String toString() {
    return super.toString();
  }

  public boolean isOk() {
    return this.equals(OK);
  }
}
