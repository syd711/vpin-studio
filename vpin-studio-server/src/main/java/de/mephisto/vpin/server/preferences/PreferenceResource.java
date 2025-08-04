package de.mephisto.vpin.server.preferences;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.server.util.UploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "preferences")
public class PreferenceResource {
  private final static Logger LOG = LoggerFactory.getLogger(PreferenceResource.class);

  @Autowired
  private PreferencesService preferencesService;

  @GetMapping("/{key}")
  public PreferenceEntry get(@PathVariable("key") String key) {
    Object preferenceValue = preferencesService.getPreferenceValue(key);
    if (preferenceValue == null) {
      return new PreferenceEntry(key, null);
    }
    return new PreferenceEntry(key, String.valueOf(preferenceValue));
  }

  @GetMapping("/json/{key}")
  public JsonSettings getJson(@PathVariable("key") String key) {
    try {
      return preferencesService.getJsonPreference(key);
    } 
    catch (Exception e) {
      LOG.error("Failed to read JSON preferences: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Failed to read JSON preferences: " + e.getMessage());
    }
  }

  @PostMapping(value = "/avatar")
  public Boolean upload(@RequestPart(value = "file", required = false) MultipartFile file) throws IOException {
    if (file == null) {
      LOG.error("Upload request did not contain a file object.");
      return false;
    }

    byte[] crop = UploadUtil.resizeImageUpload(file, 300);
    String mimeType = "image/jpg";
    if (file.getOriginalFilename().toLowerCase().endsWith(".png")) {
      mimeType = "image/png";
    }
    preferencesService.saveAvatar(crop, mimeType);
    return true;
  }

  @PutMapping
  public boolean put(@RequestBody Map<String, Object> values) throws Exception {
    return preferencesService.savePreferenceMap(values);
  }

  @PutMapping(value = "/json/{key}")
  public boolean put(@PathVariable("key") String key, @RequestBody Map<String, Object> values) throws Exception {
    String json = (String) values.get("data");
    return preferencesService.savePreference(key, json, false);
  }

  static class PreferenceEntry {
    private final String key;
    private final String value;

    PreferenceEntry(String key, String value) {
      this.key = key;
      this.value = value;
    }

    public String getKey() {
      return key;
    }

    public String getValue() {
      return value;
    }
  }
}
