package de.mephisto.vpin.connectors.iscored;

import edu.umd.cs.findbugs.annotations.NonNull;

public class IScored {

  public static GameRoom createGameRoom(@NonNull String url) {
    GameRoom gameRoom = new GameRoom(url);
    gameRoom.load();
    return gameRoom;
  }
}
