package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.server.dof.DOFService;
import de.mephisto.vpin.server.frontend.FrontendService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DOFComponentTest {

  @Mock
  private DOFService dofService;

  @Mock
  private FrontendService frontendService;

  @InjectMocks
  private DOFComponent component;

  @Test
  void getDiffList_containsDll() {
    assertArrayEquals(new String[]{".dll"}, component.getDiffList());
  }

  @Test
  void getReleasesUrl_containsDirectOutput() {
    String url = component.getReleasesUrl();

    assertNotNull(url);
    assertTrue(url.contains("DirectOutput"));
  }

  @Test
  void getTargetFolder_delegatesToDofService() {
    File installFolder = new File("C:/dof");
    when(dofService.getInstallationFolder()).thenReturn(installFolder);

    assertSame(installFolder, component.getTargetFolder());
  }

  @Test
  void getModificationDate_returnsNull_whenInstallationFolderIsNull() {
    when(dofService.getInstallationFolder()).thenReturn(null);

    assertNull(component.getModificationDate());
  }

  @Test
  void getModificationDate_returnsNull_whenTableMappingsXmlDoesNotExist() {
    File tempDir = new File(System.getProperty("java.io.tmpdir"), "dof-test-" + System.nanoTime());
    when(dofService.getInstallationFolder()).thenReturn(tempDir);

    assertNull(component.getModificationDate());
  }

  @Test
  void isInstalled_delegatesToDofService() {
    when(dofService.isValid()).thenReturn(true);
    assertTrue(component.isInstalled());

    when(dofService.isValid()).thenReturn(false);
    assertFalse(component.isInstalled());
  }

  @Test
  void getExcludedFilenames_isNotEmpty() {
    assertFalse(component.getExcludedFilenames().isEmpty());
  }

  @Test
  void getRootFolderInArchiveIndicators_containsDirectOutputDll() {
    var indicators = component.getRootFolderInArchiveIndicators();

    assertNotNull(indicators);
    assertTrue(indicators.contains("DirectOutput.dll"));
  }

  @Test
  void preProcess_killsFrontend() {
    component.preProcess(null, null);

    verify(frontendService).killFrontend();
  }
}
