package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.highscores.HighscoreBackup;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.events.EventManager;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class HighscoreBackupDeleteProgressModel extends ProgressModel<HighscoreBackup> {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final List<HighscoreBackup> backups;
  private final Iterator<HighscoreBackup> iterator;
  private final int gameId;
  private final String rom;

  public HighscoreBackupDeleteProgressModel(List<HighscoreBackup> backups, int gameId, String rom) {
    super("Deleting Highscore Backups");
    this.backups = backups;
    this.iterator = backups.iterator();
    this.gameId = gameId;
    this.rom = rom;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public boolean isIndeterminate() {
    return backups.size() == 1;
  }

  @Override
  public int getMax() {
    return backups.size();
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public HighscoreBackup getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(HighscoreBackup backup) {
    return backup.getFilename();
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, HighscoreBackup backup) {
    try {
      client.getHigscoreBackupService().delete(rom, backup.getFilename());
    }
    catch (Exception e) {
      LOG.error("Failed to delete backup: " + e.getMessage(), e);
      Platform.runLater(() -> WidgetFactory.showAlert(Studio.stage, "Error", "Failed to delete backup: " + e.getMessage()));
    }
  }
}
