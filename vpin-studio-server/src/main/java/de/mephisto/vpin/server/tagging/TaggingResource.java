package de.mephisto.vpin.server.tagging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "tagging")
public class TaggingResource {

  @Autowired
  private TaggingService taggingService;

  @GetMapping
  public List<String> getTags() {
    return taggingService.getTags();
  }

}
