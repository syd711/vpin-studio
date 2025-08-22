package de.mephisto.vpin.server.assets;

import com.drew.imaging.ImageMetadataReader;
import com.drew.lang.Rational;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.StringValue;
import com.drew.metadata.Tag;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.assets.AssetMetaData;
import de.mephisto.vpin.restclient.assets.AssetRequest;
import de.mephisto.vpin.restclient.assets.AssetType;
import de.mephisto.vpin.restclient.frontend.FrontendMediaItem;
import de.mephisto.vpin.server.competitions.Competition;
import de.mephisto.vpin.server.competitions.ScoreSummary;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.players.Player;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.resources.ResourceLoader;
import de.mephisto.vpin.server.system.DefaultPictureService;
import de.mephisto.vpin.server.system.SystemService;
import de.mephisto.vpin.commons.fx.ImageUtil;
import de.mephisto.vpin.server.util.UploadUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AssetService {
  private final static Logger LOG = LoggerFactory.getLogger(AssetService.class);

  @Autowired
  private AssetRepository assetRepository;

  @Autowired
  private GameService gameService;

  @Autowired
  private DefaultPictureService defaultPictureService;

  @Autowired
  private FrontendService frontendService;

  @Autowired
  private PreferencesService preferencesService;

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
        if (!target.exists()) {
          defaultPictureService.extractDefaultPicture(game);
        }

        if (target.exists()) {
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


  public AssetRequest getMetadata(AssetRequest request) {
    AssetMetaData metaData = new AssetMetaData();
    request.setMetaData(metaData);
    try {
      Game game = frontendService.getOriginalGame(request.getGameId());
      if (game == null) {
        LOG.info("No game found for " + request.getGameId());
        request.setResult("No game found for " + request.getGameId());
        return request;
      }

      FrontendMediaItem mediaItem = frontendService.getMediaItem(game, request.getScreen(), request.getName());
      if (mediaItem == null) {
        LOG.info("No media item found for " + request.getName());
        request.setResult("No media item found for " + request.getName());
        return request;
      }

      File file = mediaItem.getFile();
      if (file.exists()) {
        if (file.getName().endsWith(".mp3")) {
          readMp3Metadata(file, metaData);
        }
        else {
          readVideoAndImageMetadata(file, metaData);
        }
      }
    }
    catch (Exception e) {
      LOG.error("Failed to read video metadata: " + e.getMessage());
      request.setResult("Failed to read video metadata: " + e.getMessage());
    }
    return request;
  }

  private static void readMp3Metadata(File file, AssetMetaData metaData) throws IOException, SAXException, TikaException {
    //detecting the file type
    BodyContentHandler handler = new BodyContentHandler();
    org.apache.tika.metadata.Metadata mp3Meta = new org.apache.tika.metadata.Metadata();
    FileInputStream inputstream = new FileInputStream(file);
    ParseContext pcontext = new ParseContext();

    //Mp3 parser
    Mp3Parser Mp3Parser = new Mp3Parser();
    Mp3Parser.parse(inputstream, handler, mp3Meta, pcontext);
    String[] metadataNames = mp3Meta.names();

    for (String name : metadataNames) {
      metaData.getData().put(name, mp3Meta.get(name));
    }
  }

  public static AssetMetaData readVideoAndImageMetadata(File file) throws Exception {
    AssetMetaData metadata = new AssetMetaData();
    readVideoAndImageMetadata(file, metadata);
    return metadata;
  }

  private static void readVideoAndImageMetadata(File file, AssetMetaData metaData) throws Exception {
    Metadata metadata = ImageMetadataReader.readMetadata(file);
    Iterable<Directory> directories = metadata.getDirectories();
    for (Directory directory : directories) {
      Collection<Tag> tags = directory.getTags();
      for (Tag tag : tags) {
        Object object = directory.getObject(tag.getTagType());

        if (object instanceof Rational || object instanceof byte[] || object instanceof StringValue) {
          continue;
        }
        if (object instanceof String[]) {
          object = String.join(", ", Arrays.asList((String[]) object));
        }
        if (object instanceof int[]) {
          object = String.join(", ", Arrays.asList((int[]) object).stream().map(o -> String.valueOf(o)).collect(Collectors.toList()));
        }
        if (object instanceof float[]) {
          object = String.join(", ", Arrays.asList((float[]) object).stream().map(o -> String.valueOf(o)).collect(Collectors.toList()));
        }
        metaData.getData().put(tag.getTagName(), object);

//            System.out.println(tag.getTagName() + ": " + object + " (" + object.getClass().getSimpleName() + ")");
      }
    }
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
    if (game == null) {
      LOG.error("Invalid game data.");
      return false;
    }

    defaultPictureService.deleteDefaultPictures(game);

    File rawDefaultPicture = defaultPictureService.getRawDefaultPicture(game);

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
    File wheelFile = frontendService.getWheelImage(game);
    return AssetFactory.createSubscriptionCard(asset, game, wheelFile, competition);
  }

  public byte[] getCompetitionStartedCard(@NonNull Competition competition, @NonNull Game game) {
    Asset asset = getCompetitionBackground(competition.getGameId());
    File wheelFile = frontendService.getWheelImage(game);
    return AssetFactory.createCompetitionStartedCard(asset, game, wheelFile, competition);
  }

  public byte[] getCompetitionFinishedCard(@NonNull Competition competition, @NonNull Game game, @Nullable Player winner, @NonNull ScoreSummary summary) {
    Asset asset = getCompetitionBackground(competition.getGameId());
    File wheelFile = frontendService.getWheelImage(game);
    return AssetFactory.createCompetitionFinishedCard(asset, game, wheelFile, competition, winner, summary);
  }

  public Asset getByUuid(String uuid) {
    Optional<Asset> asset = assetRepository.findByUuid(uuid);
    return asset.orElse(null);
  }


  public List<Asset> getAssets() {
    return assetRepository.findAll();
  }

  public boolean resetGameAssets(int gameId) {
    Game game = gameService.getGame(gameId);

    Optional<Asset> byId = assetRepository.findById((long) gameId);
    if (byId.isPresent()) {
      assetRepository.delete(byId.get());
      LOG.info("Deleted assets for " + game.getGameDisplayName());
      return true;
    }

    defaultPictureService.updateGame(game);

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

  public boolean isMediaIndexAvailable() {
    return defaultPictureService.isMediaIndexAvailable();
  }

  public Asset getAvatar() {
    Asset avatar = (Asset) preferencesService.getPreferenceValue(PreferenceNames.AVATAR);
    byte[] image = defaultPictureService.generateAvatarImage(avatar != null? avatar.getData() : null);
    Asset clipped = new Asset();
    clipped.setAssetType(AssetType.AVATAR.name());
    clipped.setUpdatedAt(new Date());
    clipped.setMimeType("image/png");
    clipped.setData(image);
    return clipped;
  }

}
