package de.mephisto.vpin.server.frontend.pinballx;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.Test;

import de.mephisto.vpin.connectors.assets.TableAsset;

public class PinballXAssetsAdapterTest {

  PinballXAssetsAdapter createAdapter() {
    PinballXAssetsAdapter adapter = new PinballXAssetsAdapter();
    adapter.configure("ftp.gameex.com", 21, "leprinco@yahoo.fr", "Oliver01", "/-PinballX-");
    return adapter;
  }

  @Test
  public void testSearch() throws Exception {
    PinballXAssetsAdapter adapter = createAdapter();
    List<TableAsset> assets;
    
    assets = adapter.search("Visual Pinball", "PlayField", "250Cc");
    assertEquals(3, assets.size());
    assertEquals("250cc (Inder) (1992) (JPSalas) (1.1.0).f4v", assets.get(0).getName());
    
    for (TableAsset res: assets) {
      System.out.println(res.getUrl() + "/" + res.getName());
    }
  
    //assets = adapter.search("Visual Pinball", "Wheel", "Attack from Mars");
    //assertEquals(37, assets.size());

    assets = adapter.search("Visual Pinball", "Other2", "250Cc");
    assertEquals(0, assets.size());
  }

  @Test
  public void testDownload() throws Exception {
    PinballXAssetsAdapter adapter = createAdapter();

    TableAsset asset = new TableAsset();
    asset.setUrl("/-PinballX-/Media/Visual Pinball/Table Videos");
    asset.setName("250cc (Inder) (1992) (JPSalas) (1.1.0).f4v");
    asset.setScreen("Playfield");
    
    Path tempPath = Files.createTempFile("test", "temp"); 
    try {
      File temp = tempPath.toFile();
      adapter.download(asset, temp);

      assertTrue(temp.exists());
      assertEquals(11634196, Files.size(tempPath));
    } 
    finally {
      Files.deleteIfExists(tempPath);
    };
  }


}
