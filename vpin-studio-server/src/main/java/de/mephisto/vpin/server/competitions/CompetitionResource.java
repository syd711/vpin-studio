package de.mephisto.vpin.server.competitions;

import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.highscores.ScoreList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping(API_SEGMENT + "competitions")
public class CompetitionResource {

  @Autowired
  private CompetitionService competitionService;

  @GetMapping
  public List<Competition> getCompetitions() {
    return competitionService.getCompetitions();
  }

  @GetMapping("/{id}")
  public Competition getCompetition(@PathVariable("id") int id) {
    Competition c = competitionService.getCompetition(id);
    if (c != null) {
      return c;
    }
    throw new ResponseStatusException(NOT_FOUND, "Not competition found for id " + id);
  }


  @GetMapping("/game/{id}")
  public List<Competition> existsForGame(@PathVariable("id") int id) {
    return competitionService.findCompetitionForGame(id);
  }

  @GetMapping("/finished/{limit}")
  public List<Competition> getFinishedCompetitions(@PathVariable("limit") int limit) {
    return competitionService.getFinishedCompetitions(limit);
  }

  @GetMapping("/active")
  public List<Competition> getActiveOffCompetition() {
    return competitionService.getActiveCompetitions();
  }

  @GetMapping("/scores/{id}")
  public ScoreList getScore(@PathVariable("id") int id) {
    return competitionService.getCompetitionScores(id);
  }

  @PutMapping("/finish/{id}")
  public boolean save(@PathVariable("id") int id) {
    return competitionService.finishCompetition(id) != null;
  }

  @PostMapping("/save")
  public Competition save(@RequestBody Competition c) {
    return competitionService.save(c);
  }

  @DeleteMapping("/{id}")
  public void deleteCompetition(@PathVariable("id") int id) {
    competitionService.delete(id);
  }

}
