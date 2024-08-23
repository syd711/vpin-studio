package de.mephisto.vpin.ui.tables.vps;

import de.mephisto.vpin.commons.utils.FXResizeHelper;
import de.mephisto.vpin.ui.tables.vps.dialogs.VpsAssetInstallerController;
import de.mephisto.vpin.ui.util.Dialogs;
import javafx.stage.Stage;

public class VpsDialogs {

  public static boolean openVpsAssetInstaller(String link) {
    Stage stage = Dialogs.createStudioDialogStage(VpsAssetInstallerController.class, "dialog-vps-asset-installer.fxml", "Asset Installer", "assetInstaller");
    VpsAssetInstallerController controller = (VpsAssetInstallerController) stage.getUserData();
    controller.setData(link);

    FXResizeHelper fxResizeHelper = new FXResizeHelper(stage, 30, 6);
    stage.setUserData(fxResizeHelper);
    stage.setMinWidth(600);
    stage.setMinHeight(500);

    stage.showAndWait();

    return true;
  }

}
