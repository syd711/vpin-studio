package de.mephisto.vpin.server.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "system")
public class SystemResource {
  private final static Logger LOG = LoggerFactory.getLogger(SystemResource.class);

  private final Date startupTime = new Date();

  @GetMapping("/startupTime")
  public Date startupTime() {
    return startupTime;
  }

  @GetMapping("/ping")
  public boolean ping() {
    return true;
  }

  @GetMapping("/logs")
  @ResponseBody
  public String logs() throws IOException {
    Path filePath = Path.of("./vpin-studio-server.log");
    return Files.readString(filePath);
  }

  @GetMapping("/restart")
  public boolean restart() {
    return true;
  }
}
