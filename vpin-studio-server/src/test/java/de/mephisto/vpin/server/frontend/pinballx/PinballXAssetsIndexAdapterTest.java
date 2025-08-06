package de.mephisto.vpin.server.frontend.pinballx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import de.mephisto.vpin.connectors.assets.TableAssetSource;
import org.apache.poi.ss.formula.functions.T;
import org.junit.Test;

import de.mephisto.vpin.connectors.assets.TableAsset;

public class PinballXAssetsIndexAdapterTest {

  PinballXAssetsIndexAdapter createAdapter() {
    PinballXAssetsIndexAdapter adapter = new PinballXAssetsIndexAdapter();
    adapter.configure("ftp.gameex.com", 21, "/-PinballX-");
    adapter.configureCredentials("####", "####");
    return adapter;
  }

  @Test
  public void testPersist() throws Exception {
    PinballXAssetsIndexAdapter adapter = createAdapter();
    // a bit quicker for test, use full=false
    adapter.invalidateMediaCache(false);

    PinballXIndex index = adapter.getIndex();
    int count = index.getNbAssets();

    File f = File.createTempFile("pinballx", "index");
    index.saveToFile(f);

    index.clear();
    assertEquals(0, index.getNbAssets());


    index.loadFromFile(f);
    assertEquals(count, index.getNbAssets());
  }

  @Test
  public void testSearch() throws Exception {
    PinballXAssetsIndexAdapter adapter = createAdapter();
    PinballXIndex index = adapter.getIndex();


    File f = new File("../testsystem/pinballx.index");
    index.loadFromFile(f);
    assertEquals(27974, index.getNbAssets());

    List<TableAsset> assets;

    assets = adapter.search("VisualPinball", "PlayField", "250Cc");
    assertEquals(2, assets.size());
    assertEquals("250cc (Inder 1992).png", assets.get(0).getName());
    assertEquals("250cc (Inder) (1992) (JPSalas) (1.1.0).f4v", assets.get(1).getName());
    //doPrintAssets(assets);

    //System.out.println("----------------------------------");
    assets = adapter.search("VisualPinball", "Wheel", "Attack from Mars");
    assertEquals(22, assets.size());
    //doPrintAssets(assets);

    //System.out.println("----------------------------------");
    assets = adapter.search("VisualPinball", "Loading", "air");
    assertEquals(12, assets.size());
    //doPrintAssets(assets);

    //System.out.println("----------------------------------");
    assets = adapter.search("VisualPinball", "GameSelect", "250Cc");
    assertEquals(0, assets.size());
    // useless but suppress warning...
    doPrintAssets(assets);
  }

  private void doPrintAssets(List<TableAsset> assets) {
    for (TableAsset res : assets) {
      System.out.println(res.getUrl() + "/" + res.getName());
    }
  }

  @Test
  public void testDownload() throws Exception {
    PinballXAssetsIndexAdapter adapter = createAdapter();

    String media = "/Media/Visual Pinball/Table Videos/250cc (Inder) (1992) (JPSalas) (1.1.0).f4v";
    String url = "/" + URLEncoder.encode(media, StandardCharsets.UTF_8);
    
    Path tempPath = Files.createTempFile("test", "temp");
    File temp = tempPath.toFile();
    try (FileOutputStream fout = new FileOutputStream(temp)) {
      TableAsset tableAsset = new TableAsset();
      tableAsset.setUrl(url);
      adapter.writeAsset(fout, tableAsset);
      assertTrue(temp.exists());
      assertEquals(11634196, Files.size(tempPath));
    }
    Files.deleteIfExists(tempPath);
  }


}
