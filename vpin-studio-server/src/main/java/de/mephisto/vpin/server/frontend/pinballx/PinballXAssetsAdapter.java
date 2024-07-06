package de.mephisto.vpin.server.frontend.pinballx;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.io.CopyStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import edu.umd.cs.findbugs.annotations.NonNull;

@Service
public class PinballXAssetsAdapter extends PinballXFtpClient implements TableAssetsAdapter {
  private final static Logger LOG = LoggerFactory.getLogger(PinballXAssetsAdapter.class);

  // for exclusive search : if searching for an emulators, will exclude all folders below except the one searched
  private static final String[] EMULATORS = clean(
    "Android", "Arcooda Pinball Arcade", "BigScor", "BingoGameRoom", "Future Pinball", "MAME", "MFME",
    "Pinball Arcade", "Pinball FX", "FX2", "FX3", "Pro Pinball", "Visual Pinball", "Zaccaria"
  );
  private static final String[] SCREENS = clean(
    "Backglass Image", "Backglass Video", "Company Logo", "Database", "Default Image", "Default Video", 
    "DMD Color Video", "DMD Image", "DMD Video", "FullDMD Video", "Flyer Image", "Gameplay Video", "INI", "Font", 
    "Instruction Card", "Launch Audio", "Loading Image", "Loading Video", "Promo Video", "POV", "Real DMD Color Image", 
    "Real DMD Color Video", "Real DMD Image", "Real DMD Video", "script", "Startup Image", "Startup Sound", 
    "Startup Video", "System Logo", "System Overlay", "System Underlay", "Tutorial Video", "Table Audio", 
    "Table Image", "Table Video", "Topper Image", "Topper Video", "Wheel"
  );

  @Override
  public List<TableAsset> search(@NonNull String emulatorType, @NonNull String screenSegment, @NonNull String term) throws Exception {
    if (term.length() < 3) {
        return Collections.emptyList();
    }

    LOG.info("Searching term '" + term + "'' for screen  " + screenSegment);

    VPinScreen screen = VPinScreen.valueOfSegment(screenSegment);
    String[] screenToFolders = screen!=null? fromScreenToFolders(screen): null;
    if (screen==null || screenToFolders==null) {
        return Collections.emptyList();
    }
  
    List<TableAsset> results = new ArrayList<>();

    long start = System.currentTimeMillis();
    FTPClient ftp = null;
    try {
      ftp = open();
      ftp.setDataTimeout(Duration.ofMillis(15000));
      ftp.setDefaultTimeout(10000);

      String[] folders = clean(screenToFolders);
      String emulator = clean(emulatorType);
      FTPFileFilter filter = new FTPFileFilter() {
        @Override
        public boolean accept(FTPFile ftpFile) {
          String name = clean(ftpFile.getName());
          // returns all folders that are for the desired screen, for recursive scan
          if (ftpFile.isDirectory()) {
            return !isForAnotherEmulator(name, emulator) && !isForAnotherScreen(name, folders);
          }
          // and all files matching the term
          if (ftpFile.isFile() && StringUtils.containsIgnoreCase(ftpFile.getName(), term)) {
            return true;
          }
          // else
          return false;
        }
      };
  
      searchRecursive(results, ftp, "", filter, isScreenEmulatorIndependent(screen), emulator, false, folders);
      LOG.info("PinballX search finished, took " + (System.currentTimeMillis() - start) + "ms.");
      for (TableAsset asset: results) {
        asset.setSourceId("PinballX");
        asset.setAuthor("gameex");
        asset.setEmulator(emulatorType);
        asset.setScreen(screen.name());
      }  

    }
    catch (Exception e) {
      LOG.error("Error during search for term " + term + " for screen " + screen + ": " + e.getMessage());
    }
    finally {
      close(ftp);
    }
    LOG.info("Search for '" + term + "'' returns " + results.size() + " results");
    return results;
  }

