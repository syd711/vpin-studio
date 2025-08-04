package de.mephisto.vpin.server.mediasources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class MediaSourcesService {
  private final static Logger LOG = LoggerFactory.getLogger(MediaSourcesService.class);

  public List<MediaSource> getMediaSource(long sourceId) {
    return null;
  }

  public List<MediaSource> getMediaSources() {
    return Collections.emptyList();
  }

  public boolean deleteMediaSource(long id) {
    return false;
  }

  public MediaSource save(MediaSource mediaSource) {
    return null;
  }
}
