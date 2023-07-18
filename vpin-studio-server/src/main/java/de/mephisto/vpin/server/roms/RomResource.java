package de.mephisto.vpin.server.roms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "rom")
public class RomResource {
  private final static Logger LOG = LoggerFactory.getLogger(RomResource.class);

  @Autowired
  private RomService romService;

  @PutMapping("/mapping")
  public boolean saveAliasMapping(@RequestBody Map<String, Object> values) {
    try {
      return romService.saveAliasMapping(values);
    } catch (IOException e) {
      LOG.error("Saving alias mapping failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Saving alias mapping failed: " + e.getMessage());
    }
  }

  @DeleteMapping("/mapping/{alias}")
  public boolean saveAliasMapping(@PathVariable("alias") String alias) {
    try {
      return romService.deleteAliasMapping(alias);
    } catch (IOException e) {
      LOG.error("Saving alias mapping failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Delete alias mapping failed: " + e.getMessage());
    }
  }

  @GetMapping("/clearcache")
  public boolean clearCache() {
    return romService.clearCache();
  }
}
