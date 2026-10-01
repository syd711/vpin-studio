package de.mephisto.vpin.server.components.facades;

import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.components.Component;
import de.mephisto.vpin.server.components.ComponentRepository;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VpxComponentTest {

  @Mock
  private SystemService systemService;

  @Mock
  private ComponentRepository componentRepository;

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

    OffsetDateTime result = component.getModificationDate();

    assertNull(result);
  }

  @Test
  void getModificationDate_returnsNull_whenExeDoesNotExist() {
    File nonExistent = mock(File.class);
    when(nonExistent.exists()).thenReturn(false);
    when(systemService.resolveVpx64Exe()).thenReturn(nonExistent);

      OffsetDateTime result = component.getModificationDate();

    assertNull(result);
  }

  @Test
  void getModificationDate_returnsDate_whenExeExists() {
    File exe = mock(File.class);
    when(exe.exists()).thenReturn(true);
    when(exe.lastModified()).thenReturn(1000L);
    when(systemService.resolveVpx64Exe()).thenReturn(exe);

      OffsetDateTime result = component.getModificationDate();

    assertNotNull(result);
    assertEquals(1000L, result.toInstant().toEpochMilli());
  }

  @Test
  void getTargetFolder_prefersOverride(@TempDir Path tempDir) throws Exception {
    File overrideFolder = tempDir.resolve("MyVisualPinball").toFile();
    Files.createDirectories(overrideFolder.toPath());
    Component c = new Component();
    c.setTargetFolder(overrideFolder.getAbsolutePath());
    when(componentRepository.findByType(ComponentType.vpinball)).thenReturn(Optional.of(c));

    File result = component.getTargetFolder();

    assertEquals(overrideFolder, result);
    verifyNoInteractions(systemService);
  }

  @Test
  void getModificationDate_findsExeInOverrideFolder(@TempDir Path tempDir) throws Exception {
    File overrideFolder = tempDir.resolve("MyVisualPinball").toFile();
    Files.createDirectories(overrideFolder.toPath());
    File exe = new File(overrideFolder, "VPinballX64.exe");
    Files.writeString(exe.toPath(), "x");
    Component c = new Component();
    c.setTargetFolder(overrideFolder.getAbsolutePath());
    when(componentRepository.findByType(ComponentType.vpinball)).thenReturn(Optional.of(c));

    OffsetDateTime result = component.getModificationDate();

    assertNotNull(result);
    verifyNoInteractions(systemService);
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
