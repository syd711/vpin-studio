package de.mephisto.vpin.ui.tournaments.view;

import de.mephisto.vpin.connectors.mania.model.TournamentSearchResultItem;
import de.mephisto.vpin.connectors.mania.model.TournamentTable;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;
import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentSearchTableSummary extends VBox {

  public TournamentSearchTableSummary(TournamentSearchResultItem item) {
    super(3);

    List<TournamentTable> tournamentTables = maniaClient.getTournamentClient().getTournamentTables(item.getId());
    int count = 0;
    Tooltip tt = new Tooltip();
    StringBuilder b = new StringBuilder();

    for (TournamentTable tournamentTable : tournamentTables) {
      VpsTable vpsTable = client.getVpsService().getTableById(tournamentTable.getVpsTableId());
      if(vpsTable != null) {
        b.append("- ");
        b.append(vpsTable.getDisplayName());
        b.append("\n");
      }

      if (vpsTable != null && count < 3) {
        Label titleLabel = new Label(vpsTable.getDisplayName());
        titleLabel.getStyleClass().add("default-text");
        getChildren().add(titleLabel);
      }
      count++;
    }
    tt.setText(b.toString());
    if(count > 3) {
      Label titleLabel = new Label("(+" + (tournamentTables.size()-3) + ")");
      titleLabel.getStyleClass().add("default-headline");
      titleLabel.setTooltip(tt);

      getChildren().add(titleLabel);
    }

  }
}
