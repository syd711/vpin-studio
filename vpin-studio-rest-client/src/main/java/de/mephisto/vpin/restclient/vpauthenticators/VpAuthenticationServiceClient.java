package de.mephisto.vpin.restclient.vpauthenticators;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

/*********************************************************************************************************************
 * Authentication Service
 ********************************************************************************************************************/
public class VpAuthenticationServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public VpAuthenticationServiceClient(VPinStudioClient client) {
    super(client);
  }

  public String login(AuthenticationProvider authenticationProvider, String login, String password) {
    Map<String, String> data = new HashMap<>();
    data.put("login", login);
    data.put("password", password);
    return getRestClient().post(API + "vpauthentication/" + authenticationProvider.name() + "/login", data, String.class);
  }

  public boolean isAuthenticated() {
    client.getPreferenceService().clearCache(PreferenceNames.AUTHENTICATION_SETTINGS);
    AuthenticationSettings authenticationSettings = client.getPreferenceService().getJsonPreference(PreferenceNames.AUTHENTICATION_SETTINGS, AuthenticationSettings.class);
    return authenticationSettings.isAuthenticated();
  }
}
