package de.mephisto.vpin.server.vpauthenticators;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.vpauthenticators.AuthenticationProvider;
import de.mephisto.vpin.restclient.vpauthenticators.AuthenticationSettings;
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
      return "No authentication provider selected. Please choose and configure an authentication provider from the backup settings";
    }

    if (StringUtils.isEmpty(login) || StringUtils.isEmpty(password)) {
      String msg = "Missing credentials for authentication provider " + authenticationProvider + ", login and password need to be set.";
      LOG.info(msg);
      return msg;
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
        return "Invalid authentication provider";
      }
    }
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
      String msg = "Timeout running backup authentication, please try again later.";
      LOG.error(msg, e);
      return msg;
    }
    finally {
      executor.shutdownNow();
    }
  }

}
