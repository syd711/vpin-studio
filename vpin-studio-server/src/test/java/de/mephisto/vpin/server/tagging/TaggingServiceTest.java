package de.mephisto.vpin.server.tagging;

import de.mephisto.vpin.restclient.frontend.TableDetails;
import de.mephisto.vpin.restclient.tagging.TaggingSettings;
import de.mephisto.vpin.server.frontend.FrontendService;
import de.mephisto.vpin.server.games.*;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaggingServiceTest {

  @Mock
  private FrontendService frontendService;

  @Mock
  private GameLifecycleService gameLifecycleService;

  @Mock
  private PreferencesService preferencesService;

  @InjectMocks
  private TaggingService taggingService;

  @BeforeEach
  void clearStaticCache() {
    // TaggingService.tags is a static Set — clear it before each test
    // by calling clearCache() with mocked frontendService returning no games
    when(frontendService.getGameIds()).thenReturn(Collections.emptyList());
    taggingService.clearCache();
  }

  // ---- getTags / clearCache ----

  @Test
  void getTags_returnsEmptyList_whenNoCachedTags() {
    List<String> tags = taggingService.getTags();
    assertTrue(tags.isEmpty());
  }

  @Test
  void clearCache_populatesTagsFromAllGames() {
    TableDetails details = new TableDetails();
    details.setTags("Action,Classic");

    when(frontendService.getGameIds()).thenReturn(List.of(1));
    when(frontendService.getTableDetails(1)).thenReturn(details);

    taggingService.clearCache();

    List<String> tags = taggingService.getTags();
    assertTrue(tags.contains("Action"));
    assertTrue(tags.contains("Classic"));
  }

  @Test
  void clearCache_ignoresGameWithNullTableDetails() {
    when(frontendService.getGameIds()).thenReturn(List.of(1));
    when(frontendService.getTableDetails(1)).thenReturn(null);

    taggingService.clearCache(); // must not throw

    assertTrue(taggingService.getTags().isEmpty());
  }

  // ---- gameDataChanged ----

  @Test
  void gameDataChanged_removesOldTagsAndAddsNewTags() {
    // Seed cache with an old tag
    TableDetails priorDetails = new TableDetails();
    priorDetails.setTags("OldTag");
    when(frontendService.getGameIds()).thenReturn(List.of(1));
    when(frontendService.getTableDetails(1)).thenReturn(priorDetails);
    taggingService.clearCache();

    // Simulate tag change: OldTag → NewTag
    TableDetails oldData = new TableDetails();
    oldData.setTags("OldTag");
    TableDetails newData = new TableDetails();
    newData.setTags("NewTag");

    GameDataChangedEvent event = new GameDataChangedEvent(1, oldData, newData);
    taggingService.gameDataChanged(event);

    List<String> tags = taggingService.getTags();
    assertFalse(tags.contains("OldTag"), "OldTag should have been removed");
    assertTrue(tags.contains("NewTag"), "NewTag should have been added");
  }

  @Test
  void gameDataChanged_handlesEmptyOldTags() {
    TableDetails oldData = new TableDetails();
    oldData.setTags("");
    TableDetails newData = new TableDetails();
    newData.setTags("FreshTag");

    taggingService.gameDataChanged(new GameDataChangedEvent(1, oldData, newData));

    assertTrue(taggingService.getTags().contains("FreshTag"));
  }

  // ---- gameDeleted ----

  @Test
  void gameDeleted_clearsCacheAndReloads() {
    TableDetails details = new TableDetails();
    details.setTags("AfterDelete");
    when(frontendService.getGameIds()).thenReturn(List.of(2));
    when(frontendService.getTableDetails(2)).thenReturn(details);

    taggingService.gameDeleted(99);

    assertTrue(taggingService.getTags().contains("AfterDelete"));
    verify(frontendService, atLeastOnce()).getGameIds();
  }

  // ---- gameCreated (auto-tag) ----

  @Test
  void gameCreated_doesNotAutoTag_whenAutoTagTablesDisabled() throws Exception {
    TaggingSettings settings = new TaggingSettings();
    settings.setAutoTagTablesEnabled(false);
    when(preferencesService.getJsonPreference(anyString())).thenReturn(settings);

    taggingService.preferenceChanged(de.mephisto.vpin.restclient.PreferenceNames.TAGGING_SETTINGS, null, null);
    taggingService.gameCreated(10);

    verify(frontendService, never()).getTableDetails(10);
  }

  @Test
  void gameCreated_autoTags_whenEnabledAndTableTagsPresent() throws Exception {
    TaggingSettings settings = new TaggingSettings();
    settings.setAutoTagTablesEnabled(true);
    settings.setTableTags(Arrays.asList("Auto"));
    when(preferencesService.getJsonPreference(anyString())).thenReturn(settings);

    TableDetails details = new TableDetails();
    details.setTags("");
    when(frontendService.getTableDetails(10)).thenReturn(details);

    taggingService.preferenceChanged(de.mephisto.vpin.restclient.PreferenceNames.TAGGING_SETTINGS, null, null);
    taggingService.gameCreated(10);

    verify(frontendService).saveTableDetails(eq(10), any());
  }

  @Test
  void gameCreated_doesNotSave_whenTagAlreadyPresent() throws Exception {
    TaggingSettings settings = new TaggingSettings();
    settings.setAutoTagTablesEnabled(true);
    settings.setTableTags(Arrays.asList("Existing"));
    when(preferencesService.getJsonPreference(anyString())).thenReturn(settings);

    TableDetails details = new TableDetails();
    details.setTags("Existing");
    when(frontendService.getTableDetails(5)).thenReturn(details);

    taggingService.preferenceChanged(de.mephisto.vpin.restclient.PreferenceNames.TAGGING_SETTINGS, null, null);
    taggingService.gameCreated(5);

    verify(frontendService, never()).saveTableDetails(anyInt(), any());
  }
}
