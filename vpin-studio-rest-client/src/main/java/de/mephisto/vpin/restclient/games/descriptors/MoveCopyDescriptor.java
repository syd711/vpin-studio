package de.mephisto.vpin.restclient.games.descriptors;

public class MoveCopyDescriptor {
  private int gameId;
  private int targetEmulatorId;
  private boolean move;
  private boolean createSubfolder;
  private SubfolderNaming subfolderNaming = SubfolderNaming.TABLE_NAME;
  private boolean skipVPinMAMEFiles;

  public int getGameId() {
    return gameId;
  }

  public void setGameId(int gameId) {
    this.gameId = gameId;
  }

  public int getTargetEmulatorId() {
    return targetEmulatorId;
  }

  public void setTargetEmulatorId(int targetEmulatorId) {
    this.targetEmulatorId = targetEmulatorId;
  }

  public boolean isMove() {
    return move;
  }

  public void setMove(boolean move) {
    this.move = move;
  }

  public boolean isCreateSubfolder() {
    return createSubfolder;
  }

  public void setCreateSubfolder(boolean createSubfolder) {
    this.createSubfolder = createSubfolder;
  }

  public SubfolderNaming getSubfolderNaming() {
    return subfolderNaming;
  }

  public void setSubfolderNaming(SubfolderNaming subfolderNaming) {
    this.subfolderNaming = subfolderNaming;
  }

  public boolean isSkipVPinMAMEFiles() {
    return skipVPinMAMEFiles;
  }

  public void setSkipVPinMAMEFiles(boolean skipVPinMAMEFiles) {
    this.skipVPinMAMEFiles = skipVPinMAMEFiles;
  }
}
