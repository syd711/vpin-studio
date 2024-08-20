package de.mephisto.vpin.ui.tables.vps;

import static de.mephisto.vpin.ui.Studio.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.vps.VpsInstallLink;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.TablesSidebarController;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;

public class VpsInstallerUtils {
  private final static Logger LOG = LoggerFactory.getLogger(VpsInstallerUtils.class);

  public static void installTable(@Nullable TablesSidebarController tablesController, @Nullable GameRepresentation game, 
    String link, String tableId, String versionId, String version) {
    if (installOrBrowse(tablesController, game, link, VpsDiffTypes.tableNewVersionVPX)) {
      if (game != null) {
        // If table has been installed, auto link it to VPS entry and force fix version
        try {
          client.getFrontendService().vpsLink(game.getId(), tableId, versionId);
          client.getFrontendService().fixVersion(game.getId(), version);
        }
        catch (Exception e) {
          LOG.error("Cannot link table to VPS or fix version, it has to be done manually : " + e.getMessage());
        }
      }
    }
  }

  public static boolean installOrBrowse(@Nullable TablesSidebarController controller, @Nullable GameRepresentation game, 
    String link, VpsDiffTypes type) {

    // no controller to install assets, turns link in readonly and follow the link
    if (controller != null) {

      ProgressResultModel resultModel = ProgressDialog.createProgressDialog(new VpsInstallerProgressModel(link));
      List<VpsInstallLink> installLinks = resultModel.getTypedResults();

      if (installLinks.isEmpty()
      ) {
        LOG.info("no link to install or repository not supported or error while getting them, follow the link");
      }
      else {
        AssetType assetType = vpsDiffTypeToAssetType(type);
        if (assetType == null) {
          LOG.info("Asset type '{}'' cannot be installed automatically, follow the link", type.toString());
        }
        else if (installLinks.size() == 1) {
          // only one link to install, run the installer
          installFile(controller, game, link, installLinks.get(0), assetType);
          return true;
        }
        else {
          //FIXME add a chooser of one link among the ones we got from the repository
          // ex : https://vpuniverse.com/files/file/9442-vikings-static-wheel/ with two wheels
          // openChooseDialog(controller game, links);

          // meanwile follow the link...
        }
      }
    }
    // fallback, simply follow the link
    Studio.browse(link);
    return false;
  }

  private static void installFile(TablesSidebarController controller, GameRepresentation game, String link, VpsInstallLink installFile, AssetType assetType) {
    Path tmp = null;
    try {
      tmp = Path.of(System.getProperty("java.io.tmpdir"), installFile.getName() + VpsInstallLink.VPS_INSTALL_LINK_PREFIX);
      Files.write(tmp, (link + "@" + installFile.getOrder()).getBytes());
      UploadAnalysisDispatcher.dispatchFile(controller, tmp.toFile(), game, assetType);
    }
    catch (IOException ioe) {
      LOG.info("Cannot save link in File " + tmp);          
    }
    finally {
      // clean file when installed
      if (tmp != null) {
        try {
          Files.delete(tmp);
        }
        catch (IOException ioe) {
          LOG.info("Cannot delete temp File " + tmp);          
        }
      }
    }
  }

  private static AssetType vpsDiffTypeToAssetType(VpsDiffTypes type) {
    switch(type) {
      case altColor : return AssetType.ALT_COLOR;
      case altSound : return AssetType.ALT_SOUND;
      case b2s : return AssetType.DIRECTB2S;
      case pov : return AssetType.POV;
      case rom : return AssetType.ROM;
      case sound : return AssetType.MUSIC;
      case pupPack : return AssetType.PUP_PACK;
      case tableNewVPX : return AssetType.VPX;
      case tableNewVersionVPX : return AssetType.VPX;
      //case wheel : return AssetType.?;
      //case topper : return AssetType.?;
      //case tutorial : return AssetType.?;
      //case feature : return AssetType.?;
      default : return null;
    }
  }
}
