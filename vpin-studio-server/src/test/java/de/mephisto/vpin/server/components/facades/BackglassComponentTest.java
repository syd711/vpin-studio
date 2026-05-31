package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.server.directb2s.BackglassService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BackglassComponentTest {

  @Mock
  private BackglassService backglassService;

  @InjectMocks
  private BackglassComponent component;

  @Test
  void getDiffList_containsExpectedExtensions() {
    String[] diffList = component.getDiffList();

    assertNotNull(diffList);
    assertTrue(diffList.length > 0);
  }

  @Test
  void getReleasesUrl_containsB2sBackglass() {
    String url = component.getReleasesUrl();

    assertNotNull(url);
    assertTrue(url.contains("b2s-backglass"));
  }

  @Test
  void getTargetFolder_delegatesToBackglassService() {
    File serverFolder = new File("C:/backglass");
    when(backglassService.getBackglassServerFolder()).thenReturn(serverFolder);

    assertSame(serverFolder, component.getTargetFolder());
  }

  @Test
  void getModificationDate_returnsNull_whenExeDoesNotExist() {
    File tempDir = new File(System.getProperty("java.io.tmpdir"), "backglass-test-" + System.nanoTime());
    when(backglassService.getBackglassServerFolder()).thenReturn(tempDir);

    assertNull(component.getModificationDate());
  }

  @Test
  void getExcludedFilenames_isNotEmpty() {
    assertFalse(component.getExcludedFilenames().isEmpty());
  }

  @Test
  void getRootFolderInArchiveIndicators_containsReadme() {
    var indicators = component.getRootFolderInArchiveIndicators();

    assertNotNull(indicators);
    assertTrue(indicators.contains("README.txt"));
  }
}
