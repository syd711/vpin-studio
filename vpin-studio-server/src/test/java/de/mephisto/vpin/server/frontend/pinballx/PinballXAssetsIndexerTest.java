package de.mephisto.vpin.server.frontend.pinballx;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PinballXAssetsIndexerTest {

  private PinballXAssetsIndexer indexer;

  @BeforeEach
  void setUp() {
    indexer = new PinballXAssetsIndexer();
  }

  // ---- buildIndex ----

  @Test
  void buildIndex_emptyFtp_returnsEmptyIndex() throws IOException {
    FTPClient ftp = mock(FTPClient.class);
    when(ftp.listFiles("/root/Media")).thenReturn(new FTPFile[0]);

    PinballXIndex index = indexer.buildIndex(ftp, "/root", false);

    assertThat(index.size()).isEqualTo(0);
  }

  @Test
  void buildIndex_visualPinballWheelAsset_indexesCorrectly() throws IOException {
    FTPClient ftp = mock(FTPClient.class);

    FTPFile emulatorDir = ftpDir("Visual Pinball");
    FTPFile screenDir = ftpDir("Wheel");
    FTPFile assetFile = ftpFile("mytable.png");

    when(ftp.listFiles("/root/Media")).thenReturn(new FTPFile[]{emulatorDir});
    when(ftp.listFiles("/root/Media/Visual Pinball")).thenReturn(new FTPFile[]{screenDir});
    when(ftp.listFiles("/root/Media/Visual Pinball/Wheel")).thenReturn(new FTPFile[]{assetFile});

    PinballXIndex index = indexer.buildIndex(ftp, "/root", false);

    assertThat(index.size()).isEqualTo(1);
    Optional<de.mephisto.vpin.connectors.assets.TableAsset> asset =
        index.get(EmulatorType.VisualPinball, VPinScreen.Wheel, "/Media/Visual Pinball/Wheel", "mytable.png");
    assertThat(asset).isPresent();
    assertThat(asset.get().getName()).isEqualTo("mytable.png");
  }

  @Test
  void buildIndex_futurePinballBackglass_indexesCorrectly() throws IOException {
    FTPClient ftp = mock(FTPClient.class);

    FTPFile emulatorDir = ftpDir("Future Pinball");
    FTPFile screenDir = ftpDir("Backglass");
    FTPFile assetFile = ftpFile("backglass.png");

    when(ftp.listFiles("/root/Media")).thenReturn(new FTPFile[]{emulatorDir});
    when(ftp.listFiles("/root/Media/Future Pinball")).thenReturn(new FTPFile[]{screenDir});
    when(ftp.listFiles("/root/Media/Future Pinball/Backglass")).thenReturn(new FTPFile[]{assetFile});

    PinballXIndex index = indexer.buildIndex(ftp, "/root", false);

    assertThat(index.size()).isEqualTo(1);
    Optional<de.mephisto.vpin.connectors.assets.TableAsset> asset =
        index.get(EmulatorType.FuturePinball, VPinScreen.BackGlass, "/Media/Future Pinball/Backglass", "backglass.png");
    assertThat(asset).isPresent();
  }

  @Test
  void buildIndex_visualPinballTopper_indexesCorrectly() throws IOException {
    FTPClient ftp = mock(FTPClient.class);

    FTPFile emulatorDir = ftpDir("Visual Pinball");
    FTPFile screenDir = ftpDir("Topper");
    FTPFile assetFile = ftpFile("topper.mp4");

    when(ftp.listFiles("/root/Media")).thenReturn(new FTPFile[]{emulatorDir});
    when(ftp.listFiles("/root/Media/Visual Pinball")).thenReturn(new FTPFile[]{screenDir});
    when(ftp.listFiles("/root/Media/Visual Pinball/Topper")).thenReturn(new FTPFile[]{assetFile});

    PinballXIndex index = indexer.buildIndex(ftp, "/root", false);

    assertThat(index.size()).isEqualTo(1);
    Optional<de.mephisto.vpin.connectors.assets.TableAsset> asset =
        index.get(EmulatorType.VisualPinball, VPinScreen.Topper, "/Media/Visual Pinball/Topper", "topper.mp4");
    assertThat(asset).isPresent();
  }

  @Test
  void buildIndex_unknownEmulatorType_notIndexed() throws IOException {
    FTPClient ftp = mock(FTPClient.class);

    // MAME is in the EMULATORS list but not mapped to an EmulatorType, so no recursion occurs
    FTPFile unknownDir = ftpDir("MAME");
    when(ftp.listFiles("/root/Media")).thenReturn(new FTPFile[]{unknownDir});

    PinballXIndex index = indexer.buildIndex(ftp, "/root", false);

    assertThat(index.size()).isEqualTo(0);
  }

  @Test
  void buildIndex_fullMode_includesOtherUploads() throws IOException {
    FTPClient ftp = mock(FTPClient.class);

    when(ftp.listFiles("/root/Media")).thenReturn(new FTPFile[0]);

    FTPFile uploaderDir = ftpDir("myauthor");
    FTPFile emulatorDir = ftpDir("Visual Pinball");
    FTPFile screenDir = ftpDir("Wheel");
    FTPFile assetFile = ftpFile("upload.png");

    when(ftp.listFiles("/root/Other Uploads")).thenReturn(new FTPFile[]{uploaderDir});
    when(ftp.listFiles("/root/Other Uploads/myauthor")).thenReturn(new FTPFile[]{emulatorDir});
    when(ftp.listFiles("/root/Other Uploads/myauthor/Visual Pinball")).thenReturn(new FTPFile[]{screenDir});
    when(ftp.listFiles("/root/Other Uploads/myauthor/Visual Pinball/Wheel")).thenReturn(new FTPFile[]{assetFile});

    PinballXIndex index = indexer.buildIndex(ftp, "/root", true);

    assertThat(index.size()).isEqualTo(1);
  }

  @Test
  void buildIndex_multipleAssets_allIndexed() throws IOException {
    FTPClient ftp = mock(FTPClient.class);

    FTPFile emulatorDir = ftpDir("Visual Pinball");
    FTPFile backglassScreenDir = ftpDir("Backglass");
    FTPFile wheelScreenDir = ftpDir("Wheel");
    FTPFile backglassFile1 = ftpFile("bg1.png");
    FTPFile backglassFile2 = ftpFile("bg2.png");
    FTPFile wheelFile = ftpFile("wheel.png");

    when(ftp.listFiles("/root/Media")).thenReturn(new FTPFile[]{emulatorDir});
    when(ftp.listFiles("/root/Media/Visual Pinball")).thenReturn(new FTPFile[]{backglassScreenDir, wheelScreenDir});
    when(ftp.listFiles("/root/Media/Visual Pinball/Backglass")).thenReturn(new FTPFile[]{backglassFile1, backglassFile2});
    when(ftp.listFiles("/root/Media/Visual Pinball/Wheel")).thenReturn(new FTPFile[]{wheelFile});

    PinballXIndex index = indexer.buildIndex(ftp, "/root", false);

    assertThat(index.size()).isEqualTo(3);
  }

  // ---- helpers ----

  private static FTPFile ftpDir(String name) {
    FTPFile f = mock(FTPFile.class);
    when(f.isDirectory()).thenReturn(true);
    when(f.getName()).thenReturn(name);
    return f;
  }

  private static FTPFile ftpFile(String name) {
    FTPFile f = mock(FTPFile.class);
    when(f.isDirectory()).thenReturn(false);
    when(f.isFile()).thenReturn(true);
    when(f.getName()).thenReturn(name);
    return f;
  }
}
