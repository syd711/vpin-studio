package de.mephisto.vpin.server.frontend.pinballx;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

@Service
public class PinballXAssetsIndexer {
  private final static Logger LOG = LoggerFactory.getLogger(PinballXAssetsIndexer.class);

  // for exclusive search : if searching for an emulators, will exclude all folders below except the one searched
  private static final String[] EMULATORS = clean(
    "Visual Pinball", "Future Pinball", "FX2", "FX3", "Pinball FX", "MAME",
    "Android", "Arcooda Pinball Arcade", "BigScor", "BingoGameRoom",
    "Pinball Arcade", "Pro Pinball", "Zaccaria"
  );

  private static final String[] SCREENS = clean(
    "Table Audio", "Table", "Wheel", "Backglass", "FullDMD", "DMD", "Topper", "Loading",
    "Launch Audio", "Instruction", "Flyer",
    "Logo", "Database", "Default", "Gameplay", "INI", "Font", "Promo", "POV", 
    "script", "Startup", "System", "Overlay", "Underlay", "Tutorial",
    "Audio", "Video", "Image" 
  );


  /**
   * Index is a list of lines with : emulator/screen/author/url
   */
  public PinballXIndex buildIndex(FTPClient ftp, String rootfolder, boolean full) throws IOException {

    long start = System.currentTimeMillis();
    
    PinballXIndex index = new PinballXIndex();

    searchRecursive(index, ftp, rootfolder, "/Media", null, null, "gameex");
    if (full) {
      FTPFile[] files = ftp.listFiles(rootfolder + "/Other Uploads");
      for (FTPFile file : files) {
        if (file.isDirectory()) {
          searchRecursive(index, ftp, rootfolder, "/Other Uploads/" + file.getName(), null, null, file.getName());
        }
      }
    }
    LOG.info("PinballX index finished, took " + (System.currentTimeMillis() - start) + "ms.");
    return index;
  }

  /**
   * A recursive method that crawl folders tree
   * @param index Where the write the results to
   * @param ftp The FTP client
   * @param folder the current folder that is crawled
   * @param emulator The cleaned name of the discovered emulator
   * @param screen The cleaned name of the screen
   * @throws IOException if error
   */
  private void searchRecursive(PinballXIndex index, FTPClient ftp, String rootfolder, String folder, 
        EmulatorType emulator, VPinScreen screen, String author) throws IOException {
    LOG.debug("GameEx Search Indexer: " + rootfolder + folder + ", collected " + index.size() + " assets.");
    FTPFile[] files = ftp.listFiles(rootfolder + folder);

    boolean hasEmulatorAndScreen = screen!=null && (emulator!=null || PinballXIndex.isScreenEmulatorIndependent(screen));

    for (FTPFile file : files) {
      if (file.isDirectory()) {
        String name = clean(file.getName());
        
        // is folder name an emulator-like folder ?
        String emulatorFromName = isAmong(EMULATORS, name);
        if (emulatorFromName != null) {
          EmulatorType emulatorType = fromFolderToEmulator(emulatorFromName);
          // continue crawling for emulators that should be indexed only
          if (emulatorType!=null) {
            searchRecursive(index, ftp, rootfolder, folder + "/" + file.getName(), emulatorType, screen, author);
          }
        }
        else {
          // is folder name a screen-like folder
          String screenFromName = isAmong(SCREENS, name);
          if (screenFromName != null) {
            VPinScreen vpinscreen = fromFolderToScreen(screenFromName);
            // continue crawling only for VpinScreens, other types of screens are ignored
            if (vpinscreen!=null) {
              searchRecursive(index, ftp, rootfolder, folder + "/" + file.getName(), emulator, vpinscreen, author);
            }
          }
          else {
            // just pass through
            searchRecursive(index, ftp, rootfolder, folder + "/" + file.getName(), emulator, screen, author);
          }
        }
      } 
      else if (hasEmulatorAndScreen && file.isFile()) {
        // index all files
        index.addAsset(emulator, screen, author, folder, file.getName());
      }
    }
  }

  //---------------

  private static String clean(String name) {
    return StringUtils.remove(name, " ").toLowerCase();
  }
  private static String[] clean(String... names) {
    for (int i = 0; i<names.length; i++) {
        names[i] = clean(names[i]);
    }
    return names;
  }

  private String isAmong(String[] names, String name) {
    for (int i = 0, n = names.length; i < n; i++) {
      if (StringUtils.contains(names[i], name) || StringUtils.contains(name, names[i])) {
        return names[i];
      }
    }
    return null;
  }

  private EmulatorType fromFolderToEmulator(String folder) {
    return 
      match(folder, "futurepinball", EmulatorType.FuturePinball,
      match(folder, "visualpinball", EmulatorType.VisualPinball,
      null));
  }

  private VPinScreen fromFolderToScreen(String folder) {
    // from most infrequent to most frequent
    return 
      match(folder, "flyer", VPinScreen.GameInfo,
      match(folder, "instruction", VPinScreen.GameHelp,
      match(folder, "launchaudio", VPinScreen.AudioLaunch,
      match(folder, "loading", VPinScreen.Loading,
      match(folder, "dmd", VPinScreen.DMD,
      match(folder, "fulldmd", VPinScreen.Menu,
      match(folder, "topper", VPinScreen.Topper,
      match(folder, "backglass", VPinScreen.BackGlass,
      match(folder, "logo", VPinScreen.Logo,
      match(folder, "wheel", VPinScreen.Wheel,
      match(folder, "table", VPinScreen.PlayField,
      match(folder, "tableaudio", VPinScreen.Audio,
      null))))))))))));
  }

  private <T> T match(String folder, String toMatch, T retIfMatched, T retIfNonNull) {
    return retIfNonNull != null? retIfNonNull: folder.equals(toMatch) ? retIfMatched: null;
  }

}
