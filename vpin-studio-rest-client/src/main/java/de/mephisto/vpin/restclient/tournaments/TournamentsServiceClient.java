package de.mephisto.vpin.restclient.tournaments;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.mania.ManiaSettings;
import de.mephisto.vpin.restclient.preferences.PreferencesServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/*********************************************************************************************************************
 * Tournaments
 ********************************************************************************************************************/
public class TournamentsServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public TournamentsServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean synchronize() {
    return getRestClient().get(API + "tournaments/synchronize", Boolean.class);
  }

  public boolean synchronize(TournamentMetaData tournamentMetaData) throws Exception {
    try {
      return getRestClient().post(API + "tournaments/synchronize", tournamentMetaData, Boolean.class);
    } catch (Exception e) {
      LOG.error("Failed to save tournament meta data: " + e.getMessage(), e);
      throw e;
    }
  }
}
