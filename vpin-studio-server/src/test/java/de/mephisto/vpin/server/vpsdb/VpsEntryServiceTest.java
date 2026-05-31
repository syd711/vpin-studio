package de.mephisto.vpin.server.vpsdb;

import de.mephisto.vpin.connectors.vps.model.VpsTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VpsEntryServiceTest {

  @Mock
  private VpsEntriesRepository vpsEntriesRepository;

  @InjectMocks
  private VpsEntryService vpsEntryService;

  // ---- getVpsEntry ----

  @Test
  void getVpsEntry_returnsEntryFromRepository() {
    VpsDbEntry entry = new VpsDbEntry();
    entry.setVpsTableId("table-123");
    when(vpsEntriesRepository.findByVpsTableId("table-123")).thenReturn(entry);

    VpsDbEntry result = vpsEntryService.getVpsEntry("table-123");

    assertNotNull(result);
    assertEquals("table-123", result.getVpsTableId());
  }

  @Test
  void getVpsEntry_returnsNull_whenNotFound() {
    when(vpsEntriesRepository.findByVpsTableId("unknown")).thenReturn(null);

    VpsDbEntry result = vpsEntryService.getVpsEntry("unknown");

    assertNull(result);
  }

  // ---- getAllVpsEntries ----

  @Test
  void getAllVpsEntries_returnsAllEntriesFromRepository() {
    VpsDbEntry e1 = new VpsDbEntry();
    VpsDbEntry e2 = new VpsDbEntry();
    when(vpsEntriesRepository.findAll()).thenReturn(Arrays.asList(e1, e2));

    List<VpsDbEntry> result = vpsEntryService.getAllVpsEntries();

    assertEquals(2, result.size());
  }

  @Test
  void getAllVpsEntries_returnsEmptyList_whenRepositoryIsEmpty() {
    when(vpsEntriesRepository.findAll()).thenReturn(Collections.emptyList());

    List<VpsDbEntry> result = vpsEntryService.getAllVpsEntries();

    assertTrue(result.isEmpty());
  }

  // ---- save(VpsTable) — creates new entry when not found ----

  @Test
  void save_vpsTable_createsNewEntry_whenNotFound() {
    VpsTable vpsTable = new VpsTable();
    vpsTable.setId("new-table");
    vpsTable.setComment("some comment");

    when(vpsEntriesRepository.findByVpsTableId("new-table")).thenReturn(null);

    vpsEntryService.save(vpsTable);

    ArgumentCaptor<VpsDbEntry> captor = ArgumentCaptor.forClass(VpsDbEntry.class);
    verify(vpsEntriesRepository).saveAndFlush(captor.capture());
    VpsDbEntry saved = captor.getValue();
    assertEquals("new-table", saved.getVpsTableId());
    assertEquals("some comment", saved.getComment());
  }

  @Test
  void save_vpsTable_updatesExistingEntry() {
    VpsTable vpsTable = new VpsTable();
    vpsTable.setId("existing-table");
    vpsTable.setComment("updated comment");

    VpsDbEntry existing = new VpsDbEntry();
    existing.setVpsTableId("existing-table");
    existing.setComment("old comment");
    when(vpsEntriesRepository.findByVpsTableId("existing-table")).thenReturn(existing);

    vpsEntryService.save(vpsTable);

    ArgumentCaptor<VpsDbEntry> captor = ArgumentCaptor.forClass(VpsDbEntry.class);
    verify(vpsEntriesRepository).saveAndFlush(captor.capture());
    assertEquals("updated comment", captor.getValue().getComment());
  }

  // ---- save(VpsDbEntry) ----

  @Test
  void save_vpsDbEntry_callsRepositorySaveAndFlush() {
    VpsDbEntry entry = new VpsDbEntry();
    entry.setVpsTableId("direct-save");

    vpsEntryService.save(entry);

    verify(vpsEntriesRepository).saveAndFlush(entry);
  }

  @Test
  void save_vpsDbEntry_passesExactInstance() {
    VpsDbEntry entry = new VpsDbEntry();

    vpsEntryService.save(entry);

    verify(vpsEntriesRepository).saveAndFlush(same(entry));
  }
}
