package de.mephisto.vpin.server.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.directb2s.DirectB2SData;
import de.mephisto.vpin.restclient.directb2s.DirectB2STableSettings;
import de.mephisto.vpin.restclient.frontend.FrontendMedia;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.restclient.util.FileUtils;
import de.mephisto.vpin.restclient.util.MimeTypeUtil;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.directb2s.BackglassService;
import de.mephisto.vpin.server.directb2s.DirectB2SImageExporter;
import de.mephisto.vpin.server.directb2s.DirectB2SImageRatio;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameAssetChangedEvent;
import de.mephisto.vpin.server.games.GameDataChangedEvent;
import de.mephisto.vpin.server.games.GameDataChangedListener;
import de.mephisto.vpin.server.games.GameLifecycleService;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.resources.ResourceLoader;
import de.mephisto.vpin.commons.fx.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@Service
public class DefaultPictureService implements ApplicationListener<ApplicationReadyEvent>, InitializingBean, GameDataChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(DefaultPictureService.class);

  @Autowired
  private SystemService systemService;

  @Autowired
  private BackglassService backglassService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private PupPacksService pupPackService;

  @Autowired
  private GameLifecycleService gameLifecycleService;

  public void updateGame(Game game) {
    if (game != null) {
      deleteDefaultPictures(game);
      extractDefaultPicture(game);
    }
  }

  /**
   * extracting preview need rom so need to get Game from GameService
   */
  public byte[] getPicture(Game game, VPinScreen onScreen) {
    if (VPinScreen.BackGlass.equals(onScreen)) {
      File preview = getDefaultPreview(game);
      if (!preview.exists()) {
        extractDefaultPicture(game, preview, true);
      }
      return extractBytes(preview);
    }
    else if (VPinScreen.Menu.equals(onScreen)) {
      File dmd = getDMDPicture(game);
      if (!dmd.exists()) {
        extractDmd(game, dmd);
      }
      return extractBytes(dmd);
    }
    // else other cases
    return null;
  }

  public void extractDefaultPicture(@NonNull Game game) {
    File target = getRawDefaultPicture(game);
    extractDefaultPicture(game, target, false);
  }

  private void extractDefaultPicture(@NonNull Game game, File target, boolean usePreview) {
    if (!target.getParentFile().exists() && !target.getParentFile().mkdirs()) {
      LOG.error("Failed to create raw default picture folder: " + target.getParentFile().getAbsolutePath());
    }

    // extract Preview with frame, no grill is hidden...
    if (usePreview) {
      DirectB2STableSettings tableSettings = backglassService.getTableSettings(game);
      if (tableSettings == null || !tableSettings.isHideB2SBackglass()) {
        DirectB2SData tableData = backglassService.getDirectB2SData(game);
        byte[] img = backglassService.getPreviewBackground(tableData, game, true);
        if (img != null) {
          try {
            Files.write(target.toPath(), img);
            return;
          }
          catch (IOException ioe) {
            LOG.error("Failed to extract preview image", ioe);
          }
        }
      }
    }
    // extract Raw images
    else {
      if (game.getDirectB2SFile().exists()) {
        try {
          String b2sFilename = FilenameUtils.removeExtension(game.getGameFileName()) + ".directb2s";
          String data = backglassService.getBackgroundBase64(game.getEmulatorId(), b2sFilename);
          DirectB2SImageExporter.export(target, data);
        }
        catch (VPinStudioException e) {
          LOG.error("Failed to extract background image: " + e.getMessage(), e);
        }
      }
    }

    FrontendMediaItem backGlassItem = frontendService.getDefaultMediaItem(game, VPinScreen.BackGlass);
    if (extractFromFrontendMedia(backGlassItem, target)) {
      addWatermark(target, "from " + frontendService.getFrontendName());
      return;
    }

    if (pupPackService.hasPupPack(game)) {
      pupPackService.exportDefaultPicture(game, target);
      addWatermark(target, "from PupPack");
      return;
    }

    // no more idea :)
  }

  private void extractDmd(Game game, File target) {
    // use B2S DMD image if present and not hidden
    DirectB2STableSettings tableSettings = backglassService.getTableSettings(game);
    if (tableSettings == null || !tableSettings.isHideB2SDMD()) {
      byte[] img = backglassService.getPreviewDmd(game);
      if (img != null) {
        try {
          Files.write(target.toPath(), img);
          return;
        }
        catch (IOException ioe) {
          LOG.error("Failed to extract DMD image", ioe);
        }
      }
    }
    //else
    TableDetails tableDetails = frontendService.getTableDetails(game.getId());
    String keepDisplays = tableDetails != null ? tableDetails.getKeepDisplays() : null;
    if (StringUtils.isNotEmpty(keepDisplays)) {
      boolean keepFullDmd = VPinScreen.keepDisplaysContainsScreen(keepDisplays, VPinScreen.Menu);
      if (keepFullDmd) {
        FrontendMedia frontendMedia = frontendService.getGameMedia(game.getId());
        FrontendMediaItem item = frontendMedia.getDefaultMediaItem(VPinScreen.Menu);
        if (extractFromFrontendMedia(item, target)) {
          addWatermark(target, "from " + frontendService.getFrontendName());
          return;
        }
      }
    }
  }

  public boolean extractFromFrontendMedia(FrontendMediaItem item, File target) {
    if (item != null && item.getFile().exists()) {
      String baseType = MimeTypeUtil.determineBaseType(item.getMimeType());
      if ("image".equals(baseType)) {
        try {
          org.apache.commons.io.FileUtils.copyFile(item.getFile(), target);
          return true;
        }
        catch (IOException e) {
          LOG.error("Failed to copy resource file as background: " + e.getMessage(), e);
        }
      }
      else if ("video".equals(baseType)) {
        if (JCodec.export(item.getFile(), target)) {
          return true;
        }
      }
    }
    return false;
  }

  private void addWatermark(File target, String watermark) {
    try {
      ImageUtil.drawWatermark(target, watermark, Color.CYAN);
    }
    catch (IOException ioe) {
      LOG.warn("Cannot add watermark on {}", target.getAbsolutePath());
    }
  }

  private byte[] extractBytes(File file) {
    try {
      if (file.exists()) {
        return Files.readAllBytes(file.toPath());
      }
    }
    catch (IOException e) {
      LOG.error("Cannot read image file", e);
    }
    return null;
  }

  public boolean deleteDefaultPictures(@NonNull Game game) {
    boolean success = true;
    if (!FileUtils.delete(getRawDefaultPicture(game))) {
      success = false;
    }
    if (!FileUtils.delete(getDefaultPreview(game))) {
      success = false;
    }
    return success;
  }

  public boolean deleteAllPictures(@NonNull Game game) {
    boolean success = deleteDefaultPictures(game);
    if (!FileUtils.delete(getDMDPicture(game))) {
      success = false;
    }
    return success;
  }

  @Nullable
  public BufferedImage generateCompetitionBackgroundImage(@NonNull Game game, int cropWidth, int cropHeight) {
    try {
      File backgroundImageFile = getRawDefaultPicture(game);
      if (!backgroundImageFile.exists()) {
        extractDefaultPicture(game, backgroundImageFile, false);
      }

      if (!backgroundImageFile.exists()) {
        return null;
      }

      BufferedImage image = ImageIO.read(backgroundImageFile);

      if (image.getWidth() < image.getHeight()) {
        image = ImageUtil.crop(image, DirectB2SImageRatio.RATIO_16X9.getXRatio(), DirectB2SImageRatio.RATIO_16X9.getYRatio());
      }

      BufferedImage resized = ImageUtil.resizeImage(image, cropWidth);
      LOG.info("Resized to " + resized.getWidth() + "x" + resized.getHeight());
      if (resized.getHeight() < cropHeight) {
        resized = ImageUtil.crop(resized, DirectB2SImageRatio.RATIO_16X9.getXRatio(), DirectB2SImageRatio.RATIO_16X9.getYRatio());
      }

      BufferedImage crop = resized.getSubimage(0, 0, cropWidth, cropHeight);
      BufferedImage blurred = ImageUtil.blurImage(crop, 8);

      Color start = new Color(0f, 0f, 0f, .1f);
      Color end = Color.decode("#111111");
      ImageUtil.gradient(blurred, cropWidth, cropHeight, start, end);
      return blurred;
    }
    catch (Exception e) {
      LOG.warn("Error creating competition image for " + game.getGameDisplayName() + ": " + e.getMessage(), e);
    }
    return null;
  }

  public boolean isMediaIndexAvailable() {
    return systemService.getRawImageExtractionFolder().exists()
        && !org.apache.commons.io.FileUtils.listFiles(systemService.getRawImageExtractionFolder(), null, false).isEmpty();
  }

  //-------------------------

  @NonNull
  @JsonIgnore
  public File getDefaultPreview(Game game) {
    return new File(systemService.getRawImageExtractionFolder(), game.getId() + "_" + SystemService.PREVIEW);
  }

  @NonNull
  @JsonIgnore
  public File getDMDPicture(Game game) {
    return new File(systemService.getRawImageExtractionFolder(), game.getId() + "_" + SystemService.DMD);
  }

  @NonNull
  @JsonIgnore
  public File getRawDefaultPicture(Game game) {
    return new File(systemService.getRawImageExtractionFolder(), game.getId() + "_" + SystemService.DEFAULT_BACKGROUND);
  }

  public byte[] generateAvatarImage(@Nullable byte[] data) {
    try {
      BufferedImage frameImage = ResourceLoader.getResource("logo-500.png");
      if (data == null || data.length == 0) {
        return ImageUtil.toBytes(frameImage);
      }

      Graphics g = frameImage.getGraphics();
      ByteArrayInputStream avatar = new ByteArrayInputStream(data);
      BufferedImage avatarImage = ImageIO.read(avatar);

      Ellipse2D ellipse = new Ellipse2D.Float();
      ellipse.setFrame(40, 40, 420, 420);
      g.setClip(ellipse);

      g.drawImage(avatarImage, 0, 0, frameImage.getWidth(), frameImage.getHeight(), null);

      g.dispose();

      return ImageUtil.toBytes(frameImage);
    }
    catch (Exception e) {
      LOG.error("Failed to generate avatar image: {}", e.getMessage());
      try {
        BufferedImage avatar = ResourceLoader.getResource("avatar-default.png");
        return ImageUtil.toBytes(avatar);
      }
      catch (IOException ex) {
        LOG.error("Failed to generate alternative avatar image: {}", e.getMessage());
      }
    }
    return null;
  }


  public File getManufacturerPicture(String manufacturer, Integer year, boolean useYear) {
    if (StringUtils.isEmpty(manufacturer)) {
      return null;
    }

    String manufacturerFilter = cleanName(manufacturer);
    File folder = new File(SystemService.RESOURCES, "manufacturers");
    File[] files = folder.listFiles((dir, name) -> cleanName(name).startsWith(manufacturerFilter));
    File preferred = null;
    int prefscore = -1;
    for (File f : files) {
      int score = 0;

      //String extension = FilenameUtils.getExtension(f.getName()).toLowerCase();
      String filename = FilenameUtils.removeExtension(f.getName()).trim();
      int start = filename.indexOf("(");
      if (start > 0 && filename.endsWith(")") && year != null && year > 0) {
        // if years are not used, just ignore the image as it contains year info
        if (useYear) {
          int pos = filename.indexOf("-");
          String yearStr = Integer.toString(year);
          String fromYear = StringUtils.substring(filename, start + 1, pos);
          String toYear = StringUtils.substring(filename, pos + 1, -1);
          if (StringUtils.isEmpty(fromYear) || fromYear.compareTo(yearStr) <= 0) {
            if (StringUtils.isEmpty(toYear) || toYear.compareTo(yearStr) >= 0) {
              score += 2;
            }
          }
        }
      }
      else {
        // when no year specified, prefer generic name
        score += StringUtils.equalsIgnoreCase(manufacturer, filename) ? 2 : 1;
      }

      if (score > prefscore) {
        prefscore = score;
        preferred = f;
      }
    }

    return preferred;
  }

  private String cleanName(String name) {
    String ret = name.toLowerCase();
    ret = StringUtils.remove(ret, " ");
    ret = StringUtils.remove(ret, ".");
    return ret;
  }

  //---------------------------------------------------

  @Override
  public void gameDataChanged(@NonNull GameDataChangedEvent changedEvent) {
    // not needed
  }

  @Override
  public void gameAssetChanged(@NonNull GameAssetChangedEvent changedEvent) {
    if (AssetType.DIRECTB2S.equals(changedEvent.getAssetType())) {
      // not that clean but sufficient so save access to Game
      Game game = new Game();
      game.setId(changedEvent.getGameId());
      deleteAllPictures(game);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    gameLifecycleService.addGameDataChangedListener(this);

    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }

  private void clearImages() {
    List<Integer> gameIds = frontendService.getGameIds();

    for (File f : org.apache.commons.io.FileUtils.listFiles(systemService.getRawImageExtractionFolder(), null, false)) {
      try {
        Integer id = Integer.valueOf(StringUtils.substringBefore(f.getName(), "_"));
        String type = StringUtils.substringAfter(f.getName(), "_");
        if (!gameIds.contains(id) || !(type.equals(SystemService.DEFAULT_BACKGROUND) || type.equals(SystemService.DMD) || type.equals(SystemService.PREVIEW))) {
          f.delete();
        }
      }
      catch (NumberFormatException e) {
        LOG.warn("Failed to clean up backglass media file {}", f.getAbsolutePath());
        f.delete();
      }
    }
    LOG.info("Folder '{}' cleaned", systemService.getRawImageExtractionFolder().getAbsolutePath());
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    //ALWAYS AVOID CALLING GETKNOWNGAMES DURING THE INITILIZATION PHASE OF THE SERVER
    clearImages();
  }

}
