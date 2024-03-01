package de.mephisto.vpin.restclient.util.properties;


import java.util.Map;
import java.util.Optional;

public interface ObservedPropertyChangeListener {

  void changed(String propertiesName, String key, Optional<String> updatedValue);

  void changed(String propertiesName, Map<String, String> values);
}
