package de.mephisto.vpin.server.vpinmame;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.vpinmame.VPinMameOptions;
import de.mephisto.vpin.server.emulators.EmulatorService;
import de.mephisto.vpin.server.games.GameCachingService;
import de.mephisto.vpin.server.games.GameEmulator;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.games.UniversalUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.util.List;

@RestController
@RequestMapping(API_SEGMENT + "mame")
public class VPinMameResource {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  private GameService gameService;

  @Autowired
  private VPinMameService vPinMameService;

  @Autowired
  private EmulatorService emulatorService;

  @Autowired
  private UniversalUploadService universalUploadService;

  @Autowired
  private GameCachingService gameCachingService;

  @GetMapping("/dmddevice.ini") 
  public File getDmdDeviceIni() {
    return vPinMameService.getDmdDeviceIni();
  }

  @GetMapping("/setup")
  public Boolean runSetupFile() {
    return vPinMameService.runSetupExe();
  }

  @GetMapping("/flexsetup")
  public Boolean runFlexSetupFile() {
    return vPinMameService.runFlexSetupExe();
  }

  @GetMapping("/options/{rom}")
  public VPinMameOptions getOptions(@PathVariable("rom") String rom) {
    return vPinMameService.getOptions(rom);
  }

  @PostMapping("/options")
  public VPinMameOptions saveOptions(@RequestBody VPinMameOptions options) {
    VPinMameOptions updatedOptions = vPinMameService.saveOptions(options);
    List<GameEmulator> vpxGameEmulators = emulatorService.getVpxGameEmulators();
    for (GameEmulator vpxGameEmulator : vpxGameEmulators) {
      gameCachingService.invalidateByRom(vpxGameEmulator.getId(), options.getRom());
    }
    return updatedOptions;
  }

  @DeleteMapping("/options/{rom}")
  public Boolean deleteOptions(@PathVariable("rom") String rom) {
    return vPinMameService.deleteOptions(rom);
  }

  @GetMapping("/clearcache")
  public boolean clearCache() {
    return gameService.clearMameCaches();
  }

  @GetMapping("/clearcachefor/{rom}")
  public boolean clearCacheFor(@PathVariable("rom") String rom) {
    return gameService.clearMameCacheFor(rom) && gameService.clearAliasCache();
  }


  @PostMapping("/upload/rom/{emuId}")
  public UploadDescriptor uploadRom(@PathVariable("emuId") int emuId, @RequestParam(value = "file", required = false) MultipartFile file) {
    UploadDescriptor descriptor = universalUploadService.create(file);
    descriptor.setEmulatorId(emuId);

    try {
      descriptor.upload();
      universalUploadService.importArchiveBasedAssets(descriptor, null, AssetType.ROM);
      return descriptor;
    }
    catch (Exception e) {
      LOG.error("{} upload failed: {}", AssetType.ROM.name(), e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, AssetType.ROM + " upload failed: " + e.getMessage());
    } finally {
      descriptor.finalizeUpload();
    }
  }


  @PostMapping("/upload/cfg/{emuId}")
  public UploadDescriptor uploadCfg(@PathVariable("emuId") int emuId, @RequestParam(value = "file", required = false) MultipartFile file) {
    UploadDescriptor descriptor = universalUploadService.create(file);
    descriptor.setEmulatorId(emuId);

    try {
      descriptor.upload();
      universalUploadService.importArchiveBasedAssets(descriptor, null, AssetType.CFG);
      return descriptor;
    }
    catch (Exception e) {
      LOG.error("{} upload failed: {}", AssetType.CFG.name(), e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, AssetType.CFG + " upload failed: " + e.getMessage());
    } finally {
      descriptor.finalizeUpload();
    }
  }


  @PostMapping("/upload/nvram/{emuId}")
  public UploadDescriptor uploadNvRam(@PathVariable("emuId") int emuId, @RequestParam(value = "file", required = false) MultipartFile file) {
    UploadDescriptor descriptor = universalUploadService.create(file);
    descriptor.setEmulatorId(emuId);

    try {
      descriptor.upload();
      universalUploadService.importArchiveBasedAssets(descriptor, null, AssetType.NV);
      return descriptor;
    }
    catch (Exception e) {
      LOG.error("{} upload failed: {}", AssetType.NV.name(), e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, AssetType.NV + " upload failed: " + e.getMessage());
    } finally {
      descriptor.finalizeUpload();
    }
  }
}
