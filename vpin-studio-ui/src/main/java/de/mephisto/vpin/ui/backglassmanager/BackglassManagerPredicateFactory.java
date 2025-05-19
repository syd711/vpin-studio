package de.mephisto.vpin.ui.backglassmanager;

import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.ui.tables.models.B2SVisibility;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BackglassManagerPredicateFactory {

  private final List<Integer> emulatorIds = new ArrayList<>();

    //--------------- Filters

  Property<Boolean> missingDMDImageFilter = new SimpleBooleanProperty(false);
  Property<Boolean> notFullDMDRatioFilter = new SimpleBooleanProperty(false);
  Property<Boolean> scoresAvailableFilter = new SimpleBooleanProperty(false);
  Property<Boolean> missingTableFilter = new SimpleBooleanProperty(false);

  Property<B2SVisibility> grillVisibilityFilter = new SimpleObjectProperty<B2SVisibility>();
  Property<Boolean> b2sdmdVisibilityFilter = new SimpleBooleanProperty(false);
  Property<Boolean> backglassVisibilityFilter = new SimpleBooleanProperty(false);
  Property<B2SVisibility> dmdVisibilityFilter = new SimpleObjectProperty<B2SVisibility>();


  public void selectEmulator(Integer id) {
    emulatorIds.add(id);
  }
  public void unselectEmulator(Integer id) {
    emulatorIds.remove(id);
  }

  /**
   * We need a new Predicate each time else TableView does not detect the changes
   */
  public Predicate<DirectB2SModel> buildPredicate(String searchTerm, PlaylistRepresentation playlist) {
    return new Predicate<DirectB2SModel>() {
      @Override
      public boolean test(DirectB2SModel model) {
        DirectB2S backglass = model.getBacklass();

        if (emulatorIds!=null && !emulatorIds.isEmpty() && !emulatorIds.contains(backglass.getEmulatorId())) {
          return false;
        }

        if (playlist != null && !playlist.containsGame(model.getGameId())) {
          return false;
        }

        if (StringUtils.isNotEmpty(searchTerm) && !StringUtils.containsIgnoreCase(backglass.getName(), searchTerm)) {
          return false;
        }

        if (missingDMDImageFilter.getValue() && model.hasDmd()) {
          return false;
        }
        if (notFullDMDRatioFilter.getValue() && (!model.hasDmd() || model.isFullDmd())) {
          return false;
        }
        if (scoresAvailableFilter.getValue() && model.getNbScores() <= 0) {
          return false;
        }
        if (missingTableFilter.getValue() && model._isGameAvailable()) {
          return false;
        }
        if (equalsVisibility(grillVisibilityFilter.getValue(), model.getHideGrill())) {
          return false;
        }
        if (backglassVisibilityFilter.getValue() && !model.isHideBackglass()) {
          return false;
        }
        if (b2sdmdVisibilityFilter.getValue() && !model.isHideB2SDMD()) {
          return false;
        }
        if (equalsVisibility(dmdVisibilityFilter.getValue(), model.getHideDMD())) {
          return false;
        }

        // else not filtered
        return true;
      }

      private boolean equalsVisibility(B2SVisibility value, int hide) {
        return value != null && value.getId() >= 0 && value.getId() != hide;
      }
    };
  }

}
