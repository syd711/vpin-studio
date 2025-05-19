package de.mephisto.vpin.server.games;

import de.mephisto.vpin.restclient.games.descriptors.UploadType;
import de.mephisto.vpin.restclient.games.descriptors.UploadDescriptor;
import de.mephisto.vpin.restclient.util.UploaderAnalysis;
import de.mephisto.vpin.server.frontend.FrontendService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "games")
public class UniversalUploadResource {
  private final static Logger LOG = LoggerFactory.getLogger(UniversalUploadResource.class);

  @Autowired
  private UniversalUploadService universalUploadService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private GameMediaService gameMediaService;

  @PostMapping("/upload")
  public UploadDescriptor upload(@RequestParam(value = "file") MultipartFile file,
                                 @RequestParam(value = "gameId") int gameId,
                                 @RequestParam(value = "emuId") int emuId,
                                 @RequestParam(value = "mode") UploadType mode) {
    UploadDescriptor descriptor = universalUploadService.create(file, gameId);
    try {
      descriptor.setUploadType(mode);
      descriptor.setEmulatorId(emuId);

      descriptor.upload();
    }
    catch (Exception e) {
      LOG.error("Table upload failed: " + e.getMessage(), e);
      descriptor.setError("Table upload failed: " + e.getMessage());
      descriptor.finalizeUpload();
    }
    return descriptor;
  }

  @PostMapping("/process")
  public UploadDescriptor processUploaded(@RequestBody UploadDescriptor uploadDescriptor) {
    Thread.currentThread().setName("Universal Upload Thread");
    long start = System.currentTimeMillis();
    LOG.info("*********** Importing " + uploadDescriptor.getTempFilename() + " ************************");
    try {
      // If the file is not a real file but a pointer to an external resource, it is time to get the real file...
      universalUploadService.resolveLinks(uploadDescriptor);

      File tempFile = new File(uploadDescriptor.getTempFilename());
      UploaderAnalysis analysis = new UploaderAnalysis(frontendService.supportPupPacks(), tempFile);
      analysis.analyze();
      analysis.setExclusions(uploadDescriptor.getExcludedFiles(), uploadDescriptor.getExcludedFiles());

      if (analysis.isVpxOrFpTable()) {
        LOG.info("Importing table bundle, not media bundle.");

        String tableFileName = analysis.getTableFileName(uploadDescriptor.getOriginalUploadFileName());
        File temporaryGameFile = universalUploadService.writeTableFilenameBasedEntry(uploadDescriptor, tableFileName);
        importGame(temporaryGameFile, uploadDescriptor, analysis);
      }

      universalUploadService.processGameAssets(uploadDescriptor, analysis);
    }
    catch (Exception e) {
      LOG.error("Processing \"" + uploadDescriptor.getTempFilename() + "\" failed: " + e.getMessage(), e);
      uploadDescriptor.setError("Processing failed: " + e.getMessage());
    }
    finally {
      uploadDescriptor.finalizeUpload();
      LOG.info("Import finished, took " + (System.currentTimeMillis() - start) + " ms.");
    }
    LOG.info("****************************** /Import Finished *************************************");
    return uploadDescriptor;
  }


  private void importGame(File temporaryGameFile, UploadDescriptor uploadDescriptor, UploaderAnalysis analysis) throws Exception {
    UploadType uploadType = uploadDescriptor.getUploadType();
    switch (uploadType) {
      case uploadAndImport: {
        gameMediaService.uploadAndImport(temporaryGameFile, uploadDescriptor, analysis);
        break;
      }
      case uploadAndReplace: {
        gameMediaService.uploadAndReplace(temporaryGameFile, uploadDescriptor, analysis);
        break;
      }
      case uploadAndClone: {
        gameMediaService.uploadAndClone(temporaryGameFile, uploadDescriptor, analysis);
        break;
      }
      default: {
        throw new UnsupportedOperationException("Unmapped upload type " + uploadType);
      }
    }
  }

}
