package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.resources.ResourceLoader;
import de.mephisto.vpin.server.system.DefaultPictureService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.server.util.ImageUtil;
import de.mephisto.vpin.server.util.UploadUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
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
    return assetRepository.saveAndFlush(asset);
  }

  public Asset getById(long id) {
    Optional<Asset> asset = assetRepository.findById(id);
    return asset.orElse(null);
  }

  public byte[] getRaw(int gameId) {
    try {
      Game game = gameService.getGame(gameId);
      if (game != null) {
        File target = defaultPictureService.getRawDefaultPicture(game);
        if (target != null && !target.exists()) {
          defaultPictureService.extractDefaultPicture(game);
        }

        target = defaultPictureService.getRawDefaultPicture(game);
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
    }
    catch (Exception e) {
      LOG.error("Failed to load default image: " + e.getMessage(), e);
    }
    return null;
  }

  public boolean deleteDefaultBackground(int gameId) {
    assetRepository.deleteByExternalId(String.valueOf(gameId));

    Game game = gameService.getGame(gameId);
    if (game != null) {
      defaultPictureService.deleteDefaultPictures(game);
    }
    return true;
  }

  public Boolean backgroundUpload(MultipartFile file, int gameId) throws Exception {
    if (file == null) {
      LOG.error("Upload request did not contain a file object.");
      return false;
    }

    Game game = gameService.getGame(gameId);

    File croppedDefaultPicture = defaultPictureService.getCroppedDefaultPicture(game);
    File rawDefaultPicture = defaultPictureService.getRawDefaultPicture(game);

    if (game == null || rawDefaultPicture == null || croppedDefaultPicture == null) {
      LOG.error("Invalid game data.");
      return false;
    }

    defaultPictureService.deleteDefaultPictures(game);
    
    LOG.info("Uploading " + rawDefaultPicture.getAbsolutePath());
    return UploadUtil.upload(file, rawDefaultPicture);
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
    }
    catch (Exception e) {
      LOG.warn("Failed to get competition background " + e.getMessage());
    }
    return null;
  }


  public byte[] getSubscriptionCard(@NonNull Competition competition, @NonNull Game game) {
    Asset asset = getCompetitionBackground(competition.getGameId());
    return AssetFactory.createSubscriptionCard(asset, game, competition);
  }

  public byte[] getCompetitionStartedCard(@NonNull Competition competition, @NonNull Game game) {
    Asset asset = getCompetitionBackground(competition.getGameId());
    return AssetFactory.createCompetitionStartedCard(asset, game, competition);
  }

  public byte[] getCompetitionFinishedCard(@NonNull Competition competition, @NonNull Game game, @Nullable Player winner, @NonNull ScoreSummary summary) {
    Asset asset = getCompetitionBackground(competition.getGameId());
    return AssetFactory.createCompetitionFinishedCard(asset, game, competition, winner, summary);
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
