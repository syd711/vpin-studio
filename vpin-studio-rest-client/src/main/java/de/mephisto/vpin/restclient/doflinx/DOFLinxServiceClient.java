package de.mephisto.vpin.restclient.doflinx;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.components.ComponentSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

/*********************************************************************************************************************
 * DOFLinx
 ********************************************************************************************************************/
public class DOFLinxServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public DOFLinxServiceClient(VPinStudioClient client) {
    super(client);
  }

  public boolean isAutoStartEnabled() {
    return getRestClient().get(API + "doflinx/autostart", Boolean.class);
  }

  public boolean toggleAutoStart() {
    return getRestClient().get(API + "doflinx/autostart/toggle", Boolean.class);
  }

  public boolean kill() {
    return getRestClient().get(API + "doflinx/kill", Boolean.class);
  }

  public boolean isValid() {
    return getRestClient().get(API + "doflinx/valid", Boolean.class);
  }

  public boolean isRunning() {
    return getRestClient().get(API + "doflinx/running", Boolean.class);
  }

  public boolean restart() {
    return getRestClient().get(API + "doflinx/restart", Boolean.class);
  }

  public ComponentSummary getDOFLinxSummary() {
    return getRestClient().get(API + "doflinx/summary", ComponentSummary.class);
  }
}
