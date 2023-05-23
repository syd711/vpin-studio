package de.mephisto.vpin.restclient.client;

import de.mephisto.vpin.restclient.AssetType;
import de.mephisto.vpin.restclient.DirectB2SData;
import de.mephisto.vpin.restclient.FileUploadProgressListener;
import de.mephisto.vpin.restclient.representations.GameRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.File;

/*********************************************************************************************************************
 * DirectB2S
 ********************************************************************************************************************/
public class DirectB2S extends AbstractStudioClientModule{
  private final static Logger LOG = LoggerFactory.getLogger(VPinStudioClient.class);

  DirectB2S(VPinStudioClient client) {
    super(client);
  }

  public ByteArrayInputStream getDefaultPicture(GameRepresentation game) {
    byte[] bytes = getRestClient().readBinary(API + "assets/defaultbackground/" + game.getId());
    if(bytes == null) {
      LOG.error("Failed to read image, using empty bytes.");
      bytes = new byte[]{};
    }
    return new ByteArrayInputStream(bytes);
  }

  public DirectB2SData getDirectB2SData(int gameId) {
    return getRestClient().get(API + "directb2s/" + gameId, DirectB2SData.class);
  }

  public boolean uploadDirectB2SFile(File file, String uploadType, int gameId, FileUploadProgressListener listener) throws Exception {
    try {
      String url = getRestClient().getBaseUrl() + API + "directb2s/upload";
      new RestTemplate().exchange(url, HttpMethod.POST, createUpload(file, gameId, uploadType, AssetType.DIRECT_B2S, listener), Boolean.class);
      return true;
    } catch (Exception e) {
      LOG.error("Directb2s upload failed: " + e.getMessage(), e);
      throw e;
    }
  }
}
