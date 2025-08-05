package de.mephisto.vpin.server.mediasources;

import de.mephisto.vpin.restclient.mediasources.MediaSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "mediasources")
public class MediaSourcesResource {
  private final static Logger LOG = LoggerFactory.getLogger(MediaSourcesResource.class);

  @Autowired
  private MediaSourcesService mediaSourcesService;

  @GetMapping
  public List<MediaSource> getSources() {
    return mediaSourcesService.getMediaSources();
  }

  @GetMapping("/{sourceId}")
  public MediaSource getMediaSource(@PathVariable("sourceId") String sourceId) {
    return mediaSourcesService.getMediaSource(sourceId);
  }

  @DeleteMapping("/{sourceId}")
  public boolean deleteMediaSource(@PathVariable("sourceId") String sourceId) throws Exception {
    return mediaSourcesService.deleteMediaSource(sourceId);
  }

  @PostMapping("/save")
  public MediaSource save(@RequestBody MediaSource mediaSource) throws Exception {
    return mediaSourcesService.save(mediaSource);
  }
}
