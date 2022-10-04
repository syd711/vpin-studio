package de.mephisto.vpin.server.resources;

import com.google.common.collect.Maps;
import de.mephisto.vpin.server.util.Config;
import de.mephisto.vpin.server.util.PropertiesStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "properties")
public class PropertiesResource {
  private final static Logger LOG = LoggerFactory.getLogger(PropertiesResource.class);

  @GetMapping("/{properties}")
  public Map<String, String> bundle(@PathVariable("properties") String properties){
    PropertiesStore store = Config.getConfig(properties);
    if(store == null) {
      LOG.error("No properties found for name " + properties);
      return Collections.emptyMap();
    }

    return Maps.newHashMap(Maps.fromProperties(store.getProperties()));
  }

  @PutMapping("/{properties}")
  public boolean put(@PathVariable("properties") String properties, @RequestBody Map<String,String> values) {
    PropertiesStore store = Config.getConfig(properties);
    if(store == null) {
      LOG.error("No properties found for name " + properties);
      return false;
    }
    store.set(values);
    return true;
  }
}
