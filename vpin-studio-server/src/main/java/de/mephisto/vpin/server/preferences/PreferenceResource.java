package de.mephisto.vpin.server.preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

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

  @PutMapping
  public boolean put(@RequestBody Map<String, Object> values) {
    return preferencesService.savePreference(values);
  }


  class PreferenceEntry {
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
