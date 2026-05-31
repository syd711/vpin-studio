package de.mephisto.vpin.ui.tables;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.pinvol.PinVolPreferences;
import de.mephisto.vpin.restclient.validation.ValidationSettings;
import de.mephisto.vpin.ui.tables.panels.BaseColumnSorter;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.util.Comparator;

import static de.mephisto.vpin.ui.Studio.client;

public class TableOverviewColumnSorter implements BaseColumnSorter<GameRepresentationModel> {

  private final TableOverviewController tableOverviewController;

  public TableOverviewColumnSorter(TableOverviewController tableOverviewController) {
    this.tableOverviewController = tableOverviewController;
  }

  public Comparator<GameRepresentationModel> buildComparator(TableView<GameRepresentationModel> tableView) {
    Comparator<GameRepresentationModel> comp = null;
    ValidationSettings validationSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.VALIDATION_SETTINGS, ValidationSettings.class);

    if (!tableView.getSortOrder().isEmpty()) {
      TableColumn<GameRepresentationModel, ?> column = tableView.getSortOrder().getFirst();

      if (column.equals(tableOverviewController.columnDisplayName)) {
        comp = Comparator.comparing(o -> o.getGame().getGameDisplayName().toLowerCase());
      }
      else if (column.equals(tableOverviewController.columnVersion)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getVersion()));
      }
      else if (column.equals(tableOverviewController.columnPatchVersion)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getPatchVersion()));
      }
      else if (column.equals(tableOverviewController.columnStatus)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getValidationState().getCode()));
      }
      else if (column.equals(tableOverviewController.columnRating)) {
        comp = Comparator.comparing(o -> o.getGame().getRating());
      }
      else if (column.equals(tableOverviewController.columnEmulator)) {
        comp = Comparator.comparing(o -> o.getGame().getEmulatorId());
      }
      else if (column.equals(tableOverviewController.columnDateAdded)) {
        comp = (o1, o2) -> {
          if (o1.getGame().getDateAdded() == null && o2.getGame().getDateAdded() == null) return 0;
          if (o1.getGame().getDateAdded() == null) return 1;
          if (o2.getGame().getDateAdded() == null) return -1;
          return o1.getGame().getDateAdded().compareTo(o2.getGame().getDateAdded());
        };
      }
      else if (column.equals(tableOverviewController.columnDateModified)) {
        comp = (o1, o2) -> {
          if (o1.getGame().getDateUpdated() == null && o2.getGame().getDateUpdated() == null) return 0;
          if (o1.getGame().getDateUpdated() == null) return 1;
          if (o2.getGame().getDateUpdated() == null) return -1;
          return o1.getGame().getDateUpdated().compareTo(o2.getGame().getDateUpdated());
        };
      }
      else if (column.equals(tableOverviewController.columnBackupDate)) {
        comp = (o1, o2) -> {
          if (o1.backupDate == null && o2.backupDate == null) return 0;
          if (o1.backupDate == null) return 1;
          if (o2.backupDate == null) return -1;
          return o1.backupDate.compareTo(o2.backupDate);
        };
      }
      else if (column.equals(tableOverviewController.columnB2S)) {
        comp = Comparator.comparing(o -> o.getGame().getDirectB2SPath() != null);
      }
      else if (column.equals(tableOverviewController.columnVPS)) {
        comp = Comparator.comparing(o -> !o.getGame().getVpsUpdates().isEmpty());
      }
      else if (column.equals(tableOverviewController.columnPUPPack)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getPupPackName()));
      }
      else if (column.equals(tableOverviewController.columnAltColor)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getAltColorType()));
      }
      else if (column.equals(tableOverviewController.columnAltSound)) {
        comp = Comparator.comparing(o -> o.getGame().isAltSoundAvailable());
      }
      else if (column.equals(tableOverviewController.columnMusic)) {
        comp = Comparator.comparing(o -> o.getGame().isMusicScripted());
      }
      else if (column.equals(tableOverviewController.columnRom)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getRom()));
      }
      else if (column.equals(tableOverviewController.columnPOV)) {
        comp = Comparator.comparing(o -> o.getGame().getPovPath() != null);
      }
      else if (column.equals(tableOverviewController.columnRES)) {
        comp = Comparator.comparing(o -> o.getGame().getResPath() != null);
      }
      else if (column.equals(tableOverviewController.columnVBS)) {
        comp = Comparator.comparing(o -> o.getGame().getVbsPath() != null);
      }
      else if (column.equals(tableOverviewController.columnPinVol)) {
        comp = Comparator.comparing(o -> {
          String key = PinVolPreferences.getKey(o.getGame().getGameFileName(), client.getEmulatorService().isVpxGame(o.getGame()), client.getEmulatorService().isFpGame(o.getGame()));
          return client.getPinVolService().getPinVolTablePreferences().contains(key);
        });
      }
      else if (column.equals(tableOverviewController.columnINI)) {
        comp = Comparator.comparing(o -> o.getGame().getIniPath() != null);
      }
      else if (column.equals(tableOverviewController.columnHSType)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getHighscoreType()));
      }
      else if (column.equals(tableOverviewController.columnLauncher)) {
        comp = Comparator.comparing(o -> String.valueOf(o.getGame().getLauncher()));
      }
      else if (column.equals(tableOverviewController.columnPlayfield)) {
        comp = Comparator.comparing(o -> o.getStatusColor(VPinScreen.PlayField, validationSettings));
      }
      else if (column.equals(tableOverviewController.columnBackglass)) {
        comp = Comparator.comparing(o -> o.getStatusColor(VPinScreen.BackGlass, validationSettings));
      }
      else if (column.equals(tableOverviewController.columnLoading)) {
        comp = Comparator.comparing(o -> o.getStatusColor(VPinScreen.Loading, validationSettings));
      }
      else if (column.equals(tableOverviewController.columnLogo)) {
        comp = Comparator.comparing(o -> o.getStatusColor(VPinScreen.Logo, validationSettings));
      }
      else if (column.equals(tableOverviewController.columnWheel)) {
        comp = Comparator.comparing(o -> o.getStatusColor(VPinScreen.Wheel, validationSettings));
      }
      else if (column.equals(tableOverviewController.columnDMD)) {
        comp = Comparator.comparing(o -> o.getStatusColor(VPinScreen.DMD, validationSettings));
      }
      else if (column.equals(tableOverviewController.columnTopper)) {
        comp = Comparator.comparing(o -> o.getStatusColor(VPinScreen.Topper, validationSettings));
      }
      else if (column.equals(tableOverviewController.columnFullDMD)) {
        comp = Comparator.comparing(o -> o.getStatusColor(VPinScreen.Menu, validationSettings));
      }
      else if (column.equals(tableOverviewController.columnAudio)) {
        comp = Comparator.comparing(o -> o.getStatusColor(VPinScreen.Audio, validationSettings));
      }
      else if (column.equals(tableOverviewController.columnAudioLaunch)) {
        comp = Comparator.comparing(o -> o.getStatusColor(VPinScreen.Audio, validationSettings));
      }
      else if (column.equals(tableOverviewController.columnInfo)) {
        comp = Comparator.comparing(o -> o.getStatusColor(VPinScreen.GameInfo, validationSettings));
      }
      else if (column.equals(tableOverviewController.columnHelp)) {
        comp = Comparator.comparing(o -> o.getStatusColor(VPinScreen.GameHelp, validationSettings));
      }
      else if (column.equals(tableOverviewController.columnOther2)) {
        comp = Comparator.comparing(o -> o.getStatusColor(VPinScreen.Other2, validationSettings));
      }

      // optionally reverse order
      if (comp != null && column.getSortType().equals(TableColumn.SortType.DESCENDING)) {
        comp = comp.reversed();
      }
    }
    return comp;
  }
}
