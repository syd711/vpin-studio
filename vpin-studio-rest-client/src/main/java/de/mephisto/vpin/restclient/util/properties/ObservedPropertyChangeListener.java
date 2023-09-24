package de.mephisto.vpin.restclient.util.properties;


import java.util.Optional;

public interface ObservedPropertyChangeListener {

  void changed(String propertiesName, String key, Optional<String> updatedValue);
}
