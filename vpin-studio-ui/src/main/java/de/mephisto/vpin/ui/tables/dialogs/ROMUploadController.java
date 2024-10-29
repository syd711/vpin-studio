package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.ui.util.UploadProgressModel;

public class ROMUploadController extends BaseUploadController {

  public ROMUploadController() {
    super("ROM File", true, true, "zip");
  }

  @Override
  protected UploadProgressModel createUploadModel() {
    return new RomUploadProgressModel("ROM Upload", getSelections(), getSelectedEmulatorId(), finalizer);
  }
}
