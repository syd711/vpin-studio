package de.mephisto.vpin.server.rest;

/**
 *
 */
public enum ReturnCodes {
  WRONG_PASSWORD,
  PASSWORDS_NOT_MATCHING,
  PASSWORD_NOT_CHANGED,
  INVALID_NEW_PASSWORD,

  UNAUTHORIZED_ACCESS,

  TOKEN_EXPIRED,
  NO_TOKEN_FOUND,
  INVALID_TOKEN,

  EMAIL_IN_USE,
  ACTIVATION_FAILED,
  ACCOUNT_LOCKED,
  ACCOUNT_NOT_FOUND,

  MARK_ACCOUNT_DELETION_FAILED,
  DELETION_FAILED,
  REGISTRATION_FAILED,

  OK;

  @Override
  public String toString() {
    return super.toString();
  }

  public boolean isOk() {
    return this.equals(OK);
  }
}
