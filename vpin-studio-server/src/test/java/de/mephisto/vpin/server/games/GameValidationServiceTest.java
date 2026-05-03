package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.restclient.validation.IgnoredValidationSettings;
import de.mephisto.vpin.restclient.validation.ValidationSettings;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.altcolor.AltColorService;
import de.mephisto.vpin.server.altsound.AltSoundService;
import de.mephisto.vpin.server.doflinx.DOFLinxService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.highscores.HighscoreResolver;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.highscores.parsing.vpreg.VPRegService;
import de.mephisto.vpin.server.music.MusicService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.vpinmame.VPinMameRomAliasService;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import de.mephisto.vpin.server.vps.VpsService;
import de.mephisto.vpin.server.vpx.FolderLookupService;
import de.mephisto.vpin.server.vpx.VPXService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static de.mephisto.vpin.restclient.validation.GameValidationCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameValidationServiceTest {

  @Mock
  private PreferencesService preferencesService;
  @Mock
  private AltSoundService altSoundService;
  @Mock
  private AltColorService altColorService;
  @Mock
  private PupPacksService pupPacksService;
  @Mock
  private VPinMameService vPinMameService;
  @Mock
  private FrontendService frontendService;
  @Mock
  private SystemService systemService;
  @Mock
  private HighscoreService highscoreService;
  @Mock
  private HighscoreResolver highscoreResolver;
  @Mock
  private VPinMameRomAliasService VPinMameRomAliasService;
  @Mock
  private GameDetailsRepositoryService gameDetailsRepositoryService;
  @Mock
  private VpsService vpsService;
  @Mock
  private VPRegService vpRegService;
  @Mock
  private VPXService vpxService;
  @Mock
  private MusicService musicService;
  @Mock
  private FolderLookupService folderLookupService;
  @Mock
  private DOFLinxService dofLinxService;

  @InjectMocks
  private GameValidationService service;

  @BeforeEach
  void setUp() throws Exception {
    setField("frontend", new Frontend());
    setField("validationSettings", new ValidationSettings());
    setField("ignoredValidationSettings", new IgnoredValidationSettings());
  }

  private void setField(String name, Object value) throws Exception {
    Field f = GameValidationService.class.getDeclaredField(name);
    f.setAccessible(true);
    f.set(service, value);
  }

  // ---- hasMissingAssets ----

  @Test
  void hasMissingAssets_returnsFalse_whenStatesEmpty() {
    assertFalse(service.hasMissingAssets(Collections.emptyList()));
  }

  @Test
  void hasMissingAssets_returnsTrue_whenContainsNoAudioCode() {
    ValidationState state = new ValidationState();
    state.setCode(CODE_NO_AUDIO);
    assertTrue(service.hasMissingAssets(List.of(state)));
  }

  @Test
  void hasMissingAssets_returnsTrue_whenContainsNoWheelImageCode() {
    ValidationState state = new ValidationState();
    state.setCode(CODE_NO_WHEEL_IMAGE);
    assertTrue(service.hasMissingAssets(List.of(state)));
  }

  @Test
  void hasMissingAssets_returnsFalse_whenContainsUnrelatedCode() {
    ValidationState state = new ValidationState();
    state.setCode(CODE_NO_ROM);
    assertFalse(service.hasMissingAssets(List.of(state)));
  }

  // ---- validateAltSound ----

  @Test
  void validateAltSound_returnsEmpty_whenAltSoundNotAvailable() {
    Game game = mock(Game.class);
    when(game.isAltSoundAvailable()).thenReturn(false);

    List<ValidationState> result = service.validateAltSound(game);

    assertTrue(result.isEmpty());
  }

  @Test
  void validateAltSound_addsNotEnabledCode_whenAltSoundAvailableButModeIsZero() {
    Game game = mock(Game.class);
    when(game.isAltSoundAvailable()).thenReturn(true);
    when(game.getIgnoredValidations()).thenReturn(new ArrayList<>());
    when(altSoundService.getAltSoundMode(game)).thenReturn(0);

    List<ValidationState> result = service.validateAltSound(game);

    assertEquals(1, result.size());
    assertEquals(CODE_ALT_SOUND_NOT_ENABLED, result.get(0).getCode());
  }

  @Test
  void validateAltSound_returnsEmpty_whenAltSoundAvailableAndModePositive() {
    Game game = mock(Game.class);
    when(game.isAltSoundAvailable()).thenReturn(true);
    when(game.getIgnoredValidations()).thenReturn(new ArrayList<>());
    when(altSoundService.getAltSoundMode(game)).thenReturn(1);

    // CODE_ALT_SOUND_FILE_MISSING is ignored by default in IgnoredValidationSettings
    List<ValidationState> result = service.validateAltSound(game);

    assertTrue(result.isEmpty());
  }

  // ---- validatePupPack ----

  @Test
  void validatePupPack_returnsEmpty_whenScanActive() {
    Game game = mock(Game.class);
    when(pupPacksService.isScanActive()).thenReturn(true);

    List<ValidationState> result = service.validatePupPack(game);

    assertTrue(result.isEmpty());
  }

  @Test
  void validatePupPack_addsDisabledCode_whenNob2sAndPupPackDisabled() {
    Game game = mock(Game.class);
    when(game.getDirectB2SPath()).thenReturn(null);
    when(pupPacksService.isScanActive()).thenReturn(false);
    when(pupPacksService.hasPupPack(game)).thenReturn(true);
    when(pupPacksService.isPupPackDisabled(game)).thenReturn(true);

    List<ValidationState> result = service.validatePupPack(game);

    assertTrue(result.stream().anyMatch(s -> s.getCode() == CODE_NO_DIRECTB2S_AND_PUPPACK_DISABLED));
  }

  @Test
  void validatePupPack_returnsEmpty_whenPupPackPresent_b2sPresent() {
    Game game = mock(Game.class);
    when(game.getDirectB2SPath()).thenReturn("somePath");
    when(pupPacksService.isScanActive()).thenReturn(false);

    List<ValidationState> result = service.validatePupPack(game);

    assertTrue(result.isEmpty());
  }
}
