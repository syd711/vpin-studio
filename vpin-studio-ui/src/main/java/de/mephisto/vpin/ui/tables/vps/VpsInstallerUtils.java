package de.mephisto.vpin.ui.tables.vps;

import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.vps.VpsInstallLink;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.tables.UploadAnalysisDispatcher;
import de.mephisto.vpin.ui.util.ProgressDialog;
import de.mephisto.vpin.ui.util.ProgressResultModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class VpsInstallerUtils {
  private final static Logger LOG = LoggerFactory.getLogger(VpsInstallerUtils.class);

  public static boolean installOrBrowse(@Nullable GameRepresentation game, String link, VpsDiffTypes type) {
    ProgressResultModel resultModel = ProgressDialog.createProgressDialog(new VpsInstallerProgressModel(link));
    List<VpsInstallLink> installLinks = resultModel.getTypedResults();

    if (installLinks.isEmpty()) {
      LOG.info("no link to install or repository not supported or error while getting them, follow the link");
    }
    else {
      AssetType assetType = vpsDiffTypeToAssetType(type);
      if (assetType == null) {
        LOG.info("Asset type '{}'' cannot be installed automatically, follow the link", type.toString());
      }
      else if (installLinks.size() == 1) {
        // only one link to install, run the installer
        installFile(game, link, installLinks.get(0), assetType);
        return true;
      }
      else {
        //FIXME add a chooser of one link among the ones we got from the repository
        // ex : https://vpuniverse.com/files/file/9442-vikings-static-wheel/ with two wheels
        // openChooseDialog(controller game, links);

        // meanwile follow the link...
      }
    }
    // fallback, simply follow the link
    Studio.browse(link);
    return false;
  }

  private static void installFile(GameRepresentation game, String link, VpsInstallLink installFile, AssetType assetType) {
    String filename = VpsInstallLink.getLinkFilename(installFile.getName());
    try {
      Path tmp = Path.of(System.getProperty("java.io.tmpdir"), filename);
      Files.write(tmp, (link + "@" + installFile.getOrder()).getBytes());
      UploadAnalysisDispatcher.dispatchFile(tmp.toFile(), game, assetType, () -> {
        // clean file when installed
        if (tmp != null) {
          try {
            Files.delete(tmp);
          }
          catch (IOException ioe) {
            LOG.info("Cannot delete temp File " + tmp);
          }
        }
      });
    }
    catch (IOException ioe) {
      LOG.info("Cannot save link in File " + filename);
    }
  }

  private static AssetType vpsDiffTypeToAssetType(VpsDiffTypes type) {
    switch (type) {
      case altColor:
        return AssetType.ALT_COLOR;
      case altSound:
        return AssetType.ALT_SOUND;
      case b2s:
        return AssetType.DIRECTB2S;
      case pov:
        return AssetType.POV;
      case rom:
        return AssetType.ROM;
      case sound:
        return AssetType.MUSIC;
      case pupPack:
        return AssetType.PUP_PACK;
      case tableNewVPX:
        return AssetType.VPX;
      case tableNewVersionVPX:
        return AssetType.VPX;
      //case wheel : return AssetType.?;
      //case topper : return AssetType.?;
      //case tutorial : return AssetType.?;
      //case feature : return AssetType.?;
      default:
        return null;
    }
  }
}
