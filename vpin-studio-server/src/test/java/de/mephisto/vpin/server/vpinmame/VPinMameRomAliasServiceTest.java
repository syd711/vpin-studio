package de.mephisto.vpin.server.vpinmame;

import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.GameCachingService;
import de.mephisto.vpin.server.games.GameEmulator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VPinMameRomAliasServiceTest {

  @Mock
  private EmulatorService emulatorService;

  @InjectMocks
  private VPinMameRomAliasService service;

  private GameEmulator emulator;

  @BeforeEach
  void setUp() {
    emulator = new GameEmulator();
    emulator.setId(1);
    emulator.setName("VPX");
    // FuturePinball type is non-VPX — avoids file I/O in clearCache
    emulator.setType(EmulatorType.FuturePinball);
  }

  // ---- getRomForAlias (in-memory cache) ----

  @Test
  void getRomForAlias_returnsNull_whenRomAliasIsNull() {
    String result = service.getRomForAlias(emulator, null);
    assertNull(result);
  }

  @Test
  void getRomForAlias_returnsNull_whenCacheIsEmpty() {
    String result = service.getRomForAlias(emulator, "somealias");
    assertNull(result);
  }

  // ---- getVPMAliasFile ----

  @Test
  void getVPMAliasFile_pointsToMameFolder(@TempDir Path tempDir) {
    emulator.setMameDirectory(tempDir.toString());

    File aliasFile = service.getVPMAliasFile(emulator);

    assertEquals(tempDir.toFile(), aliasFile.getParentFile());
    assertTrue(aliasFile.getName().endsWith(".txt") || aliasFile.getName().contains("alias"));
  }

  // ---- saveAliasFile (Map form) / loadAliasFile ----

  @Test
  void saveAndLoad_roundTripAliasMappings(@TempDir Path tempDir) throws IOException {
    emulator.setMameDirectory(tempDir.toString());
    // The VPMAliasFile is inside getMameFolder() — set it up
    File mameFolder = tempDir.toFile();
    emulator.setMameDirectory(mameFolder.getAbsolutePath());

    java.util.Map<String, String> mapping = new java.util.LinkedHashMap<>();
    mapping.put("mm_10", "mm_10r");
    mapping.put("godzilla", "gdz_100");

    service.saveAliasFile(emulator, mapping);

    File aliasFile = service.getVPMAliasFile(emulator);
    assertTrue(aliasFile.exists(), "alias file should have been created");

    String content = new String(Files.readAllBytes(aliasFile.toPath()), StandardCharsets.ISO_8859_1);
    assertTrue(content.contains("mm_10") && content.contains("mm_10r"),
        "Expected mm_10 alias in file content");
    assertTrue(content.contains("godzilla") && content.contains("gdz_100"),
        "Expected godzilla alias in file content");
  }

  @Test
  void loadAliasFile_returnsEmptyMonitoredTextFile_whenFileDoesNotExist(@TempDir Path tempDir) {
    emulator.setMameDirectory(tempDir.toString());
    // alias file does not exist yet

    var monitoredFile = service.loadAliasFile(emulator);

    assertNotNull(monitoredFile);
    assertNull(monitoredFile.getContent()); // no file → null content
  }

  // ---- clearCache (with emulator list) ----

  @Test
  void clearCache_returnsTrueForEmptyList() {
    boolean result = service.clearCache(Collections.emptyList());
    assertTrue(result);
  }

  @Test
  void clearCache_returnsTrueForNonVpxEmulator(@TempDir Path tempDir) {
    emulator.setMameDirectory(tempDir.toString());
    // isVpxEmulator() returns false when type is not VisualPinball — default GameEmulator
    boolean result = service.clearCache(List.of(emulator));
    assertTrue(result);
  }

  // ---- writeAlias — blank args are no-ops ----

  @Test
  void writeAlias_doesNothing_whenRomIsBlank(@TempDir Path tempDir) {
    emulator.setMameDirectory(tempDir.toString());
    // Should not throw and should not create a file
    service.writeAlias(emulator, "", "alias");
    assertFalse(service.getVPMAliasFile(emulator).exists());
  }

  @Test
  void writeAlias_doesNothing_whenAliasIsBlank(@TempDir Path tempDir) {
    emulator.setMameDirectory(tempDir.toString());
    service.writeAlias(emulator, "rom", "");
    assertFalse(service.getVPMAliasFile(emulator).exists());
  }

  // ---- deleteAlias ----

  @Test
  void deleteAlias_returnsTrueWhenAliasIsBlank(@TempDir Path tempDir) {
    emulator.setMameDirectory(tempDir.toString());
    boolean result = service.deleteAlias(emulator, "");
    assertTrue(result);
  }

  @Test
  void deleteAlias_returnsTrueWhenAliasFileDoesNotExist(@TempDir Path tempDir) {
    emulator.setMameDirectory(tempDir.toString());
    boolean result = service.deleteAlias(emulator, "nonexistent_alias");
    assertTrue(result);
  }
}
