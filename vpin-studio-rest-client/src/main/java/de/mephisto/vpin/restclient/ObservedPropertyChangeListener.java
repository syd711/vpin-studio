package de.mephisto.vpin.restclient;


import java.util.Optional;

public interface ObservedPropertyChangeListener {

  void changed(String propertiesName, String key, Optional<String> updatedValue);
}
