package de.mephisto.vpin.server.textedit;

import de.mephisto.vpin.restclient.textedit.MonitoredTextFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "textedit")
public class TextEditResource {

  @Autowired
  private TextEditService textEditService;

  @PostMapping("/open")
  @ResponseBody
  public MonitoredTextFile getText(@RequestBody MonitoredTextFile monitoredTextFile) {
    try {
      return textEditService.getText(monitoredTextFile);
    }
    catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Reading file failed: " + e.getMessage());
    }
  }

  @PostMapping("/save")
  public MonitoredTextFile save(@RequestBody MonitoredTextFile monitoredTextFile) {
    return textEditService.save(monitoredTextFile);
  }
}
