package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.ui.util.UploadProgressModel;

public class NvRamUploadController extends BaseUploadController {

  public NvRamUploadController() {
    super(AssetType.NV, true, true, "nv");
  }

  @Override
  protected UploadProgressModel createUploadModel() {
    return new NvRamUploadProgressModel("NvRAM Upload", getSelections(), getSelectedEmulatorId());
  }
}
