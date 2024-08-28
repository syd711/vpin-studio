package de.mephisto.vpin.ui.backglassmanager;

import de.mephisto.vpin.restclient.directb2s.DirectB2S;
import de.mephisto.vpin.ui.backglassmanager.BackglassManagerController.DirectB2SEntryModel;
import de.mephisto.vpin.ui.tables.models.B2SVisibility;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class BackglassManagerPredicateFactory {

  private String filterValue;

  private BackglassManagerController controller;

  private List<Integer> emulatorIds = new ArrayList<>();

  public BackglassManagerPredicateFactory(BackglassManagerController controller) {
    this.controller = controller;
  }

  public void setFilterTerm(String filterTerm) {
    this.filterValue = filterTerm;
  }

  public void selectEmulator(Integer id) {
    emulatorIds.add(id);
  }
  public void unselectEmulator(Integer id) {
    emulatorIds.remove(id);
  }

  /**
   * We need a new Predicate each time else TableView does not detect the changes
   */
  public Predicate<DirectB2SEntryModel> buildPredicate() {
    return new Predicate<DirectB2SEntryModel>() {
      @Override
      public boolean test(DirectB2SEntryModel model) {
        DirectB2S backglass = model.getBacklass();

        if (emulatorIds!=null && !emulatorIds.isEmpty() && !emulatorIds.contains(backglass.getEmulatorId())) {
          return false;
        }
        if (StringUtils.isNotEmpty(filterValue) && !StringUtils.containsIgnoreCase(backglass.getName(), filterValue)) {
          return false;
        }

        if (controller.missingDMDImageFilter.getValue() && model.hasDmd()) {
          return false;
        }
        if (controller.notFullDMDRatioFilter.getValue() && (!model.hasDmd() || model.isFullDmd())) {
          return false;
        }
        if (controller.scoresAvailableFilter.getValue() && model.getNbScores() <= 0) {
          return false;
        }
        if (controller.missingTableFilter.getValue() && model.isVpxAvailable()) {
          return false;
        }
        if (equalsVisibility(controller.grillVisibilityFilter.getValue(), model.getHideGrill())) {
          return false;
        }
        if (controller.backglassVisibilityFilter.getValue() && !model.isHideBackglass()) {
          return false;
        }
        if (controller.b2sdmdVisibilityFilter.getValue() && !model.isHideB2SDMD()) {
          return false;
        }
        if (equalsVisibility(controller.dmdVisibilityFilter.getValue(), model.getHideDMD())) {
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
