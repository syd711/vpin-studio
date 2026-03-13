package de.mephisto.vpin.restclient.mania;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/*********************************************************************************************************************
 * Mania
 ********************************************************************************************************************/
public class ManiaServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public ManiaServiceClient(VPinStudioClient client) {
    super(client);
  }

  public ManiaConfig getConfig() {
    return getRestClient().get(API + "mania/config", ManiaConfig.class);
  }

  public ManiaTableSyncResult synchronizeHighscore(String vpsTableId) {
    return getRestClient().get(API + "mania/synchronize/highscore/" + vpsTableId, ManiaTableSyncResult.class);
  }

  public Boolean deleteCabinet() {
    return getRestClient().delete(API + "mania/cabinet");
  }

  public boolean synchronizeTables() {
    return getRestClient().get(API + "mania/synchronize/tables", Boolean.class);
  }

  public ManiaRegistration register(@NonNull ManiaRegistration registration) {
    try {
      return getRestClient().post(API + "mania/register", registration, ManiaRegistration.class);
    }
    catch (Exception e) {
      LOG.error("Failed to register for VPin Mania: " + e.getMessage(), e);
      throw e;
    }
  }

  public boolean clearCache() {
    return getRestClient().get(API + "mania/clearcache", Boolean.class);
  }

}
