package de.mephisto.vpin.server.vpauthenticators;

import de.mephisto.vpin.server.util.ServerMessages;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.vpauthenticators.AuthenticationProvider;
import de.mephisto.vpin.restclient.vpauthenticators.AuthenticationSettings;
import de.mephisto.vpin.restclient.vpauthenticators.CredentialSettings;
import de.mephisto.vpin.restclient.vpf.VPFSettings;
import de.mephisto.vpin.restclient.vpu.VPUSettings;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.*;

@Service
public class VPAuthenticationService {
  private final static Logger LOG = LoggerFactory.getLogger(VPAuthenticationService.class);

  @Autowired
  private PreferencesService preferencesService;

  public String login(@NonNull AuthenticationProvider authenticationProvider, @NonNull String login, @NonNull String password) throws Exception {
    AuthenticationSettings authenticationSettings = preferencesService.getJsonPreference(PreferenceNames.AUTHENTICATION_SETTINGS, AuthenticationSettings.class);
    authenticationSettings.setAuthenticated(false);
    preferencesService.savePreference(authenticationSettings, true);
    String result = authenticate(authenticationProvider, login, password);
    if (result == null) {
      LOG.info("Login successful for authentication provider {}/{}", authenticationProvider, authenticationProvider.getUrl());
      authenticationSettings.setAuthenticated(true);
      authenticationSettings.setToken(login);
      preferencesService.savePreference(authenticationSettings);
    }
    return result;
  }

  public boolean isAuthenticated() {
    AuthenticationSettings authenticationSettings = preferencesService.getJsonPreference(PreferenceNames.AUTHENTICATION_SETTINGS, AuthenticationSettings.class);
    return authenticationSettings.isAuthenticated();
  }

  public String authenticate(AuthenticationProvider authenticationProvider, String login, String password) {
    if (authenticationProvider == null) {
      LOG.info("Skipped authentication, no provider selected.");
      return ServerMessages.msg("auth.error.no_provider");
    }

    // the client never receives the stored password, a blank password means "use the stored one"
    if (StringUtils.isEmpty(password)) {
      password = getStoredPassword(authenticationProvider, login);
    }

    if (StringUtils.isEmpty(login) || StringUtils.isEmpty(password)) {
      LOG.info("Missing credentials for authentication provider {}, login and password need to be set.", authenticationProvider);
      return ServerMessages.msg("auth.error.missing_credentials", authenticationProvider);
    }

    LOG.info("Authentication using {}", authenticationProvider);

    // Save credentials to DB on the calling thread before spawning executor,
    // so the background thread never needs a DB connection (avoids pool starvation
    // when the HTTP thread holds the only HikariCP connection and blocks on future.get).
    switch (authenticationProvider) {
      case VPF: {
        VPFSettings vpfSettings = preferencesService.getJsonPreference(PreferenceNames.VPF_SETTINGS, VPFSettings.class);
        vpfSettings.setLogin(login);
        vpfSettings.setPassword(password);
        try {
          preferencesService.savePreference(vpfSettings);
        }
        catch (Exception e) {
          LOG.error("Error saving auth settings: {}", e.getMessage());
        }
        return runWithTimeout(() -> new VpfVPAuthenticator(vpfSettings).login());
      }
      case VPU: {
        VPUSettings vpuSettings = preferencesService.getJsonPreference(PreferenceNames.VPU_SETTINGS, VPUSettings.class);
        vpuSettings.setLogin(login);
        vpuSettings.setPassword(password);
        try {
          preferencesService.savePreference(vpuSettings);
        }
        catch (Exception e) {
          LOG.error("Error saving auth settings: {}", e.getMessage());
        }
        return runWithTimeout(() -> new VpuVPAuthenticator(vpuSettings).login());
      }
      default: {
        return ServerMessages.msg("auth.error.invalid_provider");
      }
    }
  }

  private String getStoredPassword(AuthenticationProvider authenticationProvider, String login) {
    String key = switch (authenticationProvider) {
      case VPF -> PreferenceNames.VPF_SETTINGS;
      case VPU -> PreferenceNames.VPU_SETTINGS;
    };
    CredentialSettings stored = (CredentialSettings) preferencesService.getJsonPreference(key, PreferenceNames.getClassFromKey(key));
    // only reuse the stored password for the same account
    if (stored != null && StringUtils.equals(stored.getLogin(), login)) {
      return stored.getPassword();
    }
    return null;
  }

  private String runWithTimeout(Callable<String> task) {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      Future<String> future = executor.submit(() -> {
        Thread.currentThread().setName("VP Authentication Service");
        return task.call();
      });
      return future.get(60, TimeUnit.SECONDS);
    }
    catch (Exception e) {
      LOG.error("Timeout running backup authentication, please try again later.", e);
      return ServerMessages.msg("auth.error.timeout");
    }
    finally {
      executor.shutdownNow();
    }
  }

}
