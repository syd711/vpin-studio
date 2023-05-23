package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.TableManagerSettings;


/*********************************************************************************************************************
 * Table Manager
 ********************************************************************************************************************/
public class TableManager extends AbstractStudioClientModule {
  TableManager(VPinStudioClient client) {
    super(client);
  }

  public TableManagerSettings getTableManagerSettings() {
    return getRestClient().get(API + "popper/manager", TableManagerSettings.class);
  }

  public boolean saveTableManagerSettings(TableManagerSettings descriptor) throws Exception {
    return getRestClient().post(API + "popper/manager", descriptor, Boolean.class);
  }
}
