package de.mephisto.vpin.server.vpinmame;

import de.mephisto.vpin.restclient.dmd.DMDInfoZone;
import de.mephisto.vpin.restclient.vpinmame.VPinMameOptions;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VPinMameServiceTest {

  @Mock
  private SystemService systemService;

  @Mock
  private FolderLookupService folderLookupService;

  @InjectMocks
  private VPinMameService service;

  // ---- clearCacheFor ----

  @Test
  void clearCacheFor_alwaysReturnsFalse() {
    assertFalse(service.clearCacheFor("myrom"));
  }

  @Test
  void clearCacheFor_handlesNull() {
    assertFalse(service.clearCacheFor(null));
  }

  // ---- isValidRom ----

  @Test
  void isValidRom_returnsTrueForUnknownRom() {
    assertTrue(service.isValidRom("unknown"));
  }

  // ---- getOptionsRaw ----

  @Test
  void getOptionsRaw_returnsNull_whenRomIsNull() {
    assertNull(service.getOptionsRaw(null));
  }

  @Test
  void getOptionsRaw_returnsNull_whenRomNotInRegistry() {
    when(systemService.getCurrentUserKeys(VPinMameService.MAME_REG_FOLDER_KEY))
        .thenReturn(Collections.emptyList());

    assertNull(service.getOptionsRaw("myrom"));
  }

  @Test
  void getOptionsRaw_returnsMap_whenRomExistsInRegistry() {
    String rom = "myrom";
    Map<String, Object> registryData = new HashMap<>();
    registryData.put("sound", 1);

    when(systemService.getCurrentUserKeys(VPinMameService.MAME_REG_FOLDER_KEY))
        .thenReturn(Arrays.asList(rom));
    when(systemService.getCurrentUserValues(VPinMameService.MAME_REG_FOLDER_KEY + rom))
        .thenReturn(registryData);

    Map<String, Object> result = service.getOptionsRaw(rom);

    assertNotNull(result);
    assertEquals(1, result.get("sound"));
  }

  // ---- getOptions ----

  @Test
  void getOptions_returnsOptions_withDefaultsFromRegistry() {
    String rom = "testrom";
    Map<String, Object> values = new HashMap<>();
    values.put("sound", 1);
    values.put("cheat", 1);

    when(systemService.getCurrentUserValues(VPinMameService.MAME_REG_FOLDER_KEY + rom))
        .thenReturn(values);

    VPinMameOptions options = service.getOptions(rom);

    assertNotNull(options);
    assertEquals(rom, options.getRom());
    assertTrue(options.isUseSound());
    assertTrue(options.isSkipPinballStartupTest());
  }

  @Test
  void getOptions_usesDefaultKey_whenRomValuesEmpty() {
    String rom = "unknownrom";
    Map<String, Object> defaultValues = new HashMap<>();
    defaultValues.put("samples", 1);

    when(systemService.getCurrentUserValues(VPinMameService.MAME_REG_FOLDER_KEY + rom))
        .thenReturn(Collections.emptyMap());
    when(systemService.getCurrentUserValues(VPinMameService.MAME_REG_FOLDER_KEY + VPinMameOptions.DEFAULT_KEY))
        .thenReturn(defaultValues);

    VPinMameOptions options = service.getOptions(rom);

    assertNotNull(options);
    assertFalse(options.isExistInRegistry());
    assertTrue(options.isUseSamples());
  }

  // ---- deleteOptions ----

  @Test
  void deleteOptions_callsDeleteUserKey() {
    String rom = "myrom";
    service.deleteOptions(rom);

    verify(systemService).deleteUserKey(VPinMameService.MAME_REG_FOLDER_KEY + rom);
  }

  // ---- getMameFolder ----

  @Test
  void getMameFolder_returnsNull_whenVpxFolderIsNull() {
    when(systemService.resolveVpx64InstallFolder()).thenReturn(null);

    assertNull(service.getMameFolder());
  }

  @Test
  void getMameFolder_returnsMameFolderUnderVpxInstall() {
    // use java.io.tmpdir which is guaranteed to exist
    File vpxFolder = new File(System.getProperty("java.io.tmpdir"));
    when(systemService.resolveVpx64InstallFolder()).thenReturn(vpxFolder);

    File result = service.getMameFolder();

    assertNotNull(result);
    assertEquals("VPinMAME", result.getName());
    assertEquals(vpxFolder, result.getParentFile());
  }

  // ---- getNvRamFolder / getCfgFolder / getRomsFolder ----

  @Test
  void getNvRamFolder_returnsFolder_fromRegistry() {
    File expected = new File("C:/mame/nvram");
    Map<String, Object> values = new HashMap<>();
    values.put(VPinMameService.NVRAM_DIRECTORY, expected.getPath());

    when(systemService.getCurrentUserValues(VPinMameService.MAME_REG_FOLDER_KEY + VPinMameOptions.GLOBALS_KEY))
        .thenReturn(values);

    File result = service.getNvRamFolder();

    assertNotNull(result);
    assertEquals(expected, result);
  }

  @Test
  void getCfgFolder_returnsNull_whenNotInRegistry() {
    when(systemService.getCurrentUserValues(VPinMameService.MAME_REG_FOLDER_KEY + VPinMameOptions.GLOBALS_KEY))
        .thenReturn(Collections.emptyMap());

    assertNull(service.getCfgFolder());
  }

  // ---- fillDmdPosition ----

  @Test
  void fillDmdPosition_returnsFalse_whenRomNotInRegistry() {
    String rom = "norom";
    DMDInfoZone zone = new DMDInfoZone();

    when(systemService.getCurrentUserKeys(VPinMameService.MAME_REG_FOLDER_KEY))
        .thenReturn(Collections.emptyList());
    when(systemService.getCurrentUserValues(VPinMameService.MAME_REG_FOLDER_KEY + VPinMameOptions.DEFAULT_KEY))
        .thenReturn(Collections.emptyMap());

    boolean result = service.fillDmdPosition(rom, zone);

    assertFalse(result);
  }

  @Test
  void fillDmdPosition_returnsTrue_whenRomExistsInRegistry() {
    String rom = "somerom";
    DMDInfoZone zone = new DMDInfoZone();
    Map<String, Object> dmdValues = new HashMap<>();
    dmdValues.put("dmd_pos_x", 10);
    dmdValues.put("dmd_pos_y", 20);
    dmdValues.put("dmd_width", 300);
    dmdValues.put("dmd_height", 100);

    when(systemService.getCurrentUserKeys(VPinMameService.MAME_REG_FOLDER_KEY))
        .thenReturn(Arrays.asList(rom.toLowerCase()));
    when(systemService.getCurrentUserValues(VPinMameService.MAME_REG_FOLDER_KEY + rom))
        .thenReturn(dmdValues);

    boolean result = service.fillDmdPosition(rom, zone);

    assertTrue(result);
    assertEquals(10, zone.getX());
    assertEquals(20, zone.getY());
  }

  // ---- MAME_REG_FOLDER_KEY constant ----

  @Test
  void mameRegFolderKey_hasExpectedValue() {
    assertEquals("SOFTWARE\\Freeware\\Visual PinMame\\", VPinMameService.MAME_REG_FOLDER_KEY);
  }
}
