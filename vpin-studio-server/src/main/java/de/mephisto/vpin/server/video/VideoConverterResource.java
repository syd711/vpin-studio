package de.mephisto.vpin.server.video;

import de.mephisto.vpin.restclient.video.VideoConversionCommand;
import de.mephisto.vpin.restclient.video.VideoOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "convertvideo")
public class VideoConverterResource {

  @Autowired
  private VideoConverterService videoConverterService;

  @PostMapping("/batch")
  public String getGamesFiltered(@RequestBody VideoOperation params) {
    return videoConverterService.convert(params);
  }

  @GetMapping("/commands")
  public List<VideoConversionCommand> getCommandList() {
    return videoConverterService.getCommandList();
  }
}
