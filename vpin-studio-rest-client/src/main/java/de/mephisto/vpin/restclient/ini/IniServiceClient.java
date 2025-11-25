package de.mephisto.vpin.restclient.ini;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.client.VPinStudioClientService;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.FileUploadProgressListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.lang.invoke.MethodHandles;

/*********************************************************************************************************************
 * Ini
 ********************************************************************************************************************/
public class IniServiceClient extends VPinStudioClientService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public IniServiceClient(VPinStudioClient client) {
    super(client);
  }

  public IniRepresentation getIniFile(int gameId) {
    return getRestClient().get(API + "ini/" + gameId, IniRepresentation.class);
  }

  public UploadDescriptor uploadIniFile(File file, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "ini/upload";
      HttpEntity upload = createUpload(file, gameId, null, AssetType.INI, listener);
      ResponseEntity<UploadDescriptor> exchange = createUploadTemplate().exchange(url, HttpMethod.POST, upload, UploadDescriptor.class);
      finalizeUpload(upload);
      return exchange.getBody();
    }
    catch (Exception e) {
      LOG.error("Ini upload failed: " + e.getMessage(), e);
      throw e;
    }
  }

  public void delete(int gameId) {
    getRestClient().delete(API + "ini/" + gameId);
  }

  public boolean save(IniRepresentation ini, int gameId) throws Exception {
    try {
      return getRestClient().post(API + "ini/save/" + gameId, ini, Boolean.class);
    }
    catch (Exception e) {
      LOG.error("Failed to save ini file: " + e.getMessage(), e);
      throw e;
    }
  }
}