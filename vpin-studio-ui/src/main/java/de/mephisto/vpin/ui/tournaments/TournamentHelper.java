package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.connectors.mania.model.Tournament;

import static de.mephisto.vpin.ui.Studio.client;

public class TournamentHelper {

  public static boolean isOwner(Tournament selectedTournament) {
    //it's a new tournament
    if (selectedTournament.getUuid() == null) {
      return true;
    }

    //TODO mania
//    if (selectedTournament.getCabinetId() > 0 && selectedTournament.getCabinetId()  client.getTournamentsService().getConfig().getSystemId())) {
//      return true;
//    }

    return false;
  }
}
