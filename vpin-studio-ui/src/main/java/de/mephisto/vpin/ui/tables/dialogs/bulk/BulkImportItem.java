package de.mephisto.vpin.ui.tables.dialogs.bulk;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import javafx.beans.property.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BulkImportItem {
  private final File file;
  private final File root;
  private final ObjectProperty<GameRepresentation> game = new SimpleObjectProperty<>();
  private final ObjectProperty<VPinScreen> screen = new SimpleObjectProperty<>();
  private final ObjectProperty<BulkImportMode> mode = new SimpleObjectProperty<>(BulkImportMode.OVERWRITE);
  private final DoubleProperty score = new SimpleDoubleProperty(0);
  private final StringProperty matchSource = new SimpleStringProperty("");
  private final BooleanProperty screenDetected = new SimpleBooleanProperty(false);
  private final BooleanProperty selected = new SimpleBooleanProperty(false);
  private boolean selectionManual = false;
  private List<BulkImportMatcher.Match> suggestions = new ArrayList<>();

  public BulkImportItem(File file, File root) {
    this.file = file;
    this.root = root;
  }

  public File getFile() {
    return file;
  }

  /**
   * The scanned source folder the file was found in.
   */
  public File getRoot() {
    return root;
  }

  public String getRelativePath() {
    if (root != null) {
      try {
        return root.toPath().relativize(file.toPath()).toString();
      }
      catch (IllegalArgumentException ignored) {
      }
    }
    return file.getName();
  }

  public ObjectProperty<GameRepresentation> gameProperty() {
    return game;
  }

  public ObjectProperty<VPinScreen> screenProperty() {
    return screen;
  }

  public ObjectProperty<BulkImportMode> modeProperty() {
    return mode;
  }

  public DoubleProperty scoreProperty() {
    return score;
  }

  public StringProperty matchSourceProperty() {
    return matchSource;
  }

  public BooleanProperty screenDetectedProperty() {
    return screenDetected;
  }

  public List<BulkImportMatcher.Match> getSuggestions() {
    return suggestions;
  }

  public void setSuggestions(List<BulkImportMatcher.Match> suggestions) {
    this.suggestions = suggestions;
  }

  public boolean isImportable() {
    return game.get() != null && screen.get() != null;
  }

  /**
   * The import checkbox of the row.
   */
  public BooleanProperty selectedProperty() {
    return selected;
  }

  /**
   * True when the user changed the checkbox, the automatic matching does not change it then.
   */
  public boolean isSelectionManual() {
    return selectionManual;
  }

  public void setSelectionManual(boolean selectionManual) {
    this.selectionManual = selectionManual;
  }

  /**
   * Checked and complete, so the file is imported.
   */
  public boolean isImported() {
    return selected.get() && isImportable();
  }
}
