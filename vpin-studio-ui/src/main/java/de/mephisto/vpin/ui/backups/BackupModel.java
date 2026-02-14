package de.mephisto.vpin.ui.backups;

import de.mephisto.vpin.restclient.backups.BackupDescriptorRepresentation;
import de.mephisto.vpin.ui.tables.panels.BaseLoadingModel;

public class BackupModel extends BaseLoadingModel<BackupDescriptorRepresentation, BackupModel> {

  public BackupModel(BackupDescriptorRepresentation backup) {
    super(backup);
  }

  @Override
  public boolean sameBean(BackupDescriptorRepresentation object) {
    return object.equals(getBean());
  }

  @Override
  public void load() {

  }

  @Override
  public String getName() {
    return getBean().getFilename();
  }
}
