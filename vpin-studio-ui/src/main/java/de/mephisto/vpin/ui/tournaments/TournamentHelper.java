package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.Tournament;

public class TournamentHelper {

  public static boolean isOwner(Tournament selectedTournament, Cabinet cabinet) {
    //it's a new tournament
    if (selectedTournament.getUuid() == null) {
      return true;
    }


    if (selectedTournament.getCabinetId() == cabinet.getId()) {
      return true;
    }

    return false;
  }
}
