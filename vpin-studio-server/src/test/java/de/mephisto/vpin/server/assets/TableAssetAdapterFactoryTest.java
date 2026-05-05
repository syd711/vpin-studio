package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetSourceType;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.vps.VpsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TableAssetAdapterFactoryTest {

  @Mock
  private VpsService vpsService;

  @InjectMocks
  private TableAssetAdapterFactory factory;

  private TableAssetSource sourceOfType(TableAssetSourceType type) {
    TableAssetSource source = mock(TableAssetSource.class);
    when(source.getType()).thenReturn(type);
    return source;
  }

  @Test
  void createAdapter_returnsFileSystemAdapter_forFileSystemType() {
    TableAssetsAdapter<Game> adapter = factory.createAdapter(vpsService, sourceOfType(TableAssetSourceType.FileSystem));

    assertNotNull(adapter);
    assertInstanceOf(FileSystemTableAssetAdapter.class, adapter);
  }

  @Test
  void createAdapter_returnsTutorialVideosAdapter_forTutorialVideosType() {
    TableAssetsAdapter<Game> adapter = factory.createAdapter(vpsService, sourceOfType(TableAssetSourceType.TutorialVideos));

    assertNotNull(adapter);
    assertInstanceOf(VideoTutorialsTableAssetAdapter.class, adapter);
  }

  @Test
  void createAdapter_returnsSuperHacAdapter_forSuperHacRepoType() {
    TableAssetsAdapter<Game> adapter = factory.createAdapter(vpsService, sourceOfType(TableAssetSourceType.SuperHacRepo));

    assertNotNull(adapter);
    assertInstanceOf(SuperHacTableAssetAdapter.class, adapter);
  }

  @Test
  void createAdapter_returnsManiaLogosAdapter_forManiaLogosType() {
    TableAssetsAdapter<Game> adapter = factory.createAdapter(vpsService, sourceOfType(TableAssetSourceType.ManiaLogos));

    assertNotNull(adapter);
    assertInstanceOf(ManiaLogosAssetAdapter.class, adapter);
  }

  @Test
  void createAdapter_throwsIllegalState_forUnknownType() {
    TableAssetSource source = mock(TableAssetSource.class);
    when(source.getType()).thenReturn(TableAssetSourceType.PinUPPopper);

    assertThrows(IllegalStateException.class, () -> factory.createAdapter(vpsService, source));
  }
}
