package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.ui.util.UploadProgressModel;

public class NvRamUploadController extends BaseUploadController {

  public NvRamUploadController() {
    super("NVRam File", true, true, "nv");
  }

  @Override
  protected UploadProgressModel createUploadModel() {
    return new NvRamUploadProgressModel("NvRAM Upload", getSelections(), getSelectedEmulatorId(), finalizer);
  }
}
