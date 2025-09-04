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

public class TableAssetDefaultSourcesFactory {

  public static List<TableAssetSource> createDefaults(@NonNull List<TableAssetSource> sources) {
    List<TableAssetSource> updatesList = new ArrayList<>(sources);
    Optional<TableAssetSource> sourceEntry = sources.stream().filter(s -> s.getType().equals(TableAssetSourceType.TutorialVideos)).findFirst();
    if (sourceEntry.isEmpty()) {
      updatesList.add(createTutorialVideosAssetSource());
    }

    sourceEntry = sources.stream().filter(s -> s.getType().equals(TableAssetSourceType.SuperHacRepo)).findFirst();
    if (sourceEntry.isEmpty()) {
      updatesList.add(createSuperHacAssetSource());
    }
    return updatesList;
  }

  private static TableAssetSource createTutorialVideosAssetSource() {
    TableAssetSource source = new TableAssetSource();
    source.setId(VideoTutorialsTableAssetAdapter.SOURCE_ID);
    source.setLookupStrategy(AssetLookupStrategy.screens);
    source.setType(TableAssetSourceType.TutorialVideos);
    source.setName("Kongedam Tutorials");
    source.setProvided(true);
    source.setEnabled(true);
    source.setLocation("vpin-mania.net");
    source.setSupportedScreens(Arrays.asList(VPinScreen.GameInfo.name(), VPinScreen.GameHelp.name()));
    return source;
  }

  /**
   * https://github.com/superhac/vpinmediadb/tree/main
   * @return
   */
  private static TableAssetSource createSuperHacAssetSource() {
    TableAssetSource source = new TableAssetSource();
    source.setId(SuperHacTableAssetAdapter.SOURCE_ID);
    source.setLookupStrategy(AssetLookupStrategy.screens);
    source.setType(TableAssetSourceType.SuperHacRepo);
    source.setName("superhac VPinMediaDB");
    source.setProvided(true);
    source.setEnabled(true);
    source.setLocation("https://github.com/superhac/vpinmediadb");
    source.setSupportedScreens(Arrays.asList(VPinScreen.GameInfo.name(), VPinScreen.GameHelp.name(), VPinScreen.BackGlass.name(), VPinScreen.DMD.name(), VPinScreen.PlayField.name(), VPinScreen.Wheel.name()));
    return source;
  }
}
