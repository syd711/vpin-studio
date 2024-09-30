package de.mephisto.vpin.server.system;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.cards.CardSettings;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.VPinStudioException;
import de.mephisto.vpin.server.directb2s.DirectB2SDataExtractor;
import de.mephisto.vpin.server.directb2s.DirectB2SImageExporter;
import de.mephisto.vpin.server.directb2s.DirectB2SImageRatio;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.preferences.PreferenceChangedListener;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.puppack.PupPack;
import de.mephisto.vpin.server.puppack.PupPacksService;
import de.mephisto.vpin.server.util.ImageUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class DefaultPictureService implements PreferenceChangedListener, InitializingBean {
  private final static Logger LOG = LoggerFactory.getLogger(DefaultPictureService.class);

  private final static DirectB2SImageRatio DEFAULT_MEDIA_RATIO = DirectB2SImageRatio.RATIO_16X9;

  @Autowired
  private SystemService systemService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private PupPacksService pupPackService;

  @Autowired
  private PreferencesService preferencesService;

  private CardSettings cardSettings;

  public void extractDefaultPicture(@NonNull Game game) {
    if (StringUtils.isEmpty(game.getRom())) {
      return;
    }

    File rawDefaultPicture = getRawDefaultPicture(game);
    if (!rawDefaultPicture.getParentFile().exists()) {
      rawDefaultPicture.getParentFile().mkdirs();
    }

    File target = getRawDefaultPicture(game);
    if (game.getDirectB2SFile().exists()) {
      try {
        DirectB2SDataExtractor data = new DirectB2SDataExtractor();
        data.extractData(game.getDirectB2SFile(), game.getEmulatorId(), "not needed", game.getId());

        if (data.getBackgroundBase64() != null) {
          DirectB2SImageExporter extractor = new DirectB2SImageExporter(data);
          extractor.extractBackground(target);
          extractor.extractDMD(getDMDPicture(game));
          return;
        }
        else {
          LOG.warn("Backglass of \"" + game.getDirectB2SFile().getAbsolutePath() + "\" does not contain a background image.");
        }
      }
      catch (VPinStudioException e) {
        LOG.error("Failed to extract background image: " + e.getMessage(), e);
      }
    }

    FrontendMediaItem backGlassItem = frontendService.getDefaultMediaItem(game, VPinScreen.BackGlass);
    if (backGlassItem != null && backGlassItem.getFile().exists()) {
      String name = backGlassItem.getFile().getName();
      if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg")) {
        try {
          FileUtils.copyFile(backGlassItem.getFile(), target);
          return;
        }
        catch (IOException e) {
          LOG.error("Failed to copy resource file as background: " + e.getMessage(), e);
        }
      }
      else if (name.endsWith(".mp4") || name.endsWith(".m4v") || name.endsWith(".mov")) {
        if (JCodec.export(backGlassItem.getFile(), target)) {
          return;
        }
      }
    }

    PupPack pupPack = game.getPupPack();
    if (pupPack != null) {
      pupPackService.exportDefaultPicture(pupPack, target);
    }

  }

  public void deleteDefaultPictures(@NonNull Game game) {
    File croppedDefaultPicture = getCroppedDefaultPicture(game);
    if (croppedDefaultPicture != null && croppedDefaultPicture.exists()) {
      if (croppedDefaultPicture.delete()) {
        LOG.info("Deleted " + croppedDefaultPicture.getAbsolutePath());
      }
      else {
        LOG.error("Failed to delete default crop asset.");
      }
    }

    File rawDefaultPicture = getRawDefaultPicture(game);
    if (rawDefaultPicture != null && rawDefaultPicture.exists()) {
      if (rawDefaultPicture.delete()) {
        LOG.info("Deleted " + rawDefaultPicture.getAbsolutePath());
      }
      else {
        LOG.error("Failed to delete default crop asset.");
      }
    }
  }

  @Nullable
  public File generateCroppedDefaultPicture(@NonNull Game game) {
    try {
      //try to use existing file first
      File croppedDefaultPicture = getCroppedDefaultPicture(game);
      if (croppedDefaultPicture != null && croppedDefaultPicture.exists()) {
        return croppedDefaultPicture;
      }

      File rawDefaultPicture = getRawDefaultPicture(game);
      if (rawDefaultPicture != null && !rawDefaultPicture.exists()) {
        extractDefaultPicture(game);
      }

      rawDefaultPicture = getRawDefaultPicture(game);
      if (rawDefaultPicture != null && rawDefaultPicture.exists()) {

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
      if(resized.getHeight() < cropHeight) {
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

  @Override
  public void preferenceChanged(String propertyName, Object oldValue, Object newValue) throws Exception {
    if (PreferenceNames.HIGHSCORE_CARD_SETTINGS.equalsIgnoreCase(propertyName)) {
      cardSettings = preferencesService.getJsonPreference(PreferenceNames.HIGHSCORE_CARD_SETTINGS, CardSettings.class);
    }
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    preferencesService.addChangeListener(this);
    preferenceChanged(PreferenceNames.HIGHSCORE_CARD_SETTINGS, null, null);
  }

  //-------------------------

  @Nullable
  @JsonIgnore
  public File getCroppedDefaultPicture(Game game) {
    if (game.getRom() != null) {
      File subFolder = new File(systemService.getB2SCroppedImageFolder(), game.getRom());
      if (!StringUtils.isEmpty(game.getRomAlias())) {
        subFolder = new File(systemService.getB2SCroppedImageFolder(), game.getRomAlias());
      }
      return new File(subFolder, SystemService.DEFAULT_BACKGROUND);
    }
    return null;
  }

  @Nullable
  @JsonIgnore
  public File getDMDPicture(Game game) {
    if (game.getRom() != null) {
      File subFolder = new File(systemService.getB2SCroppedImageFolder(), game.getRom());
      if (!StringUtils.isEmpty(game.getRomAlias())) {
        subFolder = new File(systemService.getB2SCroppedImageFolder(), game.getRomAlias());
      }
      return new File(subFolder, SystemService.DMD);
    }
    return null;
  }

  @Nullable
  @JsonIgnore
  public File getRawDefaultPicture(Game game) {
    if (game.getRom() != null) {
      File subFolder = new File(systemService.getB2SImageExtractionFolder(), game.getRom());
      if (!StringUtils.isEmpty(game.getRomAlias())) {
        subFolder = new File(systemService.getB2SImageExtractionFolder(), game.getRomAlias());
      }
      return new File(subFolder, SystemService.DEFAULT_BACKGROUND);
    }
    return null;
  }

}
