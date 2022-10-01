package de.mephisto.vpin.server;

import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.popper.PopperScreen;
import de.mephisto.vpin.server.util.SystemInfo;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Date;

public class GameInfo {

  private String rom;
  private String gameDisplayName;
  private String gameFileName;
  private int id;

  private File gameFile;
  private File romFile;
  private File nvRamFile;
  private File wheelIconFile;

  private Date lastPlayed;
  private int numberPlays;

  private final VPinService service;

  public GameInfo(VPinService service) {
    this.service = service;
  }

  public Highscore resolveHighscore() {
    return this.service.getHighscore(this);
  }

  @SuppressWarnings("unused")
  public boolean hasHighscore() {
    if (this.getNvRamFile() != null && this.getNvRamFile().exists()) {
      return true;
    }

    if (this.getVPRegFolder() != null && this.getVPRegFolder().exists()) {
      return true;
    }
    return false;
  }

  @SuppressWarnings("unused")
  @NonNull
  public File getPopperScreenMedia(@NonNull PopperScreen screen) {
    File emuMedia = new File(SystemInfo.getInstance().getPinUPMediaFolder(), getEmulatorName());
    File mediaFolder = new File(emuMedia, screen.name());
    return new File(mediaFolder, FilenameUtils.getBaseName(this.getGameFile().getName()) + ".png");
  }

  @NonNull
  public String getEmulatorName() {
    File gameFile = getGameFile();
    if (gameFile.getName().endsWith(".vpx")) {
      return "Visual Pinball X";
    }
    else if (gameFile.getName().endsWith(".fp")) {
      return "Future Pinball";
    }
    return "Visual Pinball X";
  }

  @Nullable
  public File getVPRegFolder() {
    if (!StringUtils.isEmpty(this.getRom())) {
      return new File(SystemInfo.getInstance().getExtractedVPRegFolder(), getRom());
    }
    return null;
  }

  @SuppressWarnings("unused")
  public int getNumberPlays() {
    return numberPlays;
  }

  public void setNumberPlays(int numberPlays) {
    this.numberPlays = numberPlays;
  }

  public void rescanRom() {
    service.rescanRom(this);
  }

  @SuppressWarnings("unused")
  @Nullable
  public Date getLastPlayed() {
    return lastPlayed;
  }

  public void setLastPlayed(Date lastPlayed) {
    this.lastPlayed = lastPlayed;
  }

  @SuppressWarnings("unused")
  @NonNull
  public File getWheelIconFile() {
    return wheelIconFile;
  }

  public void setWheelIconFile(@NonNull File wheelIconFile) {
    this.wheelIconFile = wheelIconFile;
  }

  @Nullable
  public File getNvRamFile() {
    return nvRamFile;
  }

  public void setNvRamFile(@Nullable File nvRamFile) {
    this.nvRamFile = nvRamFile;
  }

  @NonNull
  public File getGameFile() {
    return gameFile;
  }

  public void setGameFile(@NonNull File gameFile) {
    this.gameFile = gameFile;
  }

  public String getRom() {
    return rom;
  }

  public void setRom(String rom) {
    this.rom = rom;
  }

  public String getGameDisplayName() {
    return gameDisplayName;
  }

  public void setGameDisplayName(String gameDisplayName) {
    this.gameDisplayName = gameDisplayName;
  }

  public String getGameFileName() {
    return gameFileName;
  }

  public void setGameFileName(String gameFileName) {
    this.gameFileName = gameFileName;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @SuppressWarnings("unused")
  @Nullable
  public File getRomFile() {
    return romFile;
  }

  public void setRomFile(File romFile) {
    this.romFile = romFile;
  }

  @NonNull
  public File getDirectB2SFile() {
    String baseName = FilenameUtils.getBaseName(this.getGameFileName());
    return new File(SystemInfo.getInstance().getDirectB2SFolder(), baseName + ".directb2s");
  }

  @NonNull
  public File getDirectB2SImage() {
    String targetName = FilenameUtils.getBaseName(getGameFileName()) + ".png";
    return new File(SystemInfo.getInstance().getB2SImageExtractionFolder(), targetName);
  }

  @Override
  public String toString() {
    return this.getGameDisplayName();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GameInfo gameInfo = (GameInfo) o;

    return id == gameInfo.id;
  }

  @Override
  public int hashCode() {
    return id;
  }
}