  /**
   * A recursive method that crawl folders tree
   * @param results Fill a list of Assets as it meets them
   * @param ftp The FTP client
   * @param folder the current folder that is crawled
   * @param filter The Filter on FTPFiles to apply
   * @param isForEmulator Whether a folder matching the emulator has been encountered
   * @param emulator The cleaned name of the emulator to gather
   * @param isForScreen Whether a folder matching the screen has been encountered
   * @param screen The cleaned name of the screen to gather
   * @throws IOException if error
   */
  private void searchRecursive(List<TableAsset> results, FTPClient ftp, String folder, FTPFileFilter filter,
        boolean isForEmulator, String emulator, boolean isForScreen, String[] screens) throws IOException {
    FTPFile[] files = ftp.listFiles(rootfolder + "/" + folder, filter);
    for (FTPFile file : files) {
      if (file.isDirectory()) {
        LOG.info("Searching FTP dir: " + file.getName());
        String name = clean(file.getName());
        searchRecursive(results, ftp, folder + "/" + file.getName(), filter,
            isForEmulator || isForEmulator(name, emulator), emulator,
            isForScreen || isForScreen(name, screens), screens);
      } 
      else if (file.isFile() && isForEmulator && isForScreen) {
        results.add(toTableAsset(folder, file));
      }
    }
  }
  private static String clean(String name) {
    return StringUtils.remove(name, " ").toLowerCase();
  }
  private static String[] clean(String... names) {
    for (int i = 0; i<names.length; i++) {
        names[i] = clean(names[i]);
    }
    Arrays.sort(names);
    return names;
  }

  private boolean isForEmulator(String name, String emulator) {
    return StringUtils.contains(name, emulator) || StringUtils.contains(emulator, name);
  }
  private boolean isForAnotherEmulator(String name, String emulator) {
    return isAmong(EMULATORS, name) && !isForEmulator(name, emulator);
  }
  private boolean isForScreen(String name, String[] screens) {
    for (String screen : screens) {
      if (StringUtils.contains(name, screen) || StringUtils.contains(screen, name)) {
        return true;
      }
    }
    return false;
  }
  private boolean isForAnotherScreen(String name, String[] screens) {
    return isAmong(SCREENS, name) && !isForScreen(name, screens);
  }

  private boolean isAmong(String[] names, String name) {
    int low = 0;
    int high = names.length - 1;
    while (low <= high) {
      int mid = (low + high) >>> 1;
      if (StringUtils.contains(names[mid], name) || StringUtils.contains(name, names[mid])) {
        return true;
      }
      int cmp = names[mid].compareTo(name);
      if (cmp < 0) {
        low = mid + 1;
      } else if (cmp > 0) {
        high = mid - 1;
      } else {
        return true; // key found
      }
    }
    return false;
  }

  private TableAsset toTableAsset(String folder, FTPFile file) {
    String filename = file.getName();
    TableAsset asset = new TableAsset();
    String mimeType = URLConnection.guessContentTypeFromName(filename);
    if (StringUtils.endsWithIgnoreCase(filename, ".apng")) {
      mimeType = "image/png";
    } else if (StringUtils.endsWithIgnoreCase(filename, ".f4v")) {
      mimeType = "video/x-f4v";
    } 
    asset.setMimeType(mimeType);
    // double encoding needed
    String url = URLEncoder.encode(URLEncoder.encode(folder + "/" + filename, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
    asset.setUrl("/assets/d/" + url);
    asset.setName(filename);
    //asset.setSize(file.getSize());
    return asset;
  }

  //-------------------------------------

  @Override
  public void writeAsset(OutputStream outputStream, @NonNull String url) throws Exception {
    LOG.info("downlaoding " + url);
    
    FTPClient ftp = null;
    try {
      ftp = open();

      String folder = StringUtils.substringBeforeLast(url, "/");
      String name = StringUtils.substringAfterLast(url, "/");

      ftp.changeWorkingDirectory(folder);
      ftp.retrieveFile(name, outputStream); 
    }
    catch (CopyStreamException cse) {
      LOG.error("Error while downloading asset " + url + ": " + cse.getMessage());
    }
    finally {
      close(ftp);
    }
  }

  private String[] fromScreenToFolders(VPinScreen screen) {
    switch (screen) {
      case Audio: return new String[] { "Table Audio" };
      case AudioLaunch: return new String[] { "Launch Audio" };
      case Other2: return null;
      case GameInfo: return new String[] { "Flyer Image" };
      case GameHelp: return new String[] { "Instruction Card" };
      case Topper: return new String[] { "Topper Video", "Topper Image" };
      case BackGlass: return new String[] { "Backglass Video", "Backglass Image" };
      case Menu: return new String[] { "Full DMD" };
      case DMD: return new String[] { "DMD Video", "DMD Image" };
      case Loading: return new String[] { "Loading Video", "Loading Image" };
      case Wheel: return new String[] { "Wheel" };
      case PlayField: return new String[] { "Table Video", "Table Image" };
    }
    return null;
  }

  private boolean isScreenEmulatorIndependent(VPinScreen screen) {
    switch (screen) {
      case GameInfo: return true;
      case GameHelp: return true;
      case Loading: return true;
      default: return false;
    }
  }

}
