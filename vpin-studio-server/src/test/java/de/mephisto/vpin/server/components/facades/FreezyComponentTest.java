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
public class FreezyComponentTest {

  @Mock
  private VPinMameService vPinMameService;

  @InjectMocks
  private FreezyComponent component;

  @Test
  void getDiffList_containsDll() {
    assertArrayEquals(new String[]{".dll"}, component.getDiffList());
  }

  @Test
  void getReleasesUrl_containsDmdExtensions() {
    String url = component.getReleasesUrl();

    assertNotNull(url);
    assertTrue(url.contains("dmd-extensions"));
  }

  @Test
  void getTargetFolder_delegatesToVPinMameService() {
    File mameFolder = new File("C:/mame");
    when(vPinMameService.getMameFolder()).thenReturn(mameFolder);

    assertSame(mameFolder, component.getTargetFolder());
  }

  @Test
  void getModificationDate_returnsNull_whenNeitherDllExists() {
    File tempDir = new File(System.getProperty("java.io.tmpdir"), "freezy-test-" + System.nanoTime());
    when(vPinMameService.getMameFolder()).thenReturn(tempDir);

    assertNull(component.getModificationDate());
  }

  @Test
  void getExcludedFilenames_containsIniEntry() {
    var excluded = component.getExcludedFilenames();

    assertNotNull(excluded);
    assertTrue(excluded.contains("DmdDevice.ini"));
  }

  @Test
  void getRootFolderInArchiveIndicators_containsDmdDeviceIni() {
    var indicators = component.getRootFolderInArchiveIndicators();

    assertNotNull(indicators);
    assertTrue(indicators.contains("DmdDevice.ini"));
  }
}
