package de.mephisto.vpin.ui.tables.dialogs;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.ui.util.UploadProgressModel;

public class CfgUploadController extends BaseUploadController {

  public CfgUploadController() {
    super(AssetType.CFG, true, true, "cfg", "zip", "7z", "rar");
  }

  @Override
  protected UploadProgressModel createUploadModel() {
    return new CfgUploadProgressModel("Config File Upload", getSelections(), getSelectedEmulatorId());
  }
}
