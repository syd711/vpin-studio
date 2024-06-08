package de.mephisto.vpin.server.mame;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptorFactory;
import de.mephisto.vpin.restclient.mame.MameOptions;
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
@RequestMapping(API_SEGMENT + "mame")
public class MameResource {
  private final static Logger LOG = LoggerFactory.getLogger(MameResource.class);

  @Autowired
  private MameService mameService;

  @Autowired
  private MameRomAliasService mameRomAliasService;

  @Autowired
  private UniversalUploadService universalUploadService;

  @GetMapping("/options/{rom}")
  public MameOptions getOptions(@PathVariable("rom") String rom) {
    return mameService.getOptions(rom);
  }

  @PostMapping("/options")
  public MameOptions saveOptions(@RequestBody MameOptions options) {
    return mameService.saveOptions(options);
  }

  @GetMapping("/clearcache")
  public boolean clearCache() {
    mameRomAliasService.clearCache();
    return mameService.clearCache();
  }

  @GetMapping("/clearcachefor/{rom}")
  public boolean clearCacheFor(@PathVariable("rom") String rom) {
    mameRomAliasService.clearCache();
    return mameService.clearCacheFor(rom);
  }


  @PostMapping("/upload/rom/{emuId}")
  public UploadDescriptor uploadRom(@PathVariable("emuId") int emuId, @RequestParam(value = "file", required = false) MultipartFile file) {
    UploadDescriptor descriptor = UploadDescriptorFactory.create(file);
    descriptor.setEmulatorId(emuId);

    try {
      descriptor.getAssetsToImport().add(AssetType.ROM);
      descriptor.upload();
      universalUploadService.importArchiveBasedAssets(descriptor, null, AssetType.ROM);
      return descriptor;
    }
    catch (Exception e) {
      LOG.error(AssetType.ROM.name() + " upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, AssetType.ROM + " upload failed: " + e.getMessage());
    } finally {
      descriptor.finalizeUpload();
    }
  }


  @PostMapping("/upload/cfg/{emuId}")
  public UploadDescriptor uploadCfg(@PathVariable("emuId") int emuId, @RequestParam(value = "file", required = false) MultipartFile file) {
    UploadDescriptor descriptor = UploadDescriptorFactory.create(file);
    descriptor.setEmulatorId(emuId);

    try {
      descriptor.getAssetsToImport().add(AssetType.CFG);
      descriptor.upload();
      universalUploadService.importArchiveBasedAssets(descriptor, null, AssetType.CFG);
      return descriptor;
    }
    catch (Exception e) {
      LOG.error(AssetType.CFG.name() + " upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, AssetType.CFG + " upload failed: " + e.getMessage());
    } finally {
      descriptor.finalizeUpload();
    }
  }


  @PostMapping("/upload/nvram/{emuId}")
  public UploadDescriptor uploadNvRam(@PathVariable("emuId") int emuId, @RequestParam(value = "file", required = false) MultipartFile file) {
    UploadDescriptor descriptor = UploadDescriptorFactory.create(file);
    descriptor.setEmulatorId(emuId);

    try {
      descriptor.getAssetsToImport().add(AssetType.NV);
      descriptor.upload();
      universalUploadService.importArchiveBasedAssets(descriptor, null, AssetType.NV);
      return descriptor;
    }
    catch (Exception e) {
      LOG.error(AssetType.NV.name() + " upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, AssetType.NV + " upload failed: " + e.getMessage());
    } finally {
      descriptor.finalizeUpload();
    }
  }
}
