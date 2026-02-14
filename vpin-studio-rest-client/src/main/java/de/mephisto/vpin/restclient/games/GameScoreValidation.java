package de.mephisto.vpin.restclient.games;

public class GameScoreValidation {
  public final static String UNPLAYED_ICON = "bi-check2-circle";

  public final static String OK_ICON = "bi-check2-circle";
  public final static String OK_COLOR = "#66FF66";

  public final static String ERROR_ICON = "bi-x-circle";
  public final static String ERROR_COLOR = "#FF9933";

  public final static String STATUS_ROM_NOT_FOUND = "The ROM name does not exist.";
  public final static String STATUS_ROM_MATCH_FOUND = "A matching highscore entry has been found for this ROM name.";
  public final static String STATUS_ROM_ALIASED_MATCH_FOUND = "A matching highscore entry has been found for this aliased ROM name.";
  public final static String STATUS_VPREG_STG_MATCH_FOUND = "A matching highscore entry has been found for this ROM name.";
  public final static String STATUS_HSFILE_MATCH_FOUND = "A matching highscore file has been found.";

  public final static String STATUS_FIELDS_NOT_SET = "Neither ROM name nor highscore filename is set.";
  public final static String STATUS_ROM_NOT_SUPPORTED = "This ROM is currently not supported by the highscore parser.";
  public final static String STATUS_HSFILE_NOT_SUPPORTED = "This highscore text file not supported by the highscore parser.";

  public final static String STATUS_NOT_PLAYED_HSFILE_NOT_FOUND = "No highscore text file has been found for this name, but the table has not been played yet.";
  public final static String STATUS_NOT_PLAYED_NO_MATCH_FOUND = "No .nvram file or entry in VPReg.stg has been found, but the table has not been played yet.";

  public final static String STATUS_PLAYED_HSFILE_NOT_FOUND = "Table has been played, but no highscore text file has been found for this name.";
  public final static String STATUS_PLAYED_NO_MATCH_FOUND = "Table has been played, but no .nvram file or VPReg.stg entry has been found.";


  private String highscoreFilenameStatus;
  private String highscoreFilenameIcon;
  private String highscoreFilenameIconColor = "#FFFFFF";

  private String romStatus;
  private String romIcon;
  private String romIconColor = "#FFFFFF";

  private boolean validScoreConfiguration;

  public String getHighscoreFilenameIconColor() {
    return highscoreFilenameIconColor;
  }

  public void setHighscoreFilenameIconColor(String highscoreFilenameIconColor) {
    this.highscoreFilenameIconColor = highscoreFilenameIconColor;
  }

  public String getRomIconColor() {
    return romIconColor;
  }

  public void setRomIconColor(String romIconColor) {
    this.romIconColor = romIconColor;
  }

  public String getHighscoreFilenameStatus() {
    return highscoreFilenameStatus;
  }

  public void setHighscoreFilenameStatus(String highscoreFilenameStatus) {
    this.highscoreFilenameStatus = highscoreFilenameStatus;
  }

  public String getHighscoreFilenameIcon() {
    return highscoreFilenameIcon;
  }

  public void setHighscoreFilenameIcon(String highscoreFilenameIcon) {
    this.highscoreFilenameIcon = highscoreFilenameIcon;
  }

  public String getRomStatus() {
    return romStatus;
  }

  public void setRomStatus(String romStatus) {
    this.romStatus = romStatus;
  }

  public String getRomIcon() {
    return romIcon;
  }

  public void setRomIcon(String romIcon) {
    this.romIcon = romIcon;
  }

  public boolean isValidScoreConfiguration() {
    return validScoreConfiguration;
  }

  public void setValidScoreConfiguration(boolean validScoreConfiguration) {
    this.validScoreConfiguration = validScoreConfiguration;
  }
}
