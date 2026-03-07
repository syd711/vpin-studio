package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.vpx.TableScriptOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
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
        try {
            RestTemplate restTemplate = new RestTemplate();
            TableScriptOption[] options = restTemplate.getForObject(
                    url("vpx/scriptoptions/" + gameId),
                    TableScriptOption[].class
            );
            return options != null ? Arrays.asList(options) : Collections.emptyList();
        }
        catch (Exception e) {
            LOG.error("getOptions failed for game {}: {}", gameId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean saveOptions(int gameId, List<TableScriptOption> options) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            Boolean result = restTemplate.postForObject(
                    url("vpx/scriptoptions/" + gameId),
                    options,
                    Boolean.class
            );
            return Boolean.TRUE.equals(result);
        }
        catch (Exception e) {
            LOG.error("saveOptions failed for game {}: {}", gameId, e.getMessage());
            return false;
        }
    }

    public boolean resetOptions(int gameId) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            Boolean result = restTemplate.postForObject(
                    url("vpx/scriptoptions/" + gameId + "/reset"),
                    null,
                    Boolean.class
            );
            return Boolean.TRUE.equals(result);
        }
        catch (Exception e) {
            LOG.error("resetOptions failed for game {}: {}", gameId, e.getMessage());
            return false;
        }
    }
}
