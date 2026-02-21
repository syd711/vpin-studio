package de.mephisto.vpin.server.vpxz;

import de.mephisto.vpin.restclient.RestClient;
import de.mephisto.vpin.restclient.vpxz.models.Tables;
import de.mephisto.vpin.restclient.vpxz.models.Version;
import edu.umd.cs.findbugs.annotations.NonNull;

public class MobileDeviceClient {

  private RestClient restClient;

  public MobileDeviceClient(@NonNull String host, int port) {
    restClient = RestClient.createInstance(host, port);
    restClient.initRestClientWithTimeoutMs(2000);
  }

  public Version getInfo() {
    return restClient.get("info", Version.class);
  }

  public Tables getTables() {
    return restClient.get("download?q=10.8%2Ftables.json", Tables.class);
  }
}
