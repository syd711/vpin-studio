package de.mephisto.vpin.restclient.textedit;

import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;


public class TextEditorServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public TextEditorServiceClient(VPinStudioClient client) {
    super(client);
  }

  public MonitoredTextFile getText(MonitoredTextFile file) throws Exception {
    return getRestClient().post(API + "textedit/open", file, MonitoredTextFile.class);
  }

  public MonitoredTextFile save(MonitoredTextFile file) throws Exception {
    return getRestClient().post(API + "textedit/save", file, MonitoredTextFile.class);
  }
}
