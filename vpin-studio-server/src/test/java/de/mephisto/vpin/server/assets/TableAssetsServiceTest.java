package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.vps.VpsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TableAssetsServiceTest {

  @Mock
  private FrontendService frontendService;

  @Mock
  private VpsService vpsService;

  @Mock
  private TableAssetAdapterFactory tableAssetAdapterFactory;

  @InjectMocks
  private TableAssetsService tableAssetsService;

  // ---- testConnection ----

  @Test
  void testConnection_returnsFalse_whenNoAdaptersRegistered() {
    when(frontendService.getTableAssetAdapter()).thenReturn(null);

    boolean result = tableAssetsService.testConnection("unknown-source");

    assertFalse(result);
  }

  @Test
  @SuppressWarnings("unchecked")
  void testConnection_delegatesToMatchingAdapter() {
    TableAssetsAdapter<Game> adapter = mock(TableAssetsAdapter.class);
    TableAssetSource source = mock(TableAssetSource.class);
    when(source.getId()).thenReturn("my-source");
    when(adapter.getAssetSource()).thenReturn(source);
    when(adapter.testConnection()).thenReturn(true);
    when(frontendService.getTableAssetAdapter()).thenReturn(adapter);

    boolean result = tableAssetsService.testConnection("my-source");

    assertTrue(result);
    verify(adapter).testConnection();
  }

  @Test
  @SuppressWarnings("unchecked")
  void testConnection_returnsFalse_whenAdapterIdDoesNotMatch() {
    TableAssetsAdapter<Game> adapter = mock(TableAssetsAdapter.class);
    TableAssetSource source = mock(TableAssetSource.class);
    when(source.getId()).thenReturn("other-source");
    when(adapter.getAssetSource()).thenReturn(source);
    when(frontendService.getTableAssetAdapter()).thenReturn(adapter);

    boolean result = tableAssetsService.testConnection("my-source");

    assertFalse(result);
    verify(adapter, never()).testConnection();
  }

  // ---- invalidateMediaCache ----

  @Test
  void invalidateMediaCache_returnsTrue_whenNoAdapters() {
    when(frontendService.getTableAssetAdapter()).thenReturn(null);

    boolean result = tableAssetsService.invalidateMediaCache("any-source");

    assertTrue(result);
  }

  @Test
  @SuppressWarnings("unchecked")
  void invalidateMediaCache_callsInvalidateOnMatchingAdapter() {
    TableAssetsAdapter<Game> adapter = mock(TableAssetsAdapter.class);
    TableAssetSource source = mock(TableAssetSource.class);
    when(source.getId()).thenReturn("src1");
    when(adapter.getAssetSource()).thenReturn(source);
    when(frontendService.getTableAssetAdapter()).thenReturn(adapter);

    boolean result = tableAssetsService.invalidateMediaCache("src1");

    assertTrue(result);
    verify(adapter).invalidateMediaCache();
  }

  // ---- invalidateMediaSources ----

  @Test
  @SuppressWarnings("unchecked")
  void invalidateMediaSources_rebuildsAdapterList() {
    TableAssetSource source1 = mock(TableAssetSource.class);
    TableAssetsAdapter<Game> adapter1 = mock(TableAssetsAdapter.class);
    when(tableAssetAdapterFactory.createAdapter(vpsService, source1)).thenReturn(adapter1);

    tableAssetsService.invalidateMediaSources(List.of(source1));

    verify(tableAssetAdapterFactory).createAdapter(vpsService, source1);
  }

  @Test
  @SuppressWarnings("unchecked")
  void invalidateMediaSources_clearsOldAdapters() {
    // Prime with one adapter first
    TableAssetSource old = mock(TableAssetSource.class);
    TableAssetsAdapter<Game> oldAdapter = mock(TableAssetsAdapter.class);
    when(tableAssetAdapterFactory.createAdapter(vpsService, old)).thenReturn(oldAdapter);
    tableAssetsService.invalidateMediaSources(List.of(old));

    // Now replace with an empty list
    tableAssetsService.invalidateMediaSources(List.of());

    // The old adapter should no longer be used for testConnection
    when(frontendService.getTableAssetAdapter()).thenReturn(null);
    assertFalse(tableAssetsService.testConnection("any"));
  }

  // ---- get ----

  @Test
  void get_returnsEmpty_whenNoAdapters() throws Exception {
    when(frontendService.getTableAssetAdapter()).thenReturn(null);

    Optional<?> result = tableAssetsService.get(null,
        de.mephisto.vpin.restclient.frontend.EmulatorType.VisualPinball,
        "playfield", null, "folder", "file.png");

    assertTrue(result.isEmpty());
  }
}
