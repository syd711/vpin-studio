package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.server.vpinmame.VPinMameService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VPinMAMEComponentTest {

  @Mock
  private VPinMameService vPinMameService;

  @InjectMocks
  private VPinMAMEComponent component;

  @Test
  void getDiffList_containsSetupExeAndDll() {
    String[] diffList = component.getDiffList();

    assertNotNull(diffList);
    assertTrue(diffList.length > 0);
    assertTrue(java.util.Arrays.asList(diffList).contains("Setup64.exe"));
  }

  @Test
  void getReleasesUrl_containsPinmame() {
    String url = component.getReleasesUrl();

    assertNotNull(url);
    assertTrue(url.contains("pinmame"));
  }

  @Test
  void getTargetFolder_delegatesToVPinMameService() {
    File mameFolder = new File("C:/mame");
    when(vPinMameService.getMameFolder()).thenReturn(mameFolder);

    assertSame(mameFolder, component.getTargetFolder());
  }

  @Test
  void getModificationDate_returnsNull_whenSetupExesDoNotExist() {
    File tempDir = new File(System.getProperty("java.io.tmpdir"), "vpinmame-test-" + System.nanoTime());
    when(vPinMameService.getMameFolder()).thenReturn(tempDir);

    assertNull(component.getModificationDate());
  }

  @Test
  void getExcludedFilenames_containsVpmAlias() {
    var excluded = component.getExcludedFilenames();

    assertNotNull(excluded);
    assertTrue(excluded.contains("VPMAlias.txt"));
  }

  @Test
  void getRootFolderInArchiveIndicators_containsVpmAlias() {
    var indicators = component.getRootFolderInArchiveIndicators();

    assertNotNull(indicators);
    assertTrue(indicators.contains("VPMAlias.txt"));
  }
}
