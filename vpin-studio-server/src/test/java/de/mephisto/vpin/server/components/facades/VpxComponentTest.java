package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.server.system.SystemService;
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
public class VpxComponentTest {

  @Mock
  private SystemService systemService;

  @InjectMocks
  private VpxComponent component;

  @Test
  void getDiffList_containsExpectedExtensions() {
    String[] diffList = component.getDiffList();

    assertNotNull(diffList);
    assertTrue(diffList.length > 0);
    assertArrayEquals(new String[]{".vbs", ".dll", ".exe"}, diffList);
  }

  @Test
  void getReleasesUrl_isNotEmpty() {
    String url = component.getReleasesUrl();

    assertNotNull(url);
    assertFalse(url.isEmpty());
    assertTrue(url.contains("vpinball"));
  }

  @Test
  void getTargetFolder_delegatesToSystemService() {
    File folder = new File("C:/vPinball");
    when(systemService.resolveVpx64InstallFolder()).thenReturn(folder);

    File result = component.getTargetFolder();

    assertSame(folder, result);
  }

  @Test
  void getModificationDate_returnsNull_whenExeIsNull() {
    when(systemService.resolveVpx64Exe()).thenReturn(null);

    Date result = component.getModificationDate();

    assertNull(result);
  }

  @Test
  void getModificationDate_returnsNull_whenExeDoesNotExist() {
    File nonExistent = mock(File.class);
    when(nonExistent.exists()).thenReturn(false);
    when(systemService.resolveVpx64Exe()).thenReturn(nonExistent);

    Date result = component.getModificationDate();

    assertNull(result);
  }

  @Test
  void getModificationDate_returnsDate_whenExeExists() {
    File exe = mock(File.class);
    when(exe.exists()).thenReturn(true);
    when(exe.lastModified()).thenReturn(1000L);
    when(systemService.resolveVpx64Exe()).thenReturn(exe);

    Date result = component.getModificationDate();

    assertNotNull(result);
    assertEquals(1000L, result.getTime());
  }

  @Test
  void getExcludedFilenames_returnsEmptyList() {
    assertTrue(component.getExcludedFilenames().isEmpty());
  }

  @Test
  void getRootFolderInArchiveIndicators_containsExeNames() {
    var indicators = component.getRootFolderInArchiveIndicators();

    assertNotNull(indicators);
    assertTrue(indicators.contains("VPinballX64.exe"));
  }
}
