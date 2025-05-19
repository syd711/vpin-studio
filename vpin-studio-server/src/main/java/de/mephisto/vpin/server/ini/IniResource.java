package de.mephisto.vpin.server.ini;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.ini.IniRepresentation;
import de.mephisto.vpin.server.games.UniversalUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@RestController
@RequestMapping(API_SEGMENT + "ini")
public class IniResource {
  private final static Logger LOG = LoggerFactory.getLogger(IniResource.class);

  @Autowired
  private UniversalUploadService universalUploadService;

  @Autowired
  private IniService iniService;

  @DeleteMapping("{gameId}")
  public boolean delete(@PathVariable("gameId") int gameId) {
    return iniService.delete(gameId);
  }

  @GetMapping("{gameId}")
  public IniRepresentation getIniFile(@PathVariable("gameId") int gameId) {
    try {
      return iniService.getIniFile(gameId);
    }
    catch (Exception e) {
      LOG.error("INI get failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "INI load failed: " + e.getMessage());
    }
  }

  @PostMapping("/save/{gameId}")
  public boolean save(@PathVariable("gameId") int gameId, @RequestBody IniRepresentation iniRepresentation) throws Exception {
    return iniService.save(gameId, iniRepresentation);
  }

  @PostMapping("upload")
  public UploadDescriptor iniUpload(@RequestParam(value = "file", required = false) MultipartFile file,
                                    @RequestParam("objectId") Integer gameId) {
    UploadDescriptor descriptor = universalUploadService.create(file, gameId);
    try {
      descriptor.upload();
      universalUploadService.importFileBasedAssets(descriptor, AssetType.INI);
      return descriptor;
    }
    catch (Exception e) {
      LOG.error("INI upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "INI upload failed: " + e.getMessage());
    }
    finally {
      descriptor.finalizeUpload();
    }
  }

}
