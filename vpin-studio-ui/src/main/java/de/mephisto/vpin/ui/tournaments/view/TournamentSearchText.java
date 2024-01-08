package de.mephisto.vpin.ui.tournaments.view;

import de.mephisto.vpin.connectors.mania.model.ManiaTournamentSearchResultItem;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class TournamentSearchText extends VBox {

  public TournamentSearchText(ManiaTournamentSearchResultItem item) {
    super(3);

    Label titleLabel = new Label(item.getDisplayName());
    titleLabel.getStyleClass().add("default-headline");
    getChildren().add(titleLabel);

    Label descriptionLabel = new Label(item.getDescription());
    descriptionLabel.setWrapText(true);
    descriptionLabel.getStyleClass().add("default-text");
    getChildren().add(descriptionLabel);

    this.setPrefHeight(70);
  }
}
