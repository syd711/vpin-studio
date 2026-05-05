package de.mephisto.vpin.server.vpxz;

import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.jobs.JobService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VPXZServiceTest {

  @Mock
  private GameService gameService;
  @Mock
  private VPXZSourceRepository vpxzSourceRepository;
  @Mock
  private FrontendService frontendService;
  @Mock
  private JobService jobService;
  @Mock
  private VPXZFileService vpxzFileService;
  @Mock
  private PreferencesService preferencesService;

  @InjectMocks
  private VPXZService service;

  private VPXZSourceAdapter mockAdapter;
  private VPXZSource mockSource;

  @BeforeEach
  void setUp() throws Exception {
    mockAdapter = mock(VPXZSourceAdapter.class);
    mockSource = new VPXZSource();
    mockSource.setEnabled(true);
    when(mockAdapter.getVPXZSource()).thenReturn(mockSource);

    setCache(1L, mockAdapter);
  }

  @SuppressWarnings("unchecked")
  private void setCache(Long id, VPXZSourceAdapter adapter) throws Exception {
    Field f = VPXZService.class.getDeclaredField("vpxMobileSourcesCache");
    f.setAccessible(true);
    Map<Long, VPXZSourceAdapter> cache = (Map<Long, VPXZSourceAdapter>) f.get(service);
    cache.put(id, adapter);
  }

  // ---- cancelInstall / progressInstall ----

  @Test
  void cancelInstall_returnsTrue() {
    assertTrue(service.cancelInstall());
  }

  @Test
  void progressInstall_returnsNegativeOne_afterCancel() {
    service.cancelInstall();
    assertEquals(-1.0, service.progressInstall(), 0.001);
  }

  @Test
  void progressInstall_initiallyZero() {
    assertEquals(0.0, service.progressInstall(), 0.001);
  }

  // ---- getVPXMobileSources ----

  @Test
  void getVPXMobileSources_returnsSourcesFromCache() {
    List<VPXZSource> sources = service.getVPXMobileSources();
    assertEquals(1, sources.size());
    assertSame(mockSource, sources.get(0));
  }

  // ---- getVPXMobileSourceAdapter ----

  @Test
  void getVPXMobileSourceAdapter_returnsAdapter_whenPresent() {
    VPXZSourceAdapter adapter = service.getVPXMobileSourceAdapter(1L);
    assertSame(mockAdapter, adapter);
  }

  @Test
  void getVPXMobileSourceAdapter_returnsNull_whenAbsent() {
    assertNull(service.getVPXMobileSourceAdapter(99L));
  }

  // ---- deleteVPXMobileSource ----

  @Test
  void deleteVPXMobileSource_removesFromCacheAndRepository_whenPresent() {
    boolean result = service.deleteVPXMobileSource(1L);
    assertTrue(result);
    assertNull(service.getVPXMobileSourceAdapter(1L));
    verify(vpxzSourceRepository).deleteById(1L);
  }

  @Test
  void deleteVPXMobileSource_returnsFalse_whenAbsent() {
    boolean result = service.deleteVPXMobileSource(99L);
    assertFalse(result);
    verifyNoInteractions(vpxzSourceRepository);
  }

  // ---- invalidateCache ----

  @Test
  void invalidateCache_callsInvalidateOnAllAdapters() {
    service.invalidateCache();
    verify(mockAdapter).invalidate();
  }
}
