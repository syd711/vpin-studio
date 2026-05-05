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
public class SerumComponentTest {

  @Mock
  private VPinMameService vPinMameService;

  @InjectMocks
  private SerumComponent component;

  @Test
  void getDiffList_containsDll() {
    assertArrayEquals(new String[]{".dll"}, component.getDiffList());
  }

  @Test
  void getReleasesUrl_containsLibserum() {
    String url = component.getReleasesUrl();

    assertNotNull(url);
    assertTrue(url.contains("libserum"));
  }

  @Test
  void getTargetFolder_delegatesToVPinMameService() {
    File mameFolder = new File("C:/mame");
    when(vPinMameService.getMameFolder()).thenReturn(mameFolder);

    assertSame(mameFolder, component.getTargetFolder());
  }

  @Test
  void getModificationDate_returnsNull_whenTestExeDoesNotExist() {
    File tempDir = new File(System.getProperty("java.io.tmpdir"), "serum-test-" + System.nanoTime());
    when(vPinMameService.getMameFolder()).thenReturn(tempDir);

    assertNull(component.getModificationDate());
  }

  @Test
  void getExcludedFilenames_returnsEmptyList() {
    assertTrue(component.getExcludedFilenames().isEmpty());
  }

  @Test
  void getRootFolderInArchiveIndicators_containsSerumDll() {
    var indicators = component.getRootFolderInArchiveIndicators();

    assertNotNull(indicators);
    assertTrue(indicators.contains("serum64.dll"));
  }
}
