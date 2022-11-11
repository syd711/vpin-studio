package de.mephisto.vpin.server.generators;

import de.mephisto.vpin.server.directb2s.DirectB2SService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.highscores.Highscore;
import de.mephisto.vpin.server.highscores.HighscoreService;
import de.mephisto.vpin.server.popper.PopperScreen;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.Config;
import de.mephisto.vpin.server.util.ImageUtil;
import de.mephisto.vpin.server.util.RequestUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static de.mephisto.vpin.server.VPinStudioServer.API_SEGMENT;

@RestController
@RequestMapping(API_SEGMENT + "generator")
public class GeneratorResource {
  private final static Logger LOG = LoggerFactory.getLogger(GeneratorResource.class);

  public final static File GENERATED_OVERLAY_FILE = new File(SystemService.RESOURCES, "overlay.jpg");

  @Autowired
  private GameService gameService;

  @Autowired
  private HighscoreService highscoreService;

  @Autowired
  private DirectB2SService directB2SService;

  @Autowired
  private SystemService systemService;

  @GetMapping("/overlay")
  public ResponseEntity<byte[]> generateOverlay() throws Exception {
    onOverlayGeneration();
    return RequestUtil.serializeImage(GENERATED_OVERLAY_FILE);
  }

  @GetMapping("/card/{gameId}")
  public ResponseEntity<byte[]> generateCard(@PathVariable("gameId") int gameId) throws Exception {
    if (onCardGeneration(gameId, true)) {
      return RequestUtil.serializeImage(getCardSampleFile());
    }

    return RequestUtil.serializeImage(new File(SystemService.RESOURCES, "empty-preview.png"));
  }

  @GetMapping("/cards/{gameId}")
  public boolean generateCards(@PathVariable("gameId") int gameId) throws Exception {
    return onCardGeneration(gameId, false);
  }

  @GetMapping("/backgrounds")
  public List<String> getBackgrounds() {
    File folder = new File(SystemService.RESOURCES, "backgrounds");
    File[] files = folder.listFiles((dir, name) -> name.endsWith("jpg") || name.endsWith("png"));
    return Arrays.stream(files).sorted().map(f -> FilenameUtils.getBaseName(f.getName())).collect(Collectors.toList());
  }

  @GetMapping("/background/{name}")
  public ResponseEntity<byte[]> getBackground(@PathVariable("name") String imageName) throws Exception {
    File folder = new File(SystemService.RESOURCES, "backgrounds");
    File[] files = folder.listFiles((dir, name) -> URLEncoder.encode(FilenameUtils.getBaseName(name)).equals(imageName));
    if (files != null) {
      return RequestUtil.serializeImage(files[0]);
    }
    return ResponseEntity.notFound().build();
  }

  @PostMapping("/directb2supload")
  public Boolean directb2supload(@RequestParam("file") MultipartFile file,
                                 @RequestParam("uploadType") String uploadType,
                                 @RequestParam("gameId") int gameId) {
//    String name = file.getOriginalFilename().replaceAll("/", "").replaceAll("\\\\", "");
//    File out = new File(systemService.getDirectB2SFolder(), name);
//    if (uploadType.equals("generator")) {
//      Game game = gameService.getGame(gameId);
//      String directb2sFilename = FilenameUtils.getBaseName(game.getGameFileName()) + ".directb2s";
//      out = new File(systemService.getVPXTablesFolder(), directb2sFilename);
//    }
//
//    out.mkdirs();
//    LOG.info("Uploading " + out.getAbsolutePath());
//    return upload(file, out);
    return false;
  }

  @PostMapping(value = "/backgroundupload")
  public Boolean upload(HttpServletRequest request) {
    try {
      boolean isMultipart = ServletFileUpload.isMultipartContent(request);
      DiskFileItemFactory factory = new DiskFileItemFactory();
      factory.setRepository(new File(System.getProperty("java.io.tmpdir")));
      factory.setSizeThreshold(1024 * 1024 * 500);
      factory.setFileCleaningTracker(null);

      ServletFileUpload upload = new ServletFileUpload(factory);
      List items = upload.parseRequest(request);
      LOG.info("Upload has " + items.size() + " items.");

      Iterator iter = items.iterator();
      while (iter.hasNext()) {
        FileItem item = (FileItem) iter.next();
        if (!item.isFormField()) {
          String name = item.getName();
          File backgroundsFolder = new File(SystemService.RESOURCES, "backgrounds");
          File out = new File(backgroundsFolder, name);
          return upload(item, out);
        }
      }
    } catch (Exception e) {
      LOG.error("Upload failed: " + e.getMessage(), e);
    }
    return false;
  }

  private Boolean upload(FileItem file, File target) {
    try {
      byte[] bytes = IOUtils.toByteArray(file.getInputStream());
      FileOutputStream fileOutputStream = new FileOutputStream(target);
      IOUtils.write(bytes, fileOutputStream);
      fileOutputStream.close();
    } catch (Exception e) {
      LOG.error("Failed to store asset: " + e.getMessage() + ", byte size was " + file.getSize(), e);
    }
    return true;
  }

  private BufferedImage onOverlayGeneration() throws Exception {
    try {
      BufferedImage bufferedImage = new OverlayGraphics(gameService, highscoreService).draw();
      ImageUtil.write(bufferedImage, GENERATED_OVERLAY_FILE);
      return bufferedImage;
    } catch (Exception e) {
      LOG.error("Failed to generate overlay: " + e.getMessage(), e);
      throw e;
    }
  }

  private boolean onCardGeneration(int gameId, boolean generateSampleCard) throws Exception {
    try {
      Game game = gameService.getGame(gameId);
      Highscore highscore = highscoreService.getHighscore(game);
      if (highscore != null && highscore.getRaw() != null) {
        Config.getCardGeneratorConfig().reload();

        BufferedImage bufferedImage = new CardGraphics(directB2SService, game, highscore).draw();
        if (bufferedImage != null) {
          if (generateSampleCard) {
            ImageUtil.write(bufferedImage, getCardSampleFile());
            return true;
          }
          else {
            File highscoreCard = getCardFile(game);
            ImageUtil.write(bufferedImage, highscoreCard);
            return true;
          }
        }
      }
    } catch (Exception e) {
      LOG.error("Failed to generate overlay: " + e.getMessage(), e);
      throw e;
    }
    return false;
  }

  private File getCardSampleFile() {
    return new File(SystemService.RESOURCES, "highscore-card-sample.png");
  }

  @NonNull
  private File getCardFile(@NonNull Game game) {
    PopperScreen screen = PopperScreen.valueOf(Config.getCardGeneratorConfig().getString("popper.screen", PopperScreen.Other2.name()));
    File mediaFolder = game.getEmulator().getPinUPMediaFolder(screen);
    return new File(mediaFolder, FilenameUtils.getBaseName(game.getGameFileName()) + ".png");
  }
}
