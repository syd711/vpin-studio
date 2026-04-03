package de.mephisto.vpin.server.nvrams;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 */
@Service
public class NVRamMapSuperhacService {
  private final static Logger LOG = LoggerFactory.getLogger(NVRamMapService.class);

  // the root where map file can be downloaded
  public String mapRoot = "https://github.com/superhac/pinmame-score-parser/releases/download/v1.0.0/roms.json";

  private Map<String, ?> _cacheMapForRom;


  public List<String> getSupportedNVRams() {
    try {
      ensureCacheMapForRom();
      return new ArrayList<>(_cacheMapForRom.keySet());
    }
    catch (IOException ioe) {
      LOG.error("Cannot get supported NVRams: {}", ioe.getMessage());
      return Collections.emptyList();
    }
  }

  private void ensureCacheMapForRom() throws IOException {
    if (_cacheMapForRom == null) {
      String indexUrl = mapRoot;
      LOG.info("Load cache of rom map from {}", indexUrl);

      _cacheMapForRom  = download(indexUrl, in -> {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(in, new TypeReference<Map<String, ?>>() {});
      });
      LOG.info("Rom Cache loaded with {} roms", _cacheMapForRom.size());
    }
  }


  public <T> T download(String u, ProcessStream<T> consumer) throws IOException {
    HttpURLConnection connection = null;
    try {
      URL url = new URL(u);
      connection = (HttpURLConnection) url.openConnection();
      int code = connection.getResponseCode();
      if (code==200) {
        InputStream in = url.openStream();
        return consumer.process(in);
      }
      return null;
    }
    finally {
      if (connection != null) {
        connection.disconnect();
      }
    }
  }

  @FunctionalInterface
  public static interface ProcessStream<T> {
    public T process(InputStream in) throws IOException;
  }

}
