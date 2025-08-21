package de.mephisto.vpin.ui.cards;

import de.mephisto.vpin.restclient.emulators.GameEmulatorRepresentation;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.ui.tables.GameRepresentationModel;
import de.mephisto.vpin.ui.tables.panels.BaseFilterController;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Predicate;

import static de.mephisto.vpin.ui.Studio.client;

public class HighscoreCardsFilterController extends BaseFilterController<GameRepresentation, GameRepresentationModel> implements Initializable {

  @FXML
  private VBox emulatorFilters;

  private final List<CheckBox> emulatorCheckboxes = new ArrayList<>();

  private HighscoreCardsPredicateFactory predicateFactory;
  

  protected void resetFilters() {
    for (CheckBox cb : emulatorCheckboxes) {
      cb.setSelected(true);
    }
  }

  @Override
  protected boolean hasFilter() {
    boolean hasFilter = false;

    for (CheckBox cb : emulatorCheckboxes) {
      hasFilter |= !cb.isSelected();
    }
    return hasFilter;
  }

  @Override
  public Predicate<GameRepresentationModel> buildPredicate(String searchTerm, PlaylistRepresentation playlist) {
    return predicateFactory.buildPredicate(searchTerm, playlist);
  }

  //--------------------------------
  @Override
  public void initialize(URL location, ResourceBundle resources) {

    predicateFactory = new HighscoreCardsPredicateFactory();

    List<GameEmulatorRepresentation> emulators = client.getEmulatorService().getBackglassGameEmulators();
    for (GameEmulatorRepresentation gameEmulator: emulators) {
      CheckBox checkBox = new CheckBox(gameEmulator.getName());
      checkBox.setStyle("-fx-font-size: 14px;-fx-padding: 0 6 0 6;");
      checkBox.setPrefHeight(30);
      checkBox.setSelected(true);
      checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
        @Override
        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
          if (newValue) {
            predicateFactory.selectEmulator(gameEmulator.getId());
          } else {
            predicateFactory.unselectEmulator(gameEmulator.getId());
          }
          applyFilters();
        }
      });
      emulatorCheckboxes.add(checkBox);
      emulatorFilters.getChildren().add(checkBox);
    }

  }
}
