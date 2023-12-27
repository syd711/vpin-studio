package de.mephisto.vpin.ui.tournaments.view;

import de.mephisto.vpin.connectors.mania.model.ManiaTournamentRepresentation;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import static de.mephisto.vpin.ui.Studio.maniaClient;

public class TournamentCellContainer extends HBox {
  private final static int TITLE_WIDTH = 60;

  public TournamentCellContainer(ManiaTournamentRepresentation tournament) {
    super(6);

    Image image = new Image(maniaClient.getTournamentClient().getBadgeUrl(tournament));
    ImageView view = new ImageView(image);
    view.setPreserveRatio(true);
    view.setSmooth(true);
    view.setFitWidth(70);

    this.getChildren().add(view);

    VBox entries = new VBox(3);

    Label name = new Label(tournament.getDisplayName());
    name.getStyleClass().add("default-headline");
    entries.getChildren().add(name);

    HBox row = new HBox(6);
    Label titleLabel = new Label("Owner:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.getStyleClass().add("default-headline");
    Label valueLabel = new Label("TODO");
    valueLabel.getStyleClass().add("default-text");
    row.getChildren().addAll(titleLabel, valueLabel);
    entries.getChildren().add(row);

    row = new HBox(6);
    titleLabel = new Label("Tables:");
    titleLabel.setPrefWidth(TITLE_WIDTH);
    titleLabel.getStyleClass().add("default-headline");
    valueLabel = new Label(String.valueOf(tournament.getTableIdList().size()));
    valueLabel.getStyleClass().add("default-text");
    row.getChildren().addAll(titleLabel, valueLabel);
    entries.getChildren().add(row);

    this.getChildren().add(entries);
  }
}
