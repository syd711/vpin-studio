package de.mephisto.vpin.server.frontend.pinballx;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;

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
    asset.author  =author;
    listAssets.add(asset);
  }

  public List<TableAsset> match(EmulatorType emutype, VPinScreen screen, String term) {
    List<TableAsset> assets = new ArrayList<>();
    Map<VPinScreen, List<Asset>> screenAssets = isScreenEmulatorIndependent(screen) ? index.get(null) : index.get(emutype);
    if (screenAssets != null) {
      List<Asset> listAssets = screenAssets.get(screen);
      if (listAssets != null) {
        assets = listAssets.stream().filter(t -> StringUtils.containsIgnoreCase(t.name, term)).map(t -> t.createAsset(emutype)).collect(Collectors.toList());
      }
    }
    return assets;
  }

  public static boolean isScreenEmulatorIndependent(VPinScreen screen) {
    switch (screen) {
      case GameInfo: return true;
      case GameHelp: return true;
      case Loading: return true;
      default: return false;
    }
  }

  //-----------------------------------------
  public void saveToFile(File indexFile) throws IOException {
    try (FileWriter fw = new FileWriter(indexFile, Charset.forName("UTF-8"));
        BufferedWriter w = new BufferedWriter(fw)) {

      for (EmulatorType type: index.keySet()) {
        for (VPinScreen screen: index.get(type).keySet()) {
          for (Asset asset: index.get(type).get(screen)) {
            w.write((type!=null? type : "") + "@@" + screen 
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

            EmulatorType type = parts[0].length() > 0 ? EmulatorType.valueOf(parts[0]): null;
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
    for (EmulatorType type: index.keySet()) {
      for (VPinScreen screen: index.get(type).keySet()) {
        count += index.get(type).get(screen).size();
      }
    }
    return count;
  }

  public void clear() {
    index.clear();
  }

  //--------------------------------------------------------

  static class Asset {
    public String folder;
    public String name;
    public String author;


    TableAsset createAsset(EmulatorType emutype) {
      TableAsset asset = new TableAsset();

      asset.setEmulator(emutype!=null? emutype.name(): null);

      String mimeType = URLConnection.guessContentTypeFromName(name);
      if (StringUtils.endsWithIgnoreCase(name, ".apng")) {
        mimeType = "image/png";
      } else if (StringUtils.endsWithIgnoreCase(name, ".f4v")) {
        mimeType = "video/x-f4v";
      } 
      asset.setMimeType(mimeType);

      // double encoding needed
      String url = URLEncoder.encode(URLEncoder.encode(folder + "/" + name, StandardCharsets.UTF_8), StandardCharsets.UTF_8);
      asset.setUrl("/assets/d/" + url);

      asset.setName(name);
      asset.setAuthor(author);

      return asset;
    }
  }
}