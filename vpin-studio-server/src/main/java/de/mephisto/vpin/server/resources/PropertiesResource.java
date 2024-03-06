package de.mephisto.vpin.server.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.cards.CardTemplates;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "properties")
public class PropertiesResource {
  private final static Logger LOG = LoggerFactory.getLogger(PropertiesResource.class);

  @Autowired
  private PreferencesService preferencesService;

  private static Map<String, Class> propertiesMapping = new HashMap<>();

  private static ObjectMapper objectMapper = new ObjectMapper();
  static {
    propertiesMapping.put(PreferenceNames.HIGHSCORE_CARD_TEMPLATES, CardTemplates.class);

    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }


  @GetMapping("/{properties}")
  public Map<String, Object> bundle(@PathVariable("properties") String properties) throws Exception {
    String value = (String) preferencesService.getPreferenceValue(properties);
    if(StringUtils.isEmpty(value) && properties.equals(PreferenceNames.HIGHSCORE_CARD_SETTINGS)) {
      value = new CardSettings().toJson();
      preferencesService.savePreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, value);
    }

    return new ObjectMapper().readValue(value, HashMap.class);
  }

  @PutMapping("/{properties}")
  public boolean put(@PathVariable("properties") String properties, @RequestBody Map<String, Object> values) throws Exception {
    String json = (String) preferencesService.getPreferenceValue(properties);
    Class aClass = propertiesMapping.get(properties);
    Object preference = objectMapper.readValue(json, aClass);
    BeanWrapper bean = new BeanWrapperImpl(preference);

    Iterator<Map.Entry<String, Object>> iterator = values.entrySet().iterator();
    while(iterator.hasNext()) {
      Map.Entry<String, Object> next = iterator.next();
      bean.setPropertyValue(next.getKey(), next.getValue());
    }

    String updatedJson = objectMapper.writeValueAsString(preference);
    preferencesService.savePreference(properties, updatedJson);
    return true;
  }
}
