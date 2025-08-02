package de.mephisto.vpin.ui.preferences.dialogs;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.restclient.backups.BackupDescriptor;
import de.mephisto.vpin.ui.PreferencesController;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.preferences.PreferenceType;
import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static de.mephisto.vpin.ui.Studio.client;

public class BackupRestoreProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(BackupRestoreProgressModel.class);

  private List<String> entries;
  @org.jetbrains.annotations.NotNull
  private final File file;
  private final BackupDescriptor descriptor;
  private final Iterator<String> iterator;

  public BackupRestoreProgressModel(File file, BackupDescriptor descriptor) {
    super("Restoring Backup");
    this.entries = Arrays.asList("Restoring \"" + file.getName() + "\"");
    this.file = file;
    this.descriptor = descriptor;
    this.iterator = entries.iterator();
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return entries.size();
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public boolean hasNext() {
    return this.iterator.hasNext();
  }

  @Override
  public String getNext() {
    return iterator.next();
  }

  @Override
  public String nextToString(String f) {
    return "";
  }

  @Override
  public void finalizeModel(ProgressResultModel progressResultModel) {
    Platform.runLater(() -> {
      WidgetFactory.showInformation(Studio.stage, "Backup Restore Finished", "The backup file was successfully imported.");
      PreferencesController.markDirty(PreferenceType.uiSettings);
    });
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, String entry) {
    try {
      client.getSystemService().restoreSystemBackup(file, descriptor);
      PreferencesController.instance.closePreferences();
    }
    catch (Exception ex) {
      LOG.error("Failed to write backup file: {}", ex.getMessage(), ex);
      WidgetFactory.showAlert(Studio.stage, "Error", "Failed to write backup file: " + ex.getMessage());
    }
  }
}
