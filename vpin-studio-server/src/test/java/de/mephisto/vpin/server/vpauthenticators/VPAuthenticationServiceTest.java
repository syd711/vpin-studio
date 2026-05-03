package de.mephisto.vpin.server.vpauthenticators;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.vpauthenticators.AuthenticationProvider;
import de.mephisto.vpin.restclient.vpauthenticators.AuthenticationSettings;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VPAuthenticationServiceTest {

  @Mock
  private PreferencesService preferencesService;

  @InjectMocks
  private VPAuthenticationService service;

  // ---- isAuthenticated ----

  @Test
  void isAuthenticated_returnsTrue_whenSettingsAuthenticated() {
    AuthenticationSettings settings = new AuthenticationSettings();
    settings.setAuthenticated(true);
    when(preferencesService.getJsonPreference(PreferenceNames.AUTHENTICATION_SETTINGS, AuthenticationSettings.class)).thenReturn(settings);

    assertTrue(service.isAuthenticated());
  }

  @Test
  void isAuthenticated_returnsFalse_whenNotAuthenticated() {
    AuthenticationSettings settings = new AuthenticationSettings();
    settings.setAuthenticated(false);
    when(preferencesService.getJsonPreference(PreferenceNames.AUTHENTICATION_SETTINGS, AuthenticationSettings.class)).thenReturn(settings);

    assertFalse(service.isAuthenticated());
  }

  // ---- authenticate (no provider) ----

  @Test
  void authenticate_returnsErrorMessage_whenProviderIsNull() {
    String result = service.authenticate(null, "user", "pass");

    assertNotNull(result);
    assertTrue(result.contains("No authentication provider selected"));
  }

  // ---- authenticate (empty credentials) ----

  @Test
  void authenticate_returnsErrorMessage_whenLoginIsEmpty() {
    String result = service.authenticate(AuthenticationProvider.VPF, "", "pass");

    assertNotNull(result);
    assertTrue(result.contains("Missing credentials"));
  }

  @Test
  void authenticate_returnsErrorMessage_whenPasswordIsEmpty() {
    String result = service.authenticate(AuthenticationProvider.VPF, "user", "");

    assertNotNull(result);
    assertTrue(result.contains("Missing credentials"));
  }

  // ---- login ----

  @Test
  void login_setsAuthenticatedFalse_initially() throws Exception {
    AuthenticationSettings settings = new AuthenticationSettings();
    when(preferencesService.getJsonPreference(PreferenceNames.AUTHENTICATION_SETTINGS, AuthenticationSettings.class)).thenReturn(settings);

    // authenticate with null provider returns error; login sets authenticated=false first, then
    // leaves it false since authenticate returned non-null
    String result = service.login(null, "user", "pass");

    assertNotNull(result);
    verify(preferencesService, atLeastOnce()).savePreference(any(), anyBoolean());
  }
}
