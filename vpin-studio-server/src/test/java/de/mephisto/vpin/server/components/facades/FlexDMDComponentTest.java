package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.server.vpinmame.VPinMameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FlexDMDComponentTest {

  @Mock
  private VPinMameService vPinMameService;

  @InjectMocks
  private FlexDMDComponent component;

  @Test
  void getDiffList_containsExpectedExtensions() {
    assertArrayEquals(new String[]{".dll", ".exe"}, component.getDiffList());
  }

  @Test
  void getReleasesUrl_containsFlexDmd() {
    String url = component.getReleasesUrl();

    assertNotNull(url);
    assertTrue(url.contains("flexdmd"));
  }

  @Test
  void getTargetFolder_delegatesToVPinMameService() {
    File mameFolder = new File("C:/mame");
    when(vPinMameService.getMameFolder()).thenReturn(mameFolder);

    assertSame(mameFolder, component.getTargetFolder());
  }

  @Test
  void getModificationDate_returnsNull_whenDllDoesNotExist() {
    File mameFolder = mock(File.class);
    when(vPinMameService.getMameFolder()).thenReturn(mameFolder);

    // File constructor uses the parent — we need a real temp directory
    File tempDir = new File(System.getProperty("java.io.tmpdir"), "flexdmd-test-" + System.nanoTime());
    when(vPinMameService.getMameFolder()).thenReturn(tempDir);

    Date result = component.getModificationDate();

    assertNull(result);
  }

  @Test
  void getExcludedFilenames_containsLogConfig() {
    var excluded = component.getExcludedFilenames();

    assertNotNull(excluded);
    assertTrue(excluded.contains("FlexDMD.log.config"));
  }

  @Test
  void getRootFolderInArchiveIndicators_containsFlexDmdUiExe() {
    var indicators = component.getRootFolderInArchiveIndicators();

    assertNotNull(indicators);
    assertTrue(indicators.contains("FlexDMDUI.exe"));
  }
}
