package de.mephisto.vpin.server.iscored;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "iscored")
public class IScoredResources {

  @Autowired
  private IScoredService iScoredService;

  @DeleteMapping("/{gameRoomId}")
  public boolean deleteGameRoom(@PathVariable("gameRoomId") String gameRoomId) throws Exception {
    return iScoredService.deleteGameRoom(gameRoomId);
  }
}
