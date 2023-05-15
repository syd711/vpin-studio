package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.restclient.AssetType;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.resources.ResourceLoader;
import de.mephisto.vpin.server.system.DefaultPictureService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.ImageUtil;
import de.mephisto.vpin.server.util.UploadUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AssetService {
  private final static Logger LOG = LoggerFactory.getLogger(AssetService.class);

  @Autowired
  private AssetRepository assetRepository;

  @Autowired
  private GameService gameService;

  @Autowired
  private DefaultPictureService defaultPictureService;

  public Asset save(Asset asset) {
    return assetRepository.save(asset);
  }

  public Asset getById(long id) {
    Optional<Asset> asset = assetRepository.findById(id);
    return asset.orElse(null);
  }

  public byte[] getRaw(int gameId) {
    try {
      Game game = gameService.getGame(gameId);
      if (game != null) {
        File target = game.getRawDefaultPicture();
        if (target != null && !target.exists()) {
          defaultPictureService.extractDefaultPicture(game);
        }

        target = game.getRawDefaultPicture();
        if (target != null && target.exists()) {
          BufferedImage bufferedImage = ImageUtil.loadImage(target);
          return ImageUtil.toBytes(bufferedImage);
        }
      }
      else {
        LOG.warn("No GameInfo found for id " + gameId);
      }

      InputStream in = ResourceLoader.class.getResourceAsStream("empty-preview.png");
      return IOUtils.toByteArray(in);
    } catch (Exception e) {
      LOG.error("Failed to load default image: " + e.getMessage(), e);
    }
    return null;
  }

  public boolean deleteDefaultBackground(int gameId) {
    Game game = gameService.getGame(gameId);
    if (game != null) {
      if (game.getCroppedDefaultPicture() != null && game.getCroppedDefaultPicture().exists()) {
        if (!game.getCroppedDefaultPicture().delete()) {
          LOG.error("Failed to delete default crop asset.");
        }
      }

      if (game.getRawDefaultPicture() != null && game.getRawDefaultPicture().exists()) {
        if (!game.getRawDefaultPicture().delete()) {
          LOG.error("Failed to delete default crop asset.");
        }
      }
    }
    return true;
  }

  public Boolean backgroundUpload(MultipartFile file, int gameId) throws Exception {
    if (file == null) {
      LOG.error("Upload request did not contain a file object.");
      return false;
    }

    Game game = gameService.getGame(gameId);
    if (game == null || game.getRawDefaultPicture() == null || game.getCroppedDefaultPicture() == null) {
      LOG.error("Invalid game data.");
      return false;
    }

    if (game.getCroppedDefaultPicture().exists()) {
      game.getCroppedDefaultPicture().delete();
    }

    if (game.getRawDefaultPicture().exists()) {
      game.getRawDefaultPicture().delete();
    }

    File out = game.getRawDefaultPicture();
    LOG.info("Uploading " + out.getAbsolutePath());
    return UploadUtil.upload(file, out);
  }


  public Asset getCompetitionBackground(long gameId) {
    try {
      Game game = gameService.getGame((int) gameId);
      Optional<Asset> asset = assetRepository.findByExternalIdAndAssetType(String.valueOf(game.getId()), AssetType.COMPETITION.name());
      if (asset.isEmpty()) {
        BufferedImage background = defaultPictureService.generateCompetitionBackgroundImage(game, 800, 340);
        if (background == null) {
          File defaultAsset = new File(SystemService.RESOURCES, "competition-bg-default.png");
          background = ImageUtil.loadImage(defaultAsset);
        }
        byte[] bytes = ImageUtil.toBytes(background);
        return saveOrUpdate(bytes, -1, "image.png", AssetType.COMPETITION.name(), String.valueOf(game.getId()));
      }

      return asset.get();
    } catch (Exception e) {
      LOG.error("Failed to get competition background " + e.getMessage(), e);
    }
    return null;
  }


  public byte[] getCompetitionBackgroundFor(@NonNull Competition competition) {
    try {
      Game game = gameService.getGame(competition.getGameId());
      Asset asset = getCompetitionBackground(competition.getGameId());
      byte[] data = asset.getData();
      BufferedImage background = ImageIO.read(new ByteArrayInputStream(data));
      Graphics2D graphics = (Graphics2D) background.getGraphics();
      graphics.setRenderingHint(
          RenderingHints.KEY_TEXT_ANTIALIASING,
          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      graphics.setColor(Color.WHITE);

      String name = competition.getName();
      if (name.length() > 35) {
        name = name.substring(0, 34) + "...";
      }

      String table = game.getGameDisplayName();
      if(table.length() > 36) {
        table = table.substring(0, 35) + "...";
      }

      int yOffset = 0;
      int xOffset = 32;
      Font font = new Font("System", Font.BOLD, 38);
      graphics.setFont(font);
      graphics.drawString(name, xOffset, yOffset += 64);

      font = new Font("System", Font.BOLD, 30);
      graphics.setFont(font);
      graphics.drawString("Table: " + table, xOffset, yOffset += 72);

      font = new Font("System", Font.PLAIN, 32);
      graphics.setFont(font);
      graphics.drawString("Start Date:", xOffset, yOffset += 52);
      graphics.drawString(DateUtil.formatDateTime(competition.getStartDate()), 232, yOffset);
      graphics.drawString("End Date:", xOffset, yOffset += 42);
      graphics.drawString(DateUtil.formatDateTime(competition.getEndDate()), 232, yOffset);
      graphics.drawString("Duration: " + DateUtil.formatDuration(competition.getStartDate(), competition.getEndDate()), xOffset, yOffset += 72);

      return ImageUtil.toBytes(background);
    } catch (Exception e) {
      LOG.error("Failed to get competition background " + e.getMessage(), e);
    }
    return null;
  }

  public Asset getByUuid(String uuid) {
    Optional<Asset> asset = assetRepository.findByUuid(uuid);
    return asset.orElse(null);
  }


  public List<Asset> getAssets() {
    return assetRepository.findAll();
  }

  public boolean resetGameAssets(long gameId) {
    Game game = gameService.getGame((int) gameId);

    Optional<Asset> byId = assetRepository.findById(gameId);
    if (byId.isPresent()) {
      assetRepository.delete(byId.get());
      LOG.info("Deleted assets for " + game.getGameDisplayName());
      return true;
    }

    defaultPictureService.deleteDefaultPictures(game);
    defaultPictureService.extractDefaultPicture(game);

    return false;
  }

  public Asset saveOrUpdate(byte[] data, long id, String assetName, String assetType, String externalId) {
    String mimeType = "image/jpg";
    if (assetName.toLowerCase().endsWith(".png")) {
      mimeType = "image/png";
    }

    Asset asset = new Asset();
    asset.setAssetType(assetType);

    asset.setUuid(UUID.randomUUID().toString());
    if (id >= 0) {
      Optional<Asset> byId = assetRepository.findById(id);
      if (byId.isPresent()) {
        asset = byId.get();
      }
    }
    asset.setData(data);
    asset.setMimeType(mimeType);
    asset.setExternalId(externalId);
    Asset updated = assetRepository.saveAndFlush(asset);
    LOG.info("Saved " + updated);
    return updated;
  }
}
