package de.mephisto.vpin.server.vpauthenticators;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.vpauthenticators.AuthenticationProvider;
import de.mephisto.vpin.restclient.vpauthenticators.AuthenticationSettings;
import de.mephisto.vpin.restclient.vpf.VPFSettings;
import de.mephisto.vpin.restclient.vpu.VPUSettings;
import de.mephisto.vpin.server.preferences.PreferencesService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.StringUtils;
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
    try {
      ExecutorService executor = Executors.newSingleThreadExecutor();
      Future<String> submit = executor.submit(new Callable<String>() {
        @Override
        public String call() throws Exception {
          Thread.currentThread().setName("VP Authentication Service");
          if (authenticationProvider == null) {
            LOG.info("Skipped authentication, no provider selected.");
            return "No authentication provider selected. Please choose and configure an authentication provider from the backup settings";
          }

          if (StringUtils.isEmpty(login) || StringUtils.isEmpty(password)) {
            String msg = "Missing credentials for authentication provider " + authenticationProvider + ", login and password need to be set.";
            LOG.info(msg);
            return msg;
          }

          LOG.info("Authentication using " + authenticationProvider);
          switch (authenticationProvider) {
            case VPF: {
              VPFSettings settings = preferencesService.getJsonPreference(PreferenceNames.VPF_SETTINGS, VPFSettings.class);
              settings.setLogin(login);
              settings.setPassword(password);
              preferencesService.savePreference(settings);
              return new VpfVPAuthenticator(settings).login();
            }
            case VPU: {
              VPUSettings settings = preferencesService.getJsonPreference(PreferenceNames.VPU_SETTINGS, VPUSettings.class);
              settings.setLogin(login);
              settings.setPassword(password);
              preferencesService.savePreference(settings);
              return new VpuVPAuthenticator(settings).login();
            }
            default: {
              return "Invalid authentication provider";
            }
          }
        }
      });

      return submit.get(5, TimeUnit.SECONDS);
    }
    catch (Exception e) {
      String msg = "Timeout running backup authentication, please try again later. (" + e.getMessage() + ")";
      LOG.error(msg + ": {}", e.getMessage());
      return msg;
    }
  }

}
