package de.mephisto.vpin.server.nvrams;

import de.mephisto.vpin.restclient.highscores.NVRamList;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NVRamServiceTest {

  @Mock
  private SystemService systemService;

  @InjectMocks
  private NVRamService nvRamService;

  @Test
  void getResettedNVRams_returnsEmpty_whenFolderDoesNotExist(@TempDir Path tempDir) {
    File nonExistentFolder = tempDir.resolve("missing").toFile();
    when(systemService.getResettedNVRamsFolder()).thenReturn(nonExistentFolder);

    NVRamList result = nvRamService.getResettedNVRams();

    assertNotNull(result);
    assertTrue(result.getEntries().isEmpty());
  }

  @Test
  void getResettedNVRams_listsNvFileBasenames(@TempDir Path tempDir) throws IOException {
    Path nvFolder = tempDir.resolve("nvrams");
    Files.createDirectories(nvFolder);
    Files.createFile(nvFolder.resolve("funhouse.nv"));
    Files.createFile(nvFolder.resolve("addams.nv"));
    Files.createFile(nvFolder.resolve("readme.txt"));

    when(systemService.getResettedNVRamsFolder()).thenReturn(nvFolder.toFile());

    NVRamList result = nvRamService.getResettedNVRams();

    assertEquals(2, result.getEntries().size());
    assertTrue(result.getEntries().contains("funhouse"));
    assertTrue(result.getEntries().contains("addams"));
    assertFalse(result.getEntries().contains("readme"));
  }

  @Test
  void copyResettedNvRam_returnsTrue_whenResettedFileDoesNotExist(@TempDir Path tempDir) throws IOException {
    Path nvFolder = tempDir.resolve("nvrams");
    Files.createDirectories(nvFolder);
    when(systemService.getResettedNVRamsFolder()).thenReturn(nvFolder.toFile());

    File targetFile = tempDir.resolve("funhouse.nv").toFile();

    boolean result = nvRamService.copyResettedNvRam(targetFile);

    assertTrue(result);
    assertFalse(targetFile.exists());
  }

  @Test
  void copyResettedNvRam_copiesFile_whenResettedFileExists(@TempDir Path tempDir) throws IOException {
    Path nvFolder = tempDir.resolve("nvrams");
    Files.createDirectories(nvFolder);
    Files.write(nvFolder.resolve("funhouse.nv"), "reset-data".getBytes());

    when(systemService.getResettedNVRamsFolder()).thenReturn(nvFolder.toFile());

    Path activeFolder = tempDir.resolve("active");
    Files.createDirectories(activeFolder);
    File targetFile = activeFolder.resolve("funhouse.nv").toFile();

    boolean result = nvRamService.copyResettedNvRam(targetFile);

    assertTrue(result);
    assertTrue(targetFile.exists());
    assertEquals("reset-data", new String(Files.readAllBytes(targetFile.toPath())));
  }
}
