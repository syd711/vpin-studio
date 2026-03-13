package de.mephisto.vpin.restclient.backups;

public class BackupDataStudio {
  public final static String BACKUP_FILENAME = "vpin-studio.json";
  private String comment;
  private String ignoredValidations;
  private boolean cardsDisabled;

  public boolean isCardsDisabled() {
    return cardsDisabled;
  }

  public void setCardsDisabled(boolean cardsDisabled) {
    this.cardsDisabled = cardsDisabled;
  }

  public String getIgnoredValidations() {
    return ignoredValidations;
  }

  public void setIgnoredValidations(String ignoredValidations) {
    this.ignoredValidations = ignoredValidations;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
}
