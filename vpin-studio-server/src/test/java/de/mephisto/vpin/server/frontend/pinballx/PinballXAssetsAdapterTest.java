package de.mephisto.vpin.server.frontend.pinballx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

import de.mephisto.vpin.connectors.assets.TableAsset;

public class PinballXAssetsAdapterTest {

  PinballXAssetsAdapter createAdapter() {
    PinballXAssetsAdapter adapter = new PinballXAssetsAdapter();
    adapter.configure("ftp.gameex.com", 21, "/-PinballX-/Media");
    adapter.configureCredentials("xxx", "xxx");
    return adapter;
  }

  @Test
  public void testSearch() throws Exception {
    PinballXAssetsAdapter adapter = createAdapter();
    List<TableAsset> assets;
    
    assets = adapter.search("Visual Pinball", "PlayField", null, "250Cc");
    assertEquals(2, assets.size());
    assertEquals("250cc (Inder 1992).png", assets.get(0).getName());
    assertEquals("250cc (Inder) (1992) (JPSalas) (1.1.0).f4v", assets.get(1).getName());
    //doPrintAssets(assets);
    
    //System.out.println("----------------------------------");
    assets = adapter.search("Visual Pinball", "Wheel", null, "Attack from Mars");
    assertEquals(15, assets.size());
    //doPrintAssets(assets);

    //System.out.println("----------------------------------");
    assets = adapter.search("Visual Pinball", "Loading", null, "air");
    assertEquals(12, assets.size());
    //doPrintAssets(assets);

    //System.out.println("----------------------------------");
    assets = adapter.search("Visual Pinball", "Other2", null, "250Cc");
    assertEquals(0, assets.size());
    // useless but suppress warning...
    doPrintAssets(assets);
  }

  private void doPrintAssets(List<TableAsset> assets) {
    for (TableAsset res: assets) {
      System.out.println(res.getUrl() + "/" + res.getName());
    }
  }

//  @Test
//  public void testDownload() throws Exception {
//    PinballXAssetsAdapter adapter = createAdapter();
//
//    String url = "/-PinballX-/Media/Visual Pinball/Table Videos/250cc (Inder) (1992) (JPSalas) (1.1.0).f4v";
//
//    Path tempPath = Files.createTempFile("test", "temp");
//    File temp = tempPath.toFile();
//    try (FileOutputStream fout = new FileOutputStream(temp)) {
//      adapter.readAsset(url);
//
//      assertTrue(temp.exists());
//      assertEquals(11634196, Files.size(tempPath));
//    }
//    Files.deleteIfExists(tempPath);
//  }


}
