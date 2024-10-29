package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.ui.util.UploadProgressModel;

public class CfgUploadController extends BaseUploadController {

  public CfgUploadController() {
    super("Config File", true, true, "cfg");
  }

  @Override
  protected UploadProgressModel createUploadModel() {
    return new CfgUploadProgressModel("Config File Upload", getSelections(), getSelectedEmulatorId(), finalizer);
  }
}
