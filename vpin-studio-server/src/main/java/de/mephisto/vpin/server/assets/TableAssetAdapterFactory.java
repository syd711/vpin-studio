package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetSourceType;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import static de.mephisto.vpin.connectors.assets.TableAssetSourceType.TutorialVideos;

@Service
public class TableAssetAdapterFactory {
  private final static Logger LOG = LoggerFactory.getLogger(TableAssetAdapterFactory.class);

  public TableAssetsAdapter createAdapter(@NonNull TableAssetSource tableAssetSource) {
    TableAssetSourceType type = tableAssetSource.getType();
    TableAssetsAdapter adapter = null;
    switch (type) {
      case FileSystem: {
        adapter = new FileSystemTableAssetAdapter(tableAssetSource);
        break;
      }
      case TutorialVideos: {
        adapter = new FileSystemTableAssetAdapter(tableAssetSource);
        break;
      }
      default:
        throw new IllegalStateException("Unexpected value: " + type);
    }

    LOG.info("Created new TableAssetsAdapter: {}", adapter);
    return adapter;
  }
}
