package de.mephisto.vpin.restclient.textedit;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TextEditorServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(TextEditorServiceClient.class);

  public TextEditorServiceClient(VPinStudioClient client) {
    super(client);
  }

  public TextFile getText(TextFile file) throws Exception {
    return getRestClient().post(API + "textedit/open", file, TextFile.class);
  }

  public TextFile save(TextFile file) throws Exception {
    return getRestClient().post(API + "textedit/save", file, TextFile.class);
  }
}
