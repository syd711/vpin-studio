package de.mephisto.vpin.server.directb2s;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.directb2s.DirectB2SDetail;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.directb2s.DirectB2ServerSettings;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.validation.BackglassValidationCode;
import de.mephisto.vpin.restclient.validation.IgnoredValidationSettings;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static de.mephisto.vpin.restclient.validation.BackglassValidationCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BackglassValidationServiceTest {

  @Mock
  private PreferencesService preferencesService;
  @Mock
  private EmulatorService emulatorService;
  @Mock
  private FrontendService frontendService;

  @InjectMocks
  private BackglassValidationService backglassValidationService;

  private GameEmulator vpxEmulator;
  private GameEmulator zenEmulator;

  @BeforeEach
  void setup() throws Exception {
    when(frontendService.getFrontend()).thenReturn(new Frontend());
    when(preferencesService.getJsonPreference(PreferenceNames.IGNORED_VALIDATION_SETTINGS, IgnoredValidationSettings.class))
        .thenReturn(new IgnoredValidationSettings());
    backglassValidationService.afterPropertiesSet();

    vpxEmulator = new GameEmulator();
    vpxEmulator.setType(EmulatorType.VisualPinball);

    zenEmulator = new GameEmulator();
    zenEmulator.setType(EmulatorType.ZenFX);
  }

  private DirectB2SDetail detail(int emulatorId, String filename) {
    DirectB2SDetail d = new DirectB2SDetail();
    d.setEmulatorId(emulatorId);
    d.setFilename(filename);
    return d;
  }

  // ---- CODE_NOT_RUN_AS_EXE ----

  @Test
  void validate_reportsNotRunAsExe_whenTableSettingsNullAndServerNotEXE() {
    when(emulatorService.getGameEmulator(1)).thenReturn(vpxEmulator);
    DirectB2SDetail d = detail(1, "game");

    // tableSettings=null → tableLaunchAsExe=2 (server-default), serverSettings=null → not EXE
    List<ValidationState> result = backglassValidationService.validate(d, null, null, null, false);

    assertThat(result).anyMatch(v -> v.getCode() == CODE_NOT_RUN_AS_EXE);
  }

  @Test
  void validate_noNotRunAsExe_whenServerDefaultIsEXE() {
    when(emulatorService.getGameEmulator(1)).thenReturn(vpxEmulator);
    DirectB2SDetail d = detail(1, "game");

    // DirectB2ServerSettings.defaultStartMode defaults to EXE_START_MODE (2)
    DirectB2ServerSettings serverSettings = new DirectB2ServerSettings();

    List<ValidationState> result = backglassValidationService.validate(d, null, null, serverSettings, false);

    assertThat(result).noneMatch(v -> v.getCode() == CODE_NOT_RUN_AS_EXE);
  }

  @Test
  void validate_reportsNotRunAsExe_whenTableStartAsExeIsZero() {
    when(emulatorService.getGameEmulator(1)).thenReturn(vpxEmulator);
    DirectB2SDetail d = detail(1, "game");

    DirectB2STableSettings tableSettings = new DirectB2STableSettings();
    tableSettings.setStartAsEXE(0);

    List<ValidationState> result = backglassValidationService.validate(d, null, tableSettings, null, false);

    assertThat(result).anyMatch(v -> v.getCode() == CODE_NOT_RUN_AS_EXE);
  }

  @Test
  void validate_noNotRunAsExe_whenTableExplicitlySetToEXE() {
    when(emulatorService.getGameEmulator(1)).thenReturn(vpxEmulator);
    DirectB2SDetail d = detail(1, "game");

    // startAsEXE=1 means plugin mode (not 0=off, not 2=server default)
    DirectB2STableSettings tableSettings = new DirectB2STableSettings();
    tableSettings.setStartAsEXE(1);

    List<ValidationState> result = backglassValidationService.validate(d, null, tableSettings, null, false);

    assertThat(result).noneMatch(v -> v.getCode() == CODE_NOT_RUN_AS_EXE);
  }

  // ---- CODE_NO_FULLDMD ----

  @Test
  void validate_reportsNoFullDmd_whenDmdImageNotAvailable() {
    when(emulatorService.getGameEmulator(1)).thenReturn(vpxEmulator);
    DirectB2SDetail d = detail(1, "game");
    d.setDmdImageAvailable(false);

    DirectB2ServerSettings serverSettings = new DirectB2ServerSettings(); // suppress NOT_RUN_AS_EXE

    List<ValidationState> result = backglassValidationService.validate(d, null, null, serverSettings, false);

    assertThat(result).anyMatch(v -> v.getCode() == CODE_NO_FULLDMD);
  }

  @Test
  void validate_noNoFullDmd_whenDmdImageIsAvailable() {
    when(emulatorService.getGameEmulator(1)).thenReturn(vpxEmulator);
    DirectB2SDetail d = detail(1, "game");
    d.setDmdImageAvailable(true);
    d.setFullDmd(true);

    DirectB2ServerSettings serverSettings = new DirectB2ServerSettings();

    List<ValidationState> result = backglassValidationService.validate(d, null, null, serverSettings, false);

    assertThat(result).noneMatch(v -> v.getCode() == CODE_NO_FULLDMD);
  }

  // ---- CODE_WRONG_FULLDMD_RATIO ----

  @Test
  void validate_reportsWrongFullDmdRatio_whenDmdAvailableButNotFullDmd() {
    when(emulatorService.getGameEmulator(1)).thenReturn(vpxEmulator);
    DirectB2SDetail d = detail(1, "game");
    d.setDmdImageAvailable(true);
    d.setFullDmd(false);

    DirectB2ServerSettings serverSettings = new DirectB2ServerSettings();

    List<ValidationState> result = backglassValidationService.validate(d, null, null, serverSettings, false);

    assertThat(result).anyMatch(v -> v.getCode() == CODE_WRONG_FULLDMD_RATIO);
  }

  @Test
  void validate_noWrongFullDmdRatio_whenDmdIsFullDmd() {
    when(emulatorService.getGameEmulator(1)).thenReturn(vpxEmulator);
    DirectB2SDetail d = detail(1, "game");
    d.setDmdImageAvailable(true);
    d.setFullDmd(true);

    DirectB2ServerSettings serverSettings = new DirectB2ServerSettings();

    List<ValidationState> result = backglassValidationService.validate(d, null, null, serverSettings, false);

    assertThat(result).noneMatch(v -> v.getCode() == CODE_WRONG_FULLDMD_RATIO);
  }

  // ---- findFirst ----

  @Test
  void validate_findFirst_returnsAfterFirstViolation() {
    // Use Zen emulator so CODE_NO_GAME check is skipped; CODE_NOT_RUN_AS_EXE fires first
    when(emulatorService.getGameEmulator(2)).thenReturn(zenEmulator);
    DirectB2SDetail d = detail(2, "game");
    d.setDmdImageAvailable(false); // would also trigger CODE_NO_FULLDMD if we reach it

    // serverSettings=null → serverLaunchAsExe=false → CODE_NOT_RUN_AS_EXE fires first
    List<ValidationState> result = backglassValidationService.validate(d, null, null, null, true);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).getCode()).isEqualTo(CODE_NOT_RUN_AS_EXE);
  }

  // ---- ignored validations ----

  @Test
  void validate_skipsCode_whenFrontendIgnoresIt() throws Exception {
    Frontend frontend = new Frontend();
    frontend.setIgnoredValidations(List.of(CODE_NOT_RUN_AS_EXE));
    when(frontendService.getFrontend()).thenReturn(frontend);
    when(preferencesService.getJsonPreference(PreferenceNames.IGNORED_VALIDATION_SETTINGS, IgnoredValidationSettings.class))
        .thenReturn(new IgnoredValidationSettings());
    backglassValidationService.afterPropertiesSet();

    when(emulatorService.getGameEmulator(1)).thenReturn(vpxEmulator);
    DirectB2SDetail d = detail(1, "game");

    List<ValidationState> result = backglassValidationService.validate(d, null, null, null, false);

    assertThat(result).noneMatch(v -> v.getCode() == CODE_NOT_RUN_AS_EXE);
  }

  @Test
  void validate_skipsCode_whenIgnoredValidationSettingsIgnoresIt() throws Exception {
    IgnoredValidationSettings settings = new IgnoredValidationSettings();
    settings.getIgnoredValidators().put(String.valueOf(CODE_NOT_RUN_AS_EXE), true);
    when(preferencesService.getJsonPreference(PreferenceNames.IGNORED_VALIDATION_SETTINGS, IgnoredValidationSettings.class))
        .thenReturn(settings);
    when(frontendService.getFrontend()).thenReturn(new Frontend());
    backglassValidationService.afterPropertiesSet();

    when(emulatorService.getGameEmulator(1)).thenReturn(vpxEmulator);
    DirectB2SDetail d = detail(1, "game");

    List<ValidationState> result = backglassValidationService.validate(d, null, null, null, false);

    assertThat(result).noneMatch(v -> v.getCode() == CODE_NOT_RUN_AS_EXE);
  }

  // ---- Zen emulator skips CODE_NO_GAME ----

  @Test
  void validate_skipsNoGameCheck_forZenEmulator() {
    when(emulatorService.getGameEmulator(2)).thenReturn(zenEmulator);
    DirectB2SDetail d = detail(2, "zenGame");

    DirectB2ServerSettings serverSettings = new DirectB2ServerSettings(); // suppress NOT_RUN_AS_EXE
    d.setDmdImageAvailable(true);
    d.setFullDmd(true);

    List<ValidationState> result = backglassValidationService.validate(d, null, null, serverSettings, false);

    assertThat(result).noneMatch(v -> v.getCode() == BackglassValidationCode.CODE_NO_GAME);
  }
}
