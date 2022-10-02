package de.mephisto.vpin.server.assets;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.mephisto.vpin.server.rest.EntityModel;
import de.mephisto.vpin.server.rest.RestClient;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Asset extends EntityModel<Asset> {
  private final static Logger LOG = LoggerFactory.getLogger(Asset.class);

  private String mimeType;

  @JsonIgnore
  private byte[] data;

  @JsonIgnore
  private Map<String, String> urls = new HashMap<>();

  public String getMimeType() {
    return mimeType;
  }

  @Nullable
  @JsonIgnore
  public String getMimeTypeSuffix() {
    if(mimeType.contains("/")) {
      return mimeType.substring(mimeType.indexOf("/")+1);
    }
    return null;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public byte[] getData() {
    return data;
  }

  private String url;
  private String name;
  private String uuid;

  public Object deserialize() {
    if(data == null) {
      String url = RestClient.getInstance().getBaseUrl() + getId() + "/data";
      data = RestClient.getInstance().readBinary(url);

      if(data == null) {
        return null;
      }
    }

    try {
      if(mimeType.startsWith("image")) {
        return ImageIO.read(new ByteArrayInputStream(this.getData()));
      }
    } catch (IOException e) {
      LOG.error("Failed to create image from byte array: " + e.getMessage(), e);
    }

    throw new UnsupportedOperationException("Unsupported asset type " + getMimeType());
  }

  public Asset upload() {
    if(data == null || data.length == 0) {
      return this;
    }

    LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

    ByteArrayResource contentsAsResource = new ByteArrayResource(getData()) {
      @Override
      public String getFilename() {
        return getName();
      }
    };
    map.add("file", contentsAsResource);
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.MULTIPART_FORM_DATA);

    HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
    String url = getId() + "/upload";

    Asset exchange = RestClient.getInstance().exchange(url, HttpMethod.POST, requestEntity, Asset.class);
    if(exchange != null) {
      return exchange;
    }
    LOG.error("Failed to upload picture " + url + ", check server log for details.");
    return null;
  }

  public Map<String, String> getUrls() {
    return urls;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }
}
