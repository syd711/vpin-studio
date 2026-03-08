package de.mephisto.vpin.restclient.vpx;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class VpxScriptOptionsServiceClient extends VPinStudioClientService {

    private static final Logger LOG = LoggerFactory.getLogger(VpxScriptOptionsServiceClient.class);

    public VpxScriptOptionsServiceClient(VPinStudioClient client) {
        super(client);
    }

    private String url(String segment) {
        return client.getRestClient().getBaseUrl() + VPinStudioClientService.API + segment;
    }

    public List<TableScriptOption> getOptions(int gameId) {
      return Arrays.asList(getRestClient().get(API + "vpx/scriptoptions/" + gameId, TableScriptOption[].class));
    }

    public boolean saveOptions(int gameId, List<TableScriptOption> options) {
      return getRestClient().post(API + "vpx/scriptoptions/" + gameId, options, Boolean.class);
    }

    public boolean resetOptions(int gameId) {
      return getRestClient().post(API + "vpx/scriptoptions/" + gameId + "/reset", null, Boolean.class);
    }
}
