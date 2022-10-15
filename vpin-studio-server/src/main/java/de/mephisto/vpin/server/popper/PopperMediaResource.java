package de.mephisto.vpin.server.popper;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.system.SystemService;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static de.mephisto.vpin.server.util.RequestUtil.CONTENT_LENGTH;
import static de.mephisto.vpin.server.util.RequestUtil.CONTENT_TYPE;

@RestController
@RequestMapping(API_SEGMENT + "poppermedia")
public class PopperMediaResource {
  private final static Logger LOG = LoggerFactory.getLogger(PopperResource.class);

  @Autowired
  private GameService gameService;

  @Autowired
  private SystemService systemService;

//  @GetMapping("/{id}/{screen}")
//  public ResponseEntity<byte[]> handleRequest(@PathVariable("id") int id,
//                                              @PathVariable("screen") String screen,
//                                              @RequestHeader(value = "Range", required = false) String httpRangeList) {
//    PopperScreen popperScreen = PopperScreen.valueOf(screen);
//    Game game = gameService.getGame(id);
//    LOG.info("Requested " + game);
//
////    File screenMediaFolder = new File(systemService.getPinUPMediaFolder()+ "/Visual Pinball X", popperScreen.name());
//    File mediaFile = new File("C:\\vPinball\\PinUPSystem\\POPMedia\\Visual Pinball X\\PlayField\\AC'DC (Stern 2012).mp4");
//    return videoStreamService.prepareContent("AC'DC (Stern 2012)", "mp4", httpRangeList);
//  }

  @GetMapping("/{id}/{screen}")
  public ResponseEntity<Resource> handleRequest(@PathVariable("id") int id,
                                              @PathVariable("screen") String screen,
                                              @RequestHeader(value = "Range", required = false) String httpRangeList) throws IOException {
    PopperScreen popperScreen = PopperScreen.valueOf(screen);
    Game game = gameService.getGame(id);
    LOG.info("Requested " + game);

//    File screenMediaFolder = new File(systemService.getPinUPMediaFolder()+ "/Visual Pinball X", popperScreen.name());
    File mediaFile = new File("C:\\vPinball\\PinUPSystem\\POPMedia\\Visual Pinball X\\PlayField\\AC'DC (Stern 2012).mp4");

    byte[] bytes = IOUtils.toByteArray(new FileInputStream(mediaFile));
    ByteArrayResource bytesResource = new ByteArrayResource(bytes);

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.set(CONTENT_LENGTH, String.valueOf(mediaFile.length()));

    responseHeaders.set("Access-Control-Allow-Origin", "*");
    responseHeaders.set("Access-Control-Expose-Headers", "origin, range");
    responseHeaders.set("Cache-Control", "public, max-age=3600");
    responseHeaders.set(CONTENT_TYPE, "video/mp4");
    return ResponseEntity.ok().headers(responseHeaders).body(bytesResource);
  }

//  @GetMapping("{name}")
//  public ResponseEntity<Resource> getVideoByName(@PathVariable("name") String name){
//
//  }
}
