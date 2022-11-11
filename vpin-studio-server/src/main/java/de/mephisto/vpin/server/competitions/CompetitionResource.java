package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.RequestUtil;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "competitions")
public class CompetitionResource {

  public static final String COMPETITION_BADGES = "competition-badges";
  @Autowired
  private CompetitionService competitionService;

  @GetMapping
  public List<Competition> getCompetitions() {
    return competitionService.getCompetitions();
  }

  @GetMapping("/{id}")
  public Competition getCompetition(@PathVariable("id") int id) {
    Competition c = competitionService.getCompetition(id);
    if(c == null) {
      throw new ResponseStatusException(NOT_FOUND, "Not game found for id " + id);
    }
    return c;
  }

  @GetMapping("/badges")
  public List<String> getCompetitionBadges() {
    File folder = new File(SystemService.RESOURCES, COMPETITION_BADGES);
    File[] files = folder.listFiles((dir, name) -> name.endsWith("png"));
    return Arrays.stream(files).sorted().map(f -> FilenameUtils.getBaseName(f.getName())).collect(Collectors.toList());
  }

  @GetMapping("/badge/{name}")
  public ResponseEntity<byte[]> getBadge(@PathVariable("name") String imageName) throws Exception {
    File folder = new File(SystemService.RESOURCES, COMPETITION_BADGES);
    File[] files = folder.listFiles((dir, name) -> URLEncoder.encode(FilenameUtils.getBaseName(name)).equals(imageName));
    if (files != null) {
      return RequestUtil.serializeImage(files[0]);
    }
    return ResponseEntity.notFound().build();
  }

  @PostMapping("/save")
  public Competition save(@RequestBody Competition c) {
    return competitionService.save(c);
  }
}
