package de.mephisto.vpin.server.puppack;

import de.mephisto.vpin.connectors.vps.model.VpsDiffTypes;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.client.CommandOption;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.games.descriptors.JobDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptorFactory;
import de.mephisto.vpin.restclient.jobs.JobDescriptorFactory;
import de.mephisto.vpin.restclient.puppacks.PupPackRepresentation;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.*;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.util.Date;
import java.util.List;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

/**
 *
 */
@RestController
@RequestMapping(API_SEGMENT + "puppacks")
public class PupPacksResource {
  private final static Logger LOG = LoggerFactory.getLogger(PupPacksResource.class);

  @Autowired
  private PupPacksService pupPacksService;

  @Autowired
  private GameService gameService;

  @Autowired
  private GameValidationService validationService;

  @Autowired
  private UniversalUploadService universalUploadService;

  @Autowired
  private FrontendService frontendService;


  @DeleteMapping("{id}")
  public boolean delete(@PathVariable("id") int id) {
    return pupPacksService.delete(gameService.getGame(id));
  }

  @GetMapping("/menu")
  public PupPackRepresentation getPupPack() {
    PupPack pupPack = pupPacksService.getMenuPupPack();
    if (pupPack != null) {
      pupPack.load();
      return toPupPackRepresentation(null, pupPack);
    }
    return null;
  }

  @GetMapping("/{gameId}")
  public PupPackRepresentation getPupPack(@PathVariable("gameId") int gameId) {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      PupPack pupPack = pupPacksService.getPupPack(game);
      if (pupPack != null) {
        pupPack.load();
        return toPupPackRepresentation(game, pupPack);
      }
    }
    return null;
  }

  @GetMapping("/enabled/{rom}")
  public boolean enable(@PathVariable("id") int id) {
    Game game = gameService.getGame(id);
    return !pupPacksService.isPupPackDisabled(game);
  }

  @GetMapping("/set/{id}/{enable}")
  public boolean enable(@PathVariable("id") int id,
                        @PathVariable("enable") boolean enable) {
    Game game = gameService.getGame(id);
    if (game != null) {
      return pupPacksService.setPupPackEnabled(game, enable);
    }
    return false;
  }

  @GetMapping("/clearcache")
  public boolean clearCache() {
    return pupPacksService.clearCache();
  }

  @PostMapping("/option/{id}")
  public JobDescriptor option(@PathVariable("id") Integer id,
                              @RequestBody CommandOption option) {

    Game game = gameService.getGame(id);
    if (game != null) {
      return pupPacksService.option(game, option.getCommand());
    }
    return JobDescriptorFactory.empty();
  }

  @PostMapping("/upload")
  public UploadDescriptor upload(@RequestParam(value = "file", required = false) MultipartFile file) {
    UploadDescriptor descriptor = UploadDescriptorFactory.create(file);
    try {
      descriptor.upload();

      File tempFile = new File(descriptor.getTempFilename());
      UploaderAnalysis analysis = new UploaderAnalysis(frontendService.getFrontend(), tempFile);
      analysis.analyze();

      descriptor.setAsync(true);
      universalUploadService.importArchiveBasedAssets(descriptor, analysis, AssetType.PUP_PACK);

      //these ROM names can differ, see PinBlob which uses a different ROM than PUP Pack
      List<Game> gamesByRom = gameService.getKnownGames(-1);
      String romFromPupPack = analysis.getRomFromPupPack();
      String romFromZip = analysis.getRomFromArchive();
      for (Game gameByRom : gamesByRom) {
        if (!StringUtils.isEmpty(gameByRom.getRom())) {
          String gameRom = gameByRom.getRom();
          if (gameRom.equalsIgnoreCase(String.valueOf(romFromPupPack)) || gameRom.equalsIgnoreCase(String.valueOf(romFromZip))) {
            gameService.resetUpdate(gameByRom.getId(), VpsDiffTypes.pupPack);
          }
        }
      }
      return descriptor;
    }
    catch (
        Exception e) {
      LOG.error(AssetType.PUP_PACK.name() + " upload failed: " + e.getMessage(), e);
      throw new ResponseStatusException(INTERNAL_SERVER_ERROR, AssetType.PUP_PACK.name() + " upload failed: " + e.getMessage());
    }
    finally {
      descriptor.finalizeUpload();
    }
  }

  private PupPackRepresentation toPupPackRepresentation(@Nullable Game game, @NonNull PupPack pupPack) {
    PupPackRepresentation representation = new PupPackRepresentation();
    representation.setSize(pupPack.getSize());
    representation.setScriptOnly(pupPack.isScriptOnly());
    representation.setPath(pupPack.getPupPackFolder().getPath().replaceAll("\\\\", "/"));
    representation.setModificationDate(new Date(pupPack.getPupPackFolder().lastModified()));
    representation.setOptions(pupPack.getOptions());
    representation.setScreenDMDMode(pupPack.getScreenMode(VPinScreen.DMD));
    representation.setScreenBackglassMode(pupPack.getScreenMode(VPinScreen.BackGlass));
    representation.setScreenTopperMode(pupPack.getScreenMode(VPinScreen.Topper));
    representation.setScreenFullDMDMode(pupPack.getScreenMode(VPinScreen.Menu));
    representation.setMissingResources(pupPack.getMissingResources());
    representation.setSelectedOption(pupPack.getSelectedOption());
    representation.setTxtFiles(pupPack.getTxtFiles());
    representation.setName(pupPack.getName());
    representation.setHelpTransparency(pupPack.isTransparent(VPinScreen.GameHelp));
    representation.setInfoTransparency(pupPack.isTransparent(VPinScreen.GameInfo));
    representation.setOther2Transparency(pupPack.isTransparent(VPinScreen.Other2));

    if (game != null) {
      representation.setEnabled(!pupPacksService.isPupPackDisabled(game));
      representation.setValidationStates(validationService.validatePupPack(game));
    }
    return representation;
  }
}
