package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TableAssetSourcesServiceTest {

  @Mock
  private PreferencesService preferencesService;

  @Mock
  private TableAssetsService tableAssetsService;

  @Mock
  private FrontendService frontendService;

  @InjectMocks
  private TableAssetSourcesService service;

  // ---- getAssetSource(null) ----

  @Test
  void getAssetSource_returnsNull_whenSourceIdIsNull() {
    assertNull(service.getAssetSource(null));
  }

  // ---- getDefaultAssetSource ----

  @Test
  void getDefaultAssetSource_returnsNull_whenAdapterIsNull() {
    when(frontendService.getTableAssetAdapter()).thenReturn(null);

    assertNull(service.getDefaultAssetSource());
  }

  @Test
  @SuppressWarnings("unchecked")
  void getDefaultAssetSource_returnsSource_whenAdapterHasSource() {
    TableAssetsAdapter<Game> adapter = mock(TableAssetsAdapter.class);
    TableAssetSource source = mock(TableAssetSource.class);
    when(frontendService.getTableAssetAdapter()).thenReturn(adapter);
    when(adapter.getAssetSource()).thenReturn(source);

    assertSame(source, service.getDefaultAssetSource());
  }
}
