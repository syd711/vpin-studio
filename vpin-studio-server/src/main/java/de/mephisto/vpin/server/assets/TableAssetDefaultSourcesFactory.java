package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.AssetLookupStrategy;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetSourceType;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static de.mephisto.vpin.server.assets.VideoTutorialsTableAssetAdapter.SOURCE_ID;

public class TableAssetDefaultSourcesFactory {

  public static List<TableAssetSource> createDefaults(@NonNull List<TableAssetSource> sources) {
    List<TableAssetSource> updatesList = new ArrayList<>(sources);
    Optional<TableAssetSource> first = sources.stream().filter(s -> s.getType().equals(TableAssetSourceType.TutorialVideos)).findFirst();
    if (first.isEmpty()) {
      updatesList.add(createTutorialVideosAssetSource());
    }
    return updatesList;
  }

  private static TableAssetSource createTutorialVideosAssetSource() {
    TableAssetSource source = new TableAssetSource();
    source.setId(SOURCE_ID);
    source.setLookupStrategy(AssetLookupStrategy.screens);
    source.setType(TableAssetSourceType.TutorialVideos);
    source.setName("Kongedam Tutorials");
    source.setEnabled(true);
    source.setLocation("vpin-mania.net");
    source.setSupportedScreens(Arrays.asList(VPinScreen.GameInfo.name(), VPinScreen.GameHelp.name()));
    return source;
  }
}
