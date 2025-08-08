package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetSourceType;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.vps.VpsService;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TableAssetAdapterFactory {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetAdapterFactory.class);

  public TableAssetsAdapter<Game> createAdapter(@NonNull VpsService vpsService, @NonNull TableAssetSource tableAssetSource) {
    TableAssetSourceType type = tableAssetSource.getType();
    TableAssetsAdapter<Game> adapter = null;
    switch (type) {
      case FileSystem: {
        adapter = new FileSystemTableAssetAdapter(tableAssetSource);
        break;
      }
      case TutorialVideos: {
        adapter = new VideoTutorialsTableAssetAdapter(vpsService, tableAssetSource);
        break;
      }
      default:
        throw new IllegalStateException("Unexpected value: " + type);
    }

    LOG.info("Created new TableAssetsAdapter: {}", adapter);
    return adapter;
  }
}
