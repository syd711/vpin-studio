package de.mephisto.vpin.server.patcher;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.util.PackageUtil;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "patcher")
public class PatchingResource {
  private final static Logger LOG = LoggerFactory.getLogger(PatchingResource.class);

  @Autowired
  private PatchingService patchingService;

  @Autowired
  private GameService gameService;

  @Autowired
  private GameMediaService gameMediaService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private UniversalUploadService universalUploadService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  @PostMapping("/process")
  public UploadDescriptor processUploaded(@RequestBody UploadDescriptor uploadDescriptor) {
    Thread.currentThread().setName("Patcher Upload Thread");
    long start = System.currentTimeMillis();
    Game game = gameService.getGame(uploadDescriptor.getGameId());
    LOG.info("*********** Patching " + game.getGameDisplayName() + " ****************");
    try {
      File tempFile = new File(uploadDescriptor.getTempFilename());
      UploaderAnalysis analysis = new UploaderAnalysis(frontendService.supportPupPacks(), tempFile);
      analysis.analyze();
      analysis.setExclusions(uploadDescriptor.getExcludedFiles(), uploadDescriptor.getExcludedFiles());

      File difFile = tempFile;
      if (analysis.isArchive()) {
        String patchFile = analysis.getPatchFile();
        difFile = File.createTempFile(uploadDescriptor.getTempFilename(), ".dif");
        PackageUtil.unpackTargetFile(tempFile, difFile, patchFile);
      }

      File temporaryVpxFile = File.createTempFile(uploadDescriptor.getTempFilename(), ".dif");

      //patch table
      String error = patchingService.patch(game, difFile, temporaryVpxFile);
      if (!StringUtils.isEmpty(error)) {
        throw new UnsupportedOperationException(error);
      }

      //apply game media or replace file
      if (uploadDescriptor.getUploadType().equals(UploadType.uploadAndClone)) {
        gameMediaService.uploadAndClone(temporaryVpxFile, uploadDescriptor, analysis);
      }
      else if (uploadDescriptor.getUploadType().equals(UploadType.uploadAndReplace)) {
        File originalGameFile = game.getGameFile();
        if (originalGameFile.delete()) {
          LOG.info("Deleted existing game file {}", originalGameFile.getAbsolutePath());
          org.apache.commons.io.FileUtils.copyFile(temporaryVpxFile, originalGameFile);
          LOG.info("Replace game file {} with {}", originalGameFile.getAbsolutePath(), temporaryVpxFile.getAbsolutePath());
        }
        else {
          throw new UnsupportedOperationException("Failed to delete existing game file \"" + originalGameFile.getAbsolutePath() + "\"");
        }
      }
      else {
        throw new UnsupportedOperationException("Invalid upload type: " + uploadDescriptor.getUploadType());
      }

      //process additional assets of the archive
      if (analysis.isArchive()) {
        universalUploadService.processGameAssets(uploadDescriptor, analysis);
      }
    }
    catch (Exception e) {
      LOG.error("Processing \"" + uploadDescriptor.getTempFilename() + "\" failed: " + e.getMessage(), e);
      uploadDescriptor.setError(e.getMessage());
    }
    finally {
      uploadDescriptor.finalizeUpload();
      LOG.info("Import finished, took " + (System.currentTimeMillis() - start) + " ms.");
      gameLifecycleService.notifyGameAssetsChanged(game.getId(), AssetType.DIF, null);
    }
    LOG.info("****************************** /Patcher Finished *************************************");
    return uploadDescriptor;
  }
}
