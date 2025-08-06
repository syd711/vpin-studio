package de.mephisto.vpin.server.frontend.pinballx;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.io.CopyStreamException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class PinballXAssetsIndexAdapter extends PinballXFtpClient implements TableAssetsAdapter {

  private final static Logger LOG = LoggerFactory.getLogger(PinballXAssetsIndexAdapter.class);

  private PinballXIndex index;

  @Value("${pinballX.mediaserver.refreshInterval:3}")
  private int refreshInterval;

  /**
   * the refresh timer to keep the index up-to-date
   */
  private Timer refreshTimer;

  public PinballXAssetsIndexAdapter() {
  }

  @Override
  public TableAssetSource getAssetSource() {
    TableAssetSource conf = new TableAssetSource();
    conf.setAssetSearchLabel("GameEx Assets Search for PinballX");
    conf.setAssetSearchIcon("gameex.png");
    return conf;
  }

  @Override
  public void invalidateMediaCache() {
    invalidateMediaCache(true);
  }

  public void invalidateMediaCache(boolean full) {
    FTPClient ftp = null;
    try {
      ftp = open();

      PinballXAssetsIndexer indexer = new PinballXAssetsIndexer();
      this.index = indexer.buildIndex(ftp, rootfolder, full);

      // persist the pfile
      File indexFile = getIndexFile();
      File tmp = new File(indexFile.getParentFile(), indexFile.getName() + ".tmp");
      if (tmp.exists() && !tmp.delete()) {
        LOG.error("Failed to delete existing tmp file " + indexFile.getName() + ".tmp");
      }
      getIndex().saveToFile(tmp);

      // switch files
      if (indexFile.exists() && !indexFile.delete()) {
        LOG.error("Failed to delete " + indexFile.getName());
      }
      if (!tmp.renameTo(indexFile)) {
        LOG.error("Failed to rename " + indexFile.getName());
      }
      LOG.info("Written " + indexFile.getAbsolutePath());
    }
    catch (IOException ioe) {
      LOG.error("Error while reloading index file", ioe);
    }
    finally {
      close(ftp);
    }
  }

  @Override
  public List<TableAsset> search(@NonNull String emulatorType, @NonNull String screenSegment, @NonNull String term) throws Exception {
    if (term.length() < 3) {
      return Collections.emptyList();
    }

    EmulatorType emutype = EmulatorType.valueOf(emulatorType);
    VPinScreen screen = VPinScreen.valueOfSegment(screenSegment);
    LOG.info("Searching term '" + term + "'' for emulator " + emutype + " and screen  " + screen);
    return getIndex().match(emutype, screen, term);
  }

  @Override
  public Optional<TableAsset> get(String emulatorName, String screenSegment, String folder, String name) throws Exception {
    EmulatorType emutype = EmulatorType.valueOf(emulatorName);
    VPinScreen screen = VPinScreen.valueOfSegment(screenSegment);
    return getIndex().get(emutype, screen, folder, name);
  }

  //-------------------------------------

  @Override
  public void writeAsset(@NonNull OutputStream out, @NonNull TableAsset tableAsset) throws Exception {
    FTPClient ftp = null;
    try {
      ftp = open();

      String url = tableAsset.getUrl();
      String decodeUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);
      decodeUrl = decodeUrl.substring(1);
      LOG.info("downloading " + decodeUrl);

      String folder = StringUtils.substringBeforeLast(decodeUrl, "/");
      String name = StringUtils.substringAfterLast(decodeUrl, "/");

      ftp.changeWorkingDirectory(rootfolder + folder);
      ftp.setFileType(FTP.BINARY_FILE_TYPE);

      if (ftp.retrieveFile(name, out)) {
        LOG.info("Read FTP file \"" + decodeUrl + "\", " + ftp.getReplyString());
      }
      else {
        LOG.error("FTP download incomplete \"" + decodeUrl + "\", " + ftp.getReplyString());
      }
    }
    catch (CopyStreamException cse) {
      LOG.error("Error while downloading asset: " + cse.getMessage());
    }
    finally {
      close(ftp);
    }
  }

  //-----------------------------------

  public PinballXIndex getIndex() {
    if (index == null) {
      try {
        index = new PinballXIndex();
        File indexFile = getIndexFile();
        LOG.info("Load pinballX Asset index from file : " + indexFile);
        index.loadFromFile(indexFile);
      }
      catch (IOException e) {
        LOG.error("Failed to load PinballX index file: " + e.getMessage(), e);
      }
    }
    return index;
  }

  private File getIndexFile() {
    File folder = new File("./resources");
    if (!folder.exists()) {
      folder = new File("../resources");
    }
    return new File(folder, "pinballx.index");
  }

  //-----------------------------------

  public void startRefresh() {
    if (this.refreshTimer == null && this.refreshInterval > 0) {
      this.refreshTimer = new Timer();
      Calendar now = Calendar.getInstance();
      // small delay after server restart for the initial refresh, then refresh periodically
      now.add(Calendar.MINUTE, 3);
      refreshTimer.schedule(new TimerTask() {
        @Override
        public void run() {
          invalidateMediaCache();
        }
      }, now.getTime(), refreshInterval * 24 * 60 * 60 * 1000);
    }
  }

  public void stopRefresh() {
    if (this.refreshTimer != null) {
      refreshTimer.cancel();
      this.refreshTimer = null;
    }
  }

}
