package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.altcolor.AltColor;
import de.mephisto.vpin.restclient.altcolor.AltColorTypes;
import de.mephisto.vpin.restclient.altsound.AltSound;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.validation.GameValidationCode;
import de.mephisto.vpin.restclient.validation.IgnoredValidationSettings;
import de.mephisto.vpin.restclient.validation.ValidationSettings;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.restclient.vpinmame.VPinMameOptions;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static de.mephisto.vpin.restclient.validation.GameValidationCode.*;
import static de.mephisto.vpin.server.VPinStudioServer.Features;
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

  @AfterEach
  void tearDown() {
    Features.VPINMAME_OPTIONS = true;
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

  // ---- VPinMAME options, which only exist in the Windows registry ----

  private Game vpinMameGame() {
    Game game = mock(Game.class);
    lenient().when(game.getRom()).thenReturn("rom");
    lenient().when(game.isVpxGame()).thenReturn(true);
    lenient().when(game.getIgnoredValidations()).thenReturn(new ArrayList<>());
    lenient().when(game.getAltColorType()).thenReturn(AltColorTypes.serum);

    AltColor altColor = new AltColor();
    altColor.setAltColorType(AltColorTypes.serum);
    altColor.setFiles(List.of("rom.cRZ"));
    lenient().when(altColorService.getAltColor(game)).thenReturn(altColor);

    // what the registry yields without one: every option off
    lenient().when(vPinMameService.getOptions(anyString())).thenReturn(new VPinMameOptions());
    lenient().when(altSoundService.getAltSoundMode(game)).thenReturn(0);
    return game;
  }

  private static boolean hasCode(List<ValidationState> states, int code) {
    return states.stream().anyMatch(s -> s.getCode() == code);
  }

  @Test
  void vpinMameOptionChecks_fire_whenOptionsAreOff() throws Exception {
    setField("ignoredValidationSettings", noIgnoredValidations());
    Game game = vpinMameGame();
    when(game.isAltSoundAvailable()).thenReturn(true);

    assertTrue(hasCode(service.validateAltSound(game), CODE_ALT_SOUND_NOT_ENABLED));
    List<ValidationState> altColor = service.validateAltColor(game);
    assertTrue(hasCode(altColor, CODE_ALT_COLOR_COLORIZE_DMD_ENABLED));
    assertTrue(hasCode(altColor, CODE_ALT_COLOR_EXTERNAL_DMD_NOT_ENABLED));

    when(game.isAltSoundAvailable()).thenReturn(false);
    assertTrue(hasCode(service.validateForceStereo(game), CODE_FORCE_STEREO));
  }

  @Test
  void vpinMameOptionChecks_skipped_withoutRegistry() throws Exception {
    setField("ignoredValidationSettings", noIgnoredValidations());
    Features.VPINMAME_OPTIONS = false;
    Game game = vpinMameGame();
    when(game.isAltSoundAvailable()).thenReturn(true);

    assertFalse(hasCode(service.validateAltSound(game), CODE_ALT_SOUND_NOT_ENABLED));
    List<ValidationState> altColor = service.validateAltColor(game);
    assertFalse(hasCode(altColor, CODE_ALT_COLOR_COLORIZE_DMD_ENABLED));
    assertFalse(hasCode(altColor, CODE_ALT_COLOR_EXTERNAL_DMD_NOT_ENABLED));

    lenient().when(game.isAltSoundAvailable()).thenReturn(false);
    assertFalse(hasCode(service.validateForceStereo(game), CODE_FORCE_STEREO));
  }

  @Test
  void fileChecks_stillFire_withoutRegistry() throws Exception {
    setField("ignoredValidationSettings", noIgnoredValidations());
    Features.VPINMAME_OPTIONS = false;
    Game game = vpinMameGame();
    when(game.isAltSoundAvailable()).thenReturn(true);
    AltSound altSound = mock(AltSound.class);
    when(altSound.isMissingAudioFiles()).thenReturn(true);
    when(altSoundService.getAltSound(game)).thenReturn(altSound);
    altColorService.getAltColor(game).setFiles(new ArrayList<>());

    assertTrue(hasCode(service.validateAltSound(game), CODE_ALT_SOUND_FILE_MISSING));
    assertTrue(hasCode(service.validateAltColor(game), CODE_ALT_COLOR_FILES_MISSING));
  }

  private static IgnoredValidationSettings noIgnoredValidations() {
    IgnoredValidationSettings settings = new IgnoredValidationSettings();
    settings.setIgnoredValidators(new HashMap<>());
    return settings;
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
