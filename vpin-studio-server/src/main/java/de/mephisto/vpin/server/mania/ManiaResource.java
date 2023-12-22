package de.mephisto.vpin.server.mania;

import de.mephisto.vpin.connectors.mania.model.ManiaAccountRepresentation;
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
    return vPinManiaService.getAccount();
  }

  @PostMapping("/account/save")
  public ManiaAccountRepresentation save(@RequestBody ManiaAccountRepresentation account) throws Exception {
    return vPinManiaService.save(account);
  }

  @DeleteMapping("/account")
  public boolean deleteAccount() {
    return vPinManiaService.deleteAccount();
  }
}
