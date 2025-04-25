package de.mephisto.vpin.server.textedit;

import de.mephisto.vpin.restclient.textedit.TextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "textedit")
public class TextEditResource {

  @Autowired
  private TextEditService textEditService;

  @PostMapping("/open")
  @ResponseBody
  public TextFile getText(@RequestBody TextFile textFile) {
    try {
      return textEditService.getText(textFile);
    }
    catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Reading file failed: " + e.getMessage());
    }
  }

  @PostMapping("/save")
  public TextFile save(@RequestBody TextFile textFile) {
    return textEditService.save(textFile);
  }
}
