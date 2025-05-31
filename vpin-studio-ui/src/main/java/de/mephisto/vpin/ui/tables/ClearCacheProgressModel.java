package de.mephisto.vpin.ui.tables;

import static de.mephisto.vpin.ui.Studio.client;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;

public class ClearCacheProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(ClearCacheProgressModel.class);

  private static String[] caches = {
    "Mania",
    "Hooks",
    "NvRams",
    "PinVol",
    "Backglass",
    "Discord",
    "Image",
    "Game",
    "System",
    "Mame",
    "PupPack",
    "Dmd",
    "Frontend",
    "Emulator"
  };

  private int index;

  public ClearCacheProgressModel() {
    super("Clearing Caches");
    this.index = 0;
  }

  @Override
  public boolean isShowSummary() {
    return false;
  }

  @Override
  public int getMax() {
    return caches.length;
  }

  @Override
  public boolean isIndeterminate() {
    return true;
  }

  @Override
  public boolean hasNext() {
    return index < caches.length;
  }

  @Override
  public String getNext() {
    return caches[index];
  }

  @Override
  public String nextToString(String msg) {
    return "Clearing " + msg + " Cache";
  }

  @Override
  public void processNext(ProgressResultModel progressResultModel, String cache) {
    switch(cache) {
      case "Mania":
        client.getManiaService().clearCache();
        break;
      case "Hooks":
        client.getHooksService().clearCache();
        break;
      case "NVRams":
        client.getNvRamsService().clearCache();
        break;
      case "PinVol":
        client.getPinVolService().clearCache();
        break;
      case "backglass":
        client.getBackglassServiceClient().clearCache();
        break;
      case "Discord":
        client.getDiscordService().clearCache();
        break;
      case "Image":
        client.getImageCache().clearCache();
        break;
      case "Game":
        client.getGameService().clearCache();
        break;
      case "System":
        client.getSystemService().clearCache();
        break;
      case "Mame":
        client.getMameService().clearCache();
        break;
      case "PupPack":
        client.getPupPackService().clearCache();
        break;
      case "Dmd":
        client.getDmdService().clearCache();
        break;
      case "Frontend":
        client.getFrontendService().clearCache();
        break;
      case "Emulator":
        client.getEmulatorService().clearCache();
        break;
    }
    index++;
  }
}
