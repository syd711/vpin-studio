package de.mephisto.vpin.server.vps;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import de.mephisto.vpin.restclient.vps.VpsInstallLink;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "vps")
public class VpsResource {

  @Autowired
  private VpsService vpsService;

  @Autowired
  private GameService gameService;

  @GetMapping("/vpsTables")
  public List<VpsTable> getTables() {
    return vpsService.getTables();
  }

  @GetMapping("/vpsTable/{extTableId}")
  public VpsTable getTableById(@PathVariable("extTableId") String extTableId) {
    return vpsService.getTableById(extTableId);
  }

  @GetMapping("/find/{rom}/{term}")
  public List<VpsTable> find(@PathVariable("term") String term, @PathVariable("rom") String rom) {
    return vpsService.find(term, rom);
  }

  @GetMapping("/update")
  public boolean update() {
    List<Game> knownGames = gameService.getKnownGames(-1);
    return vpsService.update(knownGames);
  }

  @GetMapping("/reload")
  public boolean reload() {
    return vpsService.reload();
  }

  @GetMapping("/changeDate")
  public Date getChangeDate() {
    return vpsService.getChangeDate();
  }

  @GetMapping("/installLinks/{link}")
  public List<VpsInstallLink> getInstallLinks(@PathVariable("link") String link) {
    String decodedLink = URLDecoder.decode(link, StandardCharsets.UTF_8);
    return vpsService.getInstallLinks(decodedLink);
  }

  @PostMapping("/save")
  public VpsTable save(@RequestBody VpsTable vpsTable) throws Exception {
    return vpsService.save(vpsTable);
  }
}
