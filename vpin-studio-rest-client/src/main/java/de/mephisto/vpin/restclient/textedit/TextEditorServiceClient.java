package de.mephisto.vpin.restclient.textedit;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.popper.TableDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class TextEditorServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(TextEditorServiceClient.class);

  public TextEditorServiceClient(VPinStudioClient client) {
    super(client);
  }

  public TextFile getText(VPinFile file) {
    final RestTemplate restTemplate = new RestTemplate();
    return restTemplate.getForObject(getRestClient().getBaseUrl() + API + "textedit/" + file.name(), TextFile.class);
  }

  public TextFile save(VPinFile file, String text) throws Exception {
    Map<String, Object> model = new HashMap<>();
    model.put("text", text);
    return getRestClient().put(API + "textedit/" + file.name(), model, TextFile.class);
  }
}
