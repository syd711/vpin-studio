package de.mephisto.vpin.server.games;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.IncorrectResultSizeDataAccessException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameDetailsRepositoryServiceTest {

  @Mock
  private GameDetailsRepository gameDetailsRepository;

  @InjectMocks
  private GameDetailsRepositoryService service;

  // ---- findByPupId ----

  @Test
  void findByPupId_returnsResult_whenFound() {
    GameDetails expected = new GameDetails();
    when(gameDetailsRepository.findByPupId(1)).thenReturn(expected);

    GameDetails result = service.findByPupId(1);

    assertSame(expected, result);
  }

  @Test
  void findByPupId_deletesFirstDuplicate_andReturnsRemaining_onIncorrectResultSizeException() {
    GameDetails duplicate = new GameDetails();
    GameDetails remaining = new GameDetails();

    when(gameDetailsRepository.findByPupId(2))
        .thenThrow(new IncorrectResultSizeDataAccessException(1))
        .thenReturn(remaining);
    when(gameDetailsRepository.findAllByPupId(2)).thenReturn(List.of(duplicate));

    GameDetails result = service.findByPupId(2);

    verify(gameDetailsRepository).delete(duplicate);
    assertSame(remaining, result);
  }

  @Test
  void findByPupId_returnsNull_whenDuplicateListIsEmpty() {
    when(gameDetailsRepository.findByPupId(3))
        .thenThrow(new IncorrectResultSizeDataAccessException(1));
    when(gameDetailsRepository.findAllByPupId(3)).thenReturn(Collections.emptyList());

    GameDetails result = service.findByPupId(3);

    assertNull(result);
    verify(gameDetailsRepository, never()).delete(any(GameDetails.class));
  }

  // ---- findByRomName ----

  @Test
  void findByRomName_delegatesToRepository() {
    GameDetails gd = new GameDetails();
    when(gameDetailsRepository.findByRomName("taf")).thenReturn(List.of(gd));

    List<GameDetails> result = service.findByRomName("taf");

    assertEquals(1, result.size());
    assertSame(gd, result.get(0));
  }

  // ---- saveAndFlush ----

  @Test
  void saveAndFlush_delegatesToRepository() {
    GameDetails gd = new GameDetails();

    service.saveAndFlush(gd);

    verify(gameDetailsRepository).saveAndFlush(gd);
  }

  // ---- delete ----

  @Test
  void delete_delegatesToRepository() {
    GameDetails gd = new GameDetails();

    service.delete(gd);

    verify(gameDetailsRepository).delete(gd);
  }

  // ---- findAll ----

  @Test
  void findAll_delegatesToRepository() {
    GameDetails gd = new GameDetails();
    when(gameDetailsRepository.findAll()).thenReturn(List.of(gd));

    List<GameDetails> result = service.findAll();

    assertEquals(1, result.size());
  }

  // ---- deleteAll ----

  @Test
  void deleteAll_delegatesToRepository() {
    service.deleteAll();

    verify(gameDetailsRepository).deleteAll();
  }

  // ---- findAllPupIds ----

  @Test
  void findAllPupIds_delegatesToRepository() {
    when(gameDetailsRepository.findAllPupIds()).thenReturn(List.of(10, 20));

    List<Integer> result = service.findAllPupIds();

    assertEquals(List.of(10, 20), result);
  }

  // ---- deleteByPupId ----

  @Test
  void deleteByPupId_callsRepositoryOnce_forSmallList() {
    List<Long> ids = List.of(1L, 2L, 3L);

    service.deleteByPupId(ids);

    verify(gameDetailsRepository).deleteByPupId(ids);
  }

  @Test
  void deleteByPupId_batchesIn999_forLargeList() {
    List<Long> ids = new ArrayList<>();
    for (int i = 0; i < 2000; i++) {
      ids.add((long) i);
    }

    service.deleteByPupId(ids);

    // 2000 ids → batches of 999: [0..998], [999..1997], [1998..1999]
    verify(gameDetailsRepository, times(3)).deleteByPupId(anyList());
  }
}
