package de.mephisto.vpin.server.converter;

import de.mephisto.vpin.restclient.converter.MediaConversionCommand;
import de.mephisto.vpin.restclient.converter.MediaOperation;
import de.mephisto.vpin.restclient.converter.MediaOperationResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "convertmedia")
public class MediaConverterResource {

  @Autowired
  private MediaConverterService mediaConverterService;

  @PostMapping("/batch")
  public MediaOperationResult convert(@RequestBody MediaOperation params) {
    return mediaConverterService.convert(params);
  }

  @GetMapping("/commands")
  public List<MediaConversionCommand> getCommandList() {
    return mediaConverterService.getCommandList();
  }
}
