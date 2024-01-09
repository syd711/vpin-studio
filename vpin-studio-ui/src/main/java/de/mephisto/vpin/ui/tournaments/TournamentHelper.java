package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;

import static de.mephisto.vpin.ui.Studio.client;

public class TournamentHelper {

  public static boolean isOwner(ManiaTournamentRepresentation selectedTournament) {
    return selectedTournament.getUuid() == null || selectedTournament.getCabinetId() == null || selectedTournament.getCabinetId().equals(client.getTournamentsService().getConfig().getCabinetId());
  }
}
