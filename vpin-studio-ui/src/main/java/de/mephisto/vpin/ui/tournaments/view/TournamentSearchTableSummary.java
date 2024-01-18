package de.mephisto.vpin.ui.tournaments.view;

import de.mephisto.vpin.connectors.mania.model.TournamentSearchResultItem;
import de.mephisto.vpin.connectors.vps.VPS;
import de.mephisto.vpin.connectors.vps.model.VpsTable;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.util.List;

public class TournamentSearchTableSummary extends VBox {

  public TournamentSearchTableSummary(TournamentSearchResultItem item) {
    super(3);
    List<String> tableIdList = item.getTableIdList();
    int count = 0;
    Tooltip tt = new Tooltip();
    StringBuilder b = new StringBuilder();
    for (String tableId : tableIdList) {
      String[] split = tableId.split("#");
      VpsTable vpsTable = VPS.getInstance().getTableById(split[0]);
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
      Label titleLabel = new Label("(+" + (tableIdList.size()-3) + ")");
      titleLabel.getStyleClass().add("default-headline");
      titleLabel.setTooltip(tt);
      getChildren().add(titleLabel);
    }

  }
}
