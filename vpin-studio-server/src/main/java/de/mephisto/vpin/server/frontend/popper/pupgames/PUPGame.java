package de.mephisto.vpin.server.frontend.popper.pupgames;

import de.mephisto.vpin.restclient.frontend.TableDetails;

import java.util.Objects;

public class PUPGame {
  private String Author;
  private String GameName;
  private String GameFileName;
  private String GameDisplay;
  private String GameType;
  private String GameTheme;
  private int GameYear;
  private String GAMEVER;
  private String Category;
  private String Notes;
  private String ROM;
  private String Manufact;
  private String AltRunMode;
  private String LaunchCustomVar;
  private String GKeepDisplays;
  private int NumPlayers;
  private String CUSTOM2;
  private String CUSTOM3;

  public String getAuthor() {
    return Author;
  }

  public void setAuthor(String author) {
    Author = author;
  }

  public String getGameName() {
    return GameName;
  }

  public void setGameName(String gameName) {
    GameName = gameName;
  }

  public String getGameFileName() {
    return GameFileName;
  }

  public void setGameFileName(String gameFileName) {
    GameFileName = gameFileName;
  }

  public String getGameDisplay() {
    return GameDisplay;
  }

  public void setGameDisplay(String gameDisplay) {
    GameDisplay = gameDisplay;
  }

  public String getGameType() {
    return GameType;
  }

  public void setGameType(String gameType) {
    GameType = gameType;
  }

  public String getGameTheme() {
    return GameTheme;
  }

  public void setGameTheme(String gameTheme) {
    GameTheme = gameTheme;
  }

  public int getGameYear() {
    return GameYear;
  }

  public void setGameYear(int gameYear) {
    GameYear = gameYear;
  }

  public String getGAMEVER() {
    return GAMEVER;
  }

  public void setGAMEVER(String GAMEVER) {
    this.GAMEVER = GAMEVER;
  }

  public String getCategory() {
    return Category;
  }

  public void setCategory(String category) {
    Category = category;
  }

  public String getNotes() {
    return Notes;
  }

  public void setNotes(String notes) {
    Notes = notes;
  }

  public String getROM() {
    return ROM;
  }

  public void setROM(String ROM) {
    this.ROM = ROM;
  }

  public String getManufact() {
    return Manufact;
  }

  public void setManufact(String manufact) {
    Manufact = manufact;
  }

  public String getAltRunMode() {
    return AltRunMode;
  }

  public void setAltRunMode(String altRunMode) {
    AltRunMode = altRunMode;
  }

  public String getLaunchCustomVar() {
    return LaunchCustomVar;
  }

  public void setLaunchCustomVar(String launchCustomVar) {
    LaunchCustomVar = launchCustomVar;
  }

  public String getGKeepDisplays() {
    return GKeepDisplays;
  }

  public void setGKeepDisplays(String GKeepDisplays) {
    this.GKeepDisplays = GKeepDisplays;
  }

  public int getNumPlayers() {
    return NumPlayers;
  }

  public void setNumPlayers(int numPlayers) {
    NumPlayers = numPlayers;
  }

  public String getCUSTOM2() {
    return CUSTOM2;
  }

  public void setCUSTOM2(String CUSTOM2) {
    this.CUSTOM2 = CUSTOM2;
  }

  public String getCUSTOM3() {
    return CUSTOM3;
  }

  public void setCUSTOM3(String CUSTOM3) {
    this.CUSTOM3 = CUSTOM3;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    PUPGame pupGame = (PUPGame) o;
    return Objects.equals(Author, pupGame.Author) && Objects.equals(GameName, pupGame.GameName) && Objects.equals(GameFileName, pupGame.GameFileName) && Objects.equals(GameDisplay, pupGame.GameDisplay) && Objects.equals(GameType, pupGame.GameType) && Objects.equals(GameTheme, pupGame.GameTheme) && Objects.equals(GameYear, pupGame.GameYear) && Objects.equals(GAMEVER, pupGame.GAMEVER) && Objects.equals(Category, pupGame.Category) && Objects.equals(Notes, pupGame.Notes) && Objects.equals(ROM, pupGame.ROM) && Objects.equals(Manufact, pupGame.Manufact) && Objects.equals(AltRunMode, pupGame.AltRunMode) && Objects.equals(LaunchCustomVar, pupGame.LaunchCustomVar) && Objects.equals(GKeepDisplays, pupGame.GKeepDisplays) && Objects.equals(NumPlayers, pupGame.NumPlayers) && Objects.equals(CUSTOM2, pupGame.CUSTOM2) && Objects.equals(CUSTOM3, pupGame.CUSTOM3);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Author, GameName, GameFileName, GameDisplay, GameType, GameTheme, GameYear, GAMEVER, Category, Notes, ROM, Manufact, AltRunMode, LaunchCustomVar, GKeepDisplays, NumPlayers, CUSTOM2, CUSTOM3);
  }

  @Override
  public String toString() {
    return "PUP Game \"" + getGameDisplay() + "\"";
  }

  public TableDetails toTableDetails() {
    TableDetails tableDetails = new TableDetails();
    tableDetails.setAuthor(this.Author);
    tableDetails.setGameType(this.GameType);
    tableDetails.setGameTheme(this.GameTheme);
    tableDetails.setGameName(this.GameName);
    tableDetails.setGameYear(this.GameYear);
    tableDetails.setGameFileName(this.GameFileName);
    tableDetails.setGameDisplayName(this.GameDisplay);
    tableDetails.setGameVersion(this.GAMEVER);
    tableDetails.setCategory(this.Category);
    tableDetails.setNotes(this.Notes);
    tableDetails.setRomName(this.ROM);
    tableDetails.setAltRunMode(this.AltRunMode);
    tableDetails.setLaunchCustomVar(this.LaunchCustomVar);
    tableDetails.setKeepDisplays(this.GKeepDisplays);
    tableDetails.setNumberOfPlayers(this.NumPlayers);
    tableDetails.setCustom2(this.CUSTOM2);
    tableDetails.setCustom3(this.CUSTOM3);
    return tableDetails;
  }
}
