package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;

import static de.mephisto.vpin.ui.Studio.client;

public class TournamentHelper {

  public static boolean isOwner(ManiaTournamentRepresentation selectedTournament) {
    //it's a new tournament
    if (selectedTournament.getUuid() == null) {
      return true;
    }

    if (selectedTournament.getCabinetId() != null && selectedTournament.getCabinetId().equals(client.getTournamentsService().getConfig().getCabinetId())) {
      return true;
    }

    return false;
  }
}
