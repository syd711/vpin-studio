package de.mephisto.vpin.ui.tables;

import static de.mephisto.vpin.ui.Studio.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.mephisto.vpin.ui.util.ProgressModel;
import de.mephisto.vpin.ui.util.ProgressResultModel;

public class ClearCacheProgressModel extends ProgressModel<String> {
  private final static Logger LOG = LoggerFactory.getLogger(ClearCacheProgressModel.class);

  private final static String[] ALL_CACHES = {
    "Mania",
    "Hooks",
    "NvRams",
    "PinVol",
    "Backglass",
    "Discord",
    "Image",
    "GamesCache",
    "Games",
    "System",
    "Mame",
    "PupPack",
    "Dmd",
    "Frontend",
    "Emulator"
  };
  private final static String[] RELOADGAMES_CACHES_WITH_MAME = {
    "PinVol", "Frontend", "Games", "Mame", "Dmd", "System"
  };
  private final static String[] RELOADGAMES_CACHES = {
    "PinVol", "Frontend", "Games", "Dmd", "System"
  };


  private String[] caches;
  private int index;

  private ClearCacheProgressModel(String title, String[] caches) {
    super(title);
    this.caches = caches;
    this.index = 0;
  }

  public static ClearCacheProgressModel getFullClearCacheModel() {
    return new ClearCacheProgressModel("Clearing Caches", ALL_CACHES);
  }

  public static ClearCacheProgressModel getReloadGamesClearCacheModel(boolean invalidateMame) {
    return new ClearCacheProgressModel("Reloading Games", invalidateMame ? RELOADGAMES_CACHES_WITH_MAME : RELOADGAMES_CACHES);
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
    return false;
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
    try {
      long startTime = System.currentTimeMillis();
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
        case "Games":
          client.getGameService().clearCache();
          break;
        case "GamesCache":
          client.getGameService().reload();
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
      
      // some task are so fast, that we don't even see the counter...
      if (System.currentTimeMillis() - startTime < 200) {
        Thread.sleep(200);
      }
    }
    catch (Exception e) {
      LOG.error("Error invalidating {} cache", cache, e);
    }
  }
}
