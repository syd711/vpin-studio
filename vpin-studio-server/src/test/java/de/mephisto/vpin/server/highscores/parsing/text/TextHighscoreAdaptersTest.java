package de.mephisto.vpin.server.highscores.parsing.text;

import de.mephisto.vpin.restclient.system.ScoringDB;
import de.mephisto.vpin.server.system.SystemService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TextHighscoreAdaptersTest {

  @Mock
  private SystemService systemService;

  @InjectMocks
  private TextHighscoreAdapters service;

  @Test
  void adapters_staticList_containsBuiltInAdapters() {
    assertThat(TextHighscoreAdapters.adapters).isNotEmpty();
  }

  @Test
  void resetHighscores_returnsFalse_whenFileIsIgnored() {
    ScoringDB scoringDB = mock(ScoringDB.class);
    when(scoringDB.getIgnoredTextFiles()).thenReturn(Arrays.asList("ignored.txt"));

    boolean result = service.resetHighscores(scoringDB, new File("ignored.txt"), 0L);

    assertFalse(result);
  }

  @Test
  void resetHighscores_returnsFalse_whenFileDoesNotExist() {
    ScoringDB scoringDB = mock(ScoringDB.class);
    when(scoringDB.getIgnoredTextFiles()).thenReturn(Collections.emptyList());

    File nonExistent = new File(System.getProperty("java.io.tmpdir"), "nonexistent-score-" + System.nanoTime() + ".txt");
    boolean result = service.resetHighscores(scoringDB, nonExistent, 0L);

    assertFalse(result);
  }

  @Test
  void convertTextFileTextToMachineReadable_returnsNull_whenFileIsIgnored() {
    ScoringDB scoringDB = mock(ScoringDB.class);
    when(scoringDB.getIgnoredTextFiles()).thenReturn(Arrays.asList("ignored.txt"));

    String result = service.convertTextFileTextToMachineReadable(null, scoringDB, new File("ignored.txt"));

    assertNull(result);
  }

  @Test
  void afterPropertiesSet_loadsAdaptersFromScoringDatabase() {
    ScoringDB scoringDB = mock(ScoringDB.class);
    when(scoringDB.getHighscoreTextParsers()).thenReturn(Collections.emptyList());
    when(systemService.getScoringDatabase()).thenReturn(scoringDB);

    service.afterPropertiesSet();

    verify(systemService).getScoringDatabase();
    verify(scoringDB).getHighscoreTextParsers();
  }
}
