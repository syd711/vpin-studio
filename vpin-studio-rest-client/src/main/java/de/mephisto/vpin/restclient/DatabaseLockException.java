package de.mephisto.vpin.restclient;

public class DatabaseLockException extends Exception {
  public DatabaseLockException(Exception e) {
    super(e);
  }
}
