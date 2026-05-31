package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.server.doflinx.DOFLinxService;
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
public class DOFLinxComponentTest {

  @Mock
  private DOFLinxService dofLinxService;

  @InjectMocks
  private DOFLinxComponent component;

  @Test
  void getDiffList_containsExpectedExtensions() {
    assertArrayEquals(new String[]{".dll", ".exe"}, component.getDiffList());
  }

  @Test
  void getReleasesUrl_containsDOFLinx() {
    String url = component.getReleasesUrl();

    assertNotNull(url);
    assertTrue(url.contains("DOFLinx"));
  }

  @Test
  void getTargetFolder_delegatesToDofLinxService() {
    File installFolder = new File("C:/doflinx");
    when(dofLinxService.getInstallationFolder()).thenReturn(installFolder);

    assertSame(installFolder, component.getTargetFolder());
  }

  @Test
  void getModificationDate_returnsNull_whenInstallationFolderIsNull() {
    when(dofLinxService.getInstallationFolder()).thenReturn(null);

    assertNull(component.getModificationDate());
  }

  @Test
  void getModificationDate_returnsNull_whenDofLinxExeDoesNotExist() {
    File tempDir = new File(System.getProperty("java.io.tmpdir"), "doflinx-test-" + System.nanoTime());
    when(dofLinxService.getInstallationFolder()).thenReturn(tempDir);

    assertNull(component.getModificationDate());
  }

  @Test
  void isInstalled_delegatesToDofLinxService() {
    when(dofLinxService.isValid()).thenReturn(true);
    assertTrue(component.isInstalled());

    when(dofLinxService.isValid()).thenReturn(false);
    assertFalse(component.isInstalled());
  }

  @Test
  void getExcludedFilenames_containsIniAndLog() {
    var excluded = component.getExcludedFilenames();

    assertNotNull(excluded);
    assertTrue(excluded.contains(".ini"));
    assertTrue(excluded.contains(".log"));
  }

  @Test
  void getIncludedFilenames_containsSampleIniFiles() {
    var included = component.getIncludedFilenames();

    assertNotNull(included);
    assertTrue(included.contains("Sample INI files/"));
  }

  @Test
  void getRootFolderInArchiveIndicators_containsExpectedFiles() {
    var indicators = component.getRootFolderInArchiveIndicators();

    assertNotNull(indicators);
    assertTrue(indicators.contains("DOFLinx.vbs"));
    assertTrue(indicators.contains("HELP.txt"));
  }

  @Test
  void preProcess_killsDOFLinx() {
    component.preProcess(null, null);

    verify(dofLinxService).killDOFLinx();
  }

  @Test
  void postProcess_whenNotValid_doesNotThrow() {
    when(dofLinxService.isValid()).thenReturn(false);

    assertDoesNotThrow(() -> component.postProcess(null, null));
  }
}
