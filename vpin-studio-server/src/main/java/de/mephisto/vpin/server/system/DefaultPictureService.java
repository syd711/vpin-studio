package de.mephisto.vpin.server.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.cards.CardSettings;
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
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPack;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.resources.ResourceLoader;
import de.mephisto.vpin.server.util.ImageUtil;
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
public class DefaultPictureService implements PreferenceChangedListener, ApplicationListener<ApplicationReadyEvent>, InitializingBean, GameDataChangedListener {
  private final static Logger LOG = LoggerFactory.getLogger(DefaultPictureService.class);

  private final static DirectB2SImageRatio DEFAULT_MEDIA_RATIO = DirectB2SImageRatio.RATIO_16X9;

  @Autowired
  private SystemService systemService;

  @Autowired
  private BackglassService backglassService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private PupPacksService pupPackService;

  @Autowired
  private PreferencesService preferencesService;

  @Autowired
  private GameLifecycleService gameLifecycleService;


  private CardSettings cardSettings;


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
        extractPreviewPicture(game);
      }
      return extractBytes(preview);
    }
    else if (VPinScreen.Menu.equals(onScreen)) {
      File dmd = getDMDPicture(game);
      if (!dmd.exists()) {
        extractDmd(game);
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

  public void extractPreviewPicture(@NonNull Game game) {
    File target = getDefaultPreview(game);
    extractDefaultPicture(game, target, true);
  }

  private void extractDefaultPicture(@NonNull Game game, File target, boolean usePreview) {
    File rawDefaultPicture = getRawDefaultPicture(game);
    if (!rawDefaultPicture.getParentFile().exists() && !rawDefaultPicture.getParentFile().mkdirs()) {
      LOG.error("Failed to create raw default picture folder: " + rawDefaultPicture.getParentFile().getAbsolutePath());
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
      return;
    }

    PupPack pupPack = game.getPupPack();
    if (pupPack != null) {
      pupPackService.exportDefaultPicture(pupPack, target);
      return;
    }

    // no more idea :)
  }

  public void extractDmd(Game game) {
    File target = getDMDPicture(game);

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
    if (!FileUtils.delete(getCroppedDefaultPicture(game))) {
      success = false;
    }
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
  public File generateCroppedDefaultPicture(@NonNull Game game) {
    try {
      //try to use existing file first
      File croppedDefaultPicture = getCroppedDefaultPicture(game);
      if (croppedDefaultPicture.exists()) {
        return croppedDefaultPicture;
      }

      File rawDefaultPicture = getRawDefaultPicture(game);
      if (!rawDefaultPicture.exists()) {
        extractDefaultPicture(game);
      }

      rawDefaultPicture = getRawDefaultPicture(game);
      if (rawDefaultPicture.exists()) {

        BufferedImage image = ImageIO.read(rawDefaultPicture);
        BufferedImage crop = ImageUtil.crop(image, DEFAULT_MEDIA_RATIO.getXRatio(), DEFAULT_MEDIA_RATIO.getYRatio());
        BufferedImage resized = ImageUtil.resizeImage(crop, cardSettings.getCardResolution().toWidth());

        File target = getCroppedDefaultPicture(game);
        if (target == null) {
          return null;
        }

        if (target.exists()) {
          if (!target.delete()) {
            LOG.error("Failed to delete crop picture " + target.getAbsolutePath());
          }
        }

        if (!target.getParentFile().exists()) {
          if (!target.getParentFile().mkdirs()) {
            LOG.error("Failed to create crop image directory " + target.getParentFile().getAbsolutePath());
          }
        }

        if (target.getParentFile().exists() && target.getParentFile().canWrite()) {
          ImageUtil.write(resized, target);
          LOG.info("Written cropped default background for " + game.getRom());
        }
        else {
          LOG.error("No permission to write cropped default picture, folder " + game.getRom() + " does not exist.");
        }
        return target;
      }
    }
    catch (Exception e) {
      LOG.error("Error extracting default picture: " + e.getMessage(), e);
    }
    return null;
  }

  @Nullable
  public BufferedImage generateCompetitionBackgroundImage(@NonNull Game game, int cropWidth, int cropHeight) {
    try {
      File backgroundImageFile = getRawDefaultPicture(game);
      if (backgroundImageFile == null || !backgroundImageFile.exists()) {
        extractDefaultPicture(game);
      }

      backgroundImageFile = getRawDefaultPicture(game);
      if (backgroundImageFile == null || !backgroundImageFile.exists()) {
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
      ImageUtil.gradient(blurred, cropHeight, cropWidth, start, end);
      return blurred;
    }
    catch (Exception e) {
      LOG.warn("Error creating competition image for " + game.getGameDisplayName() + ": " + e.getMessage(), e);
    }
    return null;
  }

  public boolean isMediaIndexAvailable() {
    return systemService.getCroppedImageFolder().exists()
        && systemService.getRawImageExtractionFolder().exists()
        && !org.apache.commons.io.FileUtils.listFiles(systemService.getRawImageExtractionFolder(), null, false).isEmpty();
  }

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.HIGHSCORE_CARD_SETTINGS.equalsIgnoreCase(propertyName)) {
      cardSettings = preferencesService.getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
    }
  }

  //-------------------------

  @NonNull
  @JsonIgnore
  public File getCroppedDefaultPicture(Game game) {
    return new File(systemService.getCroppedImageFolder(), game.getId() + "_" + SystemService.DEFAULT_BACKGROUND);
  }

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

  //---------------------------------------------------

  @Override
  public void gameDataChanged(GameDataChangedEvent changedEvent) {
    // not needed
  }

  @Override
  public void gameAssetChanged(GameAssetChangedEvent changedEvent) {
    if (AssetType.DIRECTB2S.equals(changedEvent.getAssetType())) {
      // not that clean but sufficient so save access to Game
      Game game = new Game();
      game.setId(changedEvent.getGameId());
      deleteAllPictures(game);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferencesService.addChangeListener(this);
    preferenceChanged(PreferenceNames.HIGHSCORE_CARD_SETTINGS, null, null);

    gameLifecycleService.addGameDataChangedListener(this);

    LOG.info("{} initialization finished.", this.getClass().getSimpleName());
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    //ALWAYS AVOID CALLING GETKNOWNGAMES DURING THE INITILIZATION PHASE OF THE SERVER
    clearImages();
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
      }
    }
    LOG.info("Folder '{}' cleaned", systemService.getRawImageExtractionFolder().getAbsolutePath());

    for (File f : org.apache.commons.io.FileUtils.listFiles(systemService.getCroppedImageFolder(), null, false)) {
      try {
        Integer id = Integer.valueOf(StringUtils.substringBefore(f.getName(), "_"));
        String type = StringUtils.substringAfter(f.getName(), "_");
        if (!gameIds.contains(id) || !(type.equals(SystemService.DEFAULT_BACKGROUND))) {
          f.delete();
        }
      }
      catch (NumberFormatException e) {
        LOG.warn("Failed to clean up cropped backglass media file {}", f.getAbsolutePath());
      }
    }
    LOG.info("Folder '{}' cleaned", systemService.getCroppedImageFolder().getAbsolutePath());
  }

}
