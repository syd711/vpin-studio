package de.mephisto.vpin.ui.tables.vps;

import static de.mephisto.vpin.ui.Studio.client;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

public class VpsInstallerUtils {
  private final static Logger LOG = LoggerFactory.getLogger(VpsInstallerUtils.class);

  private static Path tempFolder = null;

  public static void installOrBrowse(@Nullable TablesSidebarController controller, @Nullable GameRepresentation game, String link, VpsDiffTypes type) {
    // no controller to install assets, turns link in readonly and follow the link
    if (controller == null) {
      Studio.browse(link);
      return;
    }
    // else...

    List<VpsInstallLink> installLinks = client.getVpsService().getInstallLinks(link);

    if (installLinks.isEmpty()) {
      LOG.info("no link to install or repository not supported or error while getting them, follow the link");
      Studio.browse(link);
    }
    else {
      AssetType assetType = vpsDiffTypeToAssetType(type);
      if (assetType == null) {
        LOG.info("Asset type '{}'' cannot be installed automatically, follow the link", type.toString());
        Studio.browse(link);
      }
      else if (installLinks.size() == 1) {
        // only one link to install, run the installer
        installFile(controller, game, link, installLinks.get(0), assetType);
      }
      else {
        //FIXME add a chooser of one link among the ones we got from the repository
        // ex : https://vpuniverse.com/files/file/9442-vikings-static-wheel/ with two wheels
        // openChooseDialog(controller game, links);

        // meanwile....
        Studio.browse(link);
      }
    }
  }

  private static void installFile(TablesSidebarController controller, GameRepresentation game, String link, VpsInstallLink installFile, AssetType assetType) {
    Path tmp = null;
    try {
      tmp = getTempFolder().resolve(VpsInstallLink.VPS_INSTALL_LINK_PREFIX +  installFile.getName());
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

  private static Path getTempFolder() throws IOException {
    if (tempFolder == null) {
      tempFolder = Files.createTempDirectory("VpsInstallTmp");
      Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
          try {
            Files.delete(tempFolder);
          } 
          catch (IOException ioe) {
          }
        }
      });
    }
    return tempFolder;
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
