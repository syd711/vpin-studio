package de.mephisto.vpin.server.frontend.pinballx;

import de.mephisto.vpin.connectors.assets.TableAssetSourceType;
import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class PinballXIndex {

  private Map<EmulatorType, Map<VPinScreen, List<Asset>>> index = new HashMap<>();

  public void addAsset(EmulatorType emulator, VPinScreen screen, String author, String folder, String name) {
    Map<VPinScreen, List<Asset>> screenAssets = index.get(emulator);
    if (screenAssets == null) {
      screenAssets = new HashMap<>();
      index.put(emulator, screenAssets);
    }

    List<Asset> listAssets = screenAssets.get(screen);
    if (listAssets == null) {
      listAssets = new ArrayList<>();
      screenAssets.put(screen, listAssets);
    }

    Asset asset = new Asset();
    asset.folder = folder;
    asset.name = name;
    asset.author = author;
    listAssets.add(asset);
  }

  public List<TableAsset> match(EmulatorType emutype, VPinScreen screen, String term) {
    List<TableAsset> assets = new ArrayList<>();
    Map<VPinScreen, List<Asset>> screenAssets = isScreenEmulatorIndependent(screen) ? index.get(null) : index.get(emutype);
    if (screenAssets != null) {
      List<Asset> listAssets = screenAssets.get(screen);
      if (listAssets != null) {
        assets = listAssets.stream()
          .filter(t -> matchTerm(t, term))
          .map(t -> t.createAsset(emutype, screen))
          .collect(Collectors.toList());
      }
    }
    return assets;
  }

  private boolean matchTerm(Asset t, String term) {
    return StringUtils.containsIgnoreCase(t.name, term) || StringUtils.containsIgnoreCase(t.folder, term);
  }

  public Optional<TableAsset> get(EmulatorType emutype, VPinScreen screen, String folder, String name) {
    Map<VPinScreen, List<Asset>> screenAssets = isScreenEmulatorIndependent(screen) ? index.get(null) : index.get(emutype);
    if (screenAssets != null) {
      List<Asset> listAssets = screenAssets.get(screen);
      if (listAssets != null) {
        return listAssets.stream()
            .filter(t -> t.name.equalsIgnoreCase(name) && t.folder.equalsIgnoreCase(folder))
            .map(t -> t.createAsset(emutype, screen))
            .findFirst();
      }
    }
    return Optional.empty();
  }

  public static boolean isScreenEmulatorIndependent(VPinScreen screen) {
    switch (screen) {
      case GameInfo:
        return true;
      case GameHelp:
        return true;
      case Loading:
        return true;
      default:
        return false;
    }
  }

  //-----------------------------------------
  public void saveToFile(File indexFile) throws IOException {
    try (FileWriter fw = new FileWriter(indexFile, Charset.forName("UTF-8"));
         BufferedWriter w = new BufferedWriter(fw)) {

      for (EmulatorType type : index.keySet()) {
        for (VPinScreen screen : index.get(type).keySet()) {
          for (Asset asset : index.get(type).get(screen)) {
            w.write((type != null ? type : "") + "@@" + screen
                + "@@" + asset.author + "@@" + asset.folder + "@@" + asset.name + "\n");
          }
        }
      }
    }
  }

  public void loadFromFile(File indexFile) throws IOException {
    if (indexFile.exists()) {
      index.clear();

      try (FileReader fr = new FileReader(indexFile, Charset.forName("UTF-8"));
           BufferedReader r = new BufferedReader(fr)) {

        String line = null;
        int nbLines = 0;
        try {
          while ((line = r.readLine()) != null) {
            nbLines++;
            String[] parts = StringUtils.splitByWholeSeparatorPreserveAllTokens(line, "@@");

            EmulatorType type = parts[0].length() > 0 ? EmulatorType.valueOf(parts[0]) : null;
            VPinScreen screen = VPinScreen.valueOf(parts[1]);
            addAsset(type, screen, parts[2], parts[3], parts[4]);
          }
        }
        catch (Exception e) {
          System.err.println("error at line " + nbLines + ", " + e.getMessage());
        }
      }
    }
  }

  public int getNbAssets() {
    int count = 0;
    for (EmulatorType type : index.keySet()) {
      for (VPinScreen screen : index.get(type).keySet()) {
        count += index.get(type).get(screen).size();
      }
    }
    return count;
  }

  public void clear() {
    index.clear();
  }

  public int size() {
    int count = 0;
    Collection<Map<VPinScreen, List<Asset>>> values = index.values();
    for (Map<VPinScreen, List<Asset>> value : values) {
      Collection<List<Asset>> assetLists = value.values();
      for (List<Asset> assetList : assetLists) {
        count += assetList.size();
      }
    }

    return count;
  }

  //--------------------------------------------------------

  /*
   * the url in TableAsset is the folder from rootfolder, double encrypted
   */
  static class Asset {
    public String folder;
    public String name;
    public String author;


    TableAsset createAsset(EmulatorType emutype, VPinScreen screen) {
      TableAsset asset = new TableAsset();

      asset.setEmulator(emutype != null ? emutype.name() : null);
      asset.setScreen(screen.getSegment());
      asset.setPlayfieldMediaInverted(true);

      String mimeType = URLConnection.guessContentTypeFromName(name);
      if (StringUtils.endsWithIgnoreCase(name, ".apng")) {
        mimeType = "image/png";
      }
      else if (StringUtils.endsWithIgnoreCase(name, ".f4v")) {
        mimeType = "video/x-f4v";
      }
      asset.setMimeType(mimeType);

      // double encoding needed, first one here, second in client
      // url must start with / not encoded !
      String url = "/" + URLEncoder.encode(folder + "/" + name, StandardCharsets.UTF_8);
      asset.setUrl(url);
      asset.setSourceId(TableAssetSourceType.PinballX.name());
      asset.setName(name);
      asset.setAuthor(author);

      return asset;
    }
  }
}