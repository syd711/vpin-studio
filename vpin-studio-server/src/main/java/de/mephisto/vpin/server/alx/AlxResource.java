package de.mephisto.vpin.server.alx;

import de.mephisto.vpin.restclient.alx.AlxSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "alx")
public class AlxResource {
  @Autowired
  private AlxService analyticsService;

  @GetMapping
  public AlxSummary getAlxSummary() {
    return analyticsService.getAlxSummary();
  }

  @GetMapping("/{gameId}")
  public AlxSummary getAlxSummary(@PathVariable("gameId") int gameId) {
    return analyticsService.getAlxSummary(gameId);
  }

  @DeleteMapping("/game/numberplays/{gameId}")
  public boolean deleteNumberPlaysForGame(@PathVariable("gameId") int gameId) {
    return analyticsService.deleteNumberPlaysForGame(gameId);
  }

  @DeleteMapping("/emulator/numberplays/{emulatorId}")
  public boolean deleteNumberOfPlaysForEmulator(@PathVariable("emulatorId") int emulatorId) {
    return analyticsService.deleteNumberOfPlaysForEmulator(emulatorId);
  }

  @DeleteMapping("/game/timeplayed/{gameId}")
  public boolean deleteTimePlayedForGame(@PathVariable("gameId") int gameId) {
    return analyticsService.deleteTimePlayedForGame(gameId);
  }

  @DeleteMapping("/emulator/timeplayed/{emulatorId}")
  public boolean deleteTimePlayedForEmulator(@PathVariable("emulatorId") int emulatorId) {
    return analyticsService.deleteTimePlayedForEmulator(emulatorId);
  }

  @PutMapping("/{gameId}")
  public boolean updateAlx(@PathVariable("gameId") int gameId,
                           @RequestBody Map<String, Object> values) {
    String field = (String) values.get("dataField");
    long value = (long) values.get("value");
    if(field.equals("numberOfPlays")) {
      return analyticsService.updateNumberOfPlaysForGame(gameId, value);
    }
    else if(field.equals("timePlayed")) {
      long seconds = value * 60;
      return analyticsService.updateSecondsPlayedForGame(gameId, seconds);
    }

    return false;
  }
}
