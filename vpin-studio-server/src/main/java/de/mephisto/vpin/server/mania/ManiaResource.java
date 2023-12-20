package de.mephisto.vpin.server.mania;

import de.mephisto.vpin.restclient.mania.ManiaAccountRepresentation;
import de.mephisto.vpin.server.players.Player;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "mania")
public class ManiaResource {

  @Autowired
  private ManiaService vPinManiaService;

  @GetMapping("/account")
  public ManiaAccountRepresentation getAccount() {
    return toAccountRepresentation(vPinManiaService.getAccount());
  }

  @PostMapping("/account/save")
  public ManiaAccountRepresentation save(@RequestBody ManiaAccount account) {
    return toAccountRepresentation(vPinManiaService.save(account));
  }

  @DeleteMapping("/account")
  public boolean deleteAccount() {
    return vPinManiaService.deleteAccount();
  }

  @Nullable
  private ManiaAccountRepresentation toAccountRepresentation(@Nullable ManiaAccount account) {
    if (account != null) {
      ManiaAccountRepresentation accountRepresentation = new ManiaAccountRepresentation();
      accountRepresentation.setCabinetId(account.getCabinetId());
      accountRepresentation.setDisplayName(account.getDisplayName());
      accountRepresentation.setInitials(account.getInitials());
      return accountRepresentation;
    }
    return null;
  }

}
