package de.mephisto.vpin.ui.tournaments;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.mania.model.Cabinet;
import de.mephisto.vpin.connectors.mania.model.Tournament;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.restclient.games.GameRepresentation;

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

  public static String getIconColor(TournamentTable value) {
    if (!value.isEnabled()) {
      return "#B0ABAB";
    }
    return null;
  }

  public static String getLabelCss(TournamentTable value) {
    String status = "";
    if (!value.isEnabled()) {
      status = WidgetFactory.DISABLED_TEXT_STYLE;
    }
    return status;
  }
}
