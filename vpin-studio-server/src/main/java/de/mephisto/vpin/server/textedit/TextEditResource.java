package de.mephisto.vpin.server.textedit;

import de.mephisto.vpin.restclient.textedit.TextFile;
import de.mephisto.vpin.restclient.textedit.VPinFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "textedit")
public class TextEditResource {

  @Autowired
  private TextEditService textEditService;

  @GetMapping("/{file}")
  @ResponseBody
  public TextFile getText(@PathVariable("file") VPinFile file) {
    return textEditService.getText(file);
  }

  @PutMapping("/{file}")
  public TextFile save(@PathVariable("file") VPinFile file, @RequestBody Map<String, Object> values) {
    return textEditService.save(file, (String)values.get("text"));
  }
}
