package de.mephisto.vpin.server.highscores.cards;

import de.mephisto.vpin.restclient.cards.CardTemplateType;
import de.mephisto.vpin.server.games.Game;
import de.mephisto.vpin.server.games.GameService;
import de.mephisto.vpin.server.preferences.PreferencesService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CardTemplatesServiceTest {

  @Mock
  private TemplateMappingRepository templateMappingRepository;
  @Mock
  private PreferencesService preferencesService;
  @Mock
  private GameService gameService;
  @Mock
  private TemplateMerger templateMerger;

  @InjectMocks
  private CardTemplatesService service;

  // ---- getTemplate ----

  @Test
  void getTemplate_nullId_returnsNull() {
    assertThat(service.getTemplate(null)).isNull();
  }

  @Test
  void getTemplate_nonExistentId_returnsNull() {
    when(templateMappingRepository.findById(99L)).thenReturn(Optional.empty());

    assertThat(service.getTemplate(99L)).isNull();
  }

  // ---- delete ----

  @Test
  void delete_nonExistentId_returnsFalse() {
    when(templateMappingRepository.findById(99L)).thenReturn(Optional.empty());

    assertThat(service.delete(99L)).isFalse();
  }

  @Test
  void delete_existingId_deletesAndReturnsTrue() {
    TemplateMapping mapping = mock(TemplateMapping.class);
    when(templateMappingRepository.findById(1L)).thenReturn(Optional.of(mapping));

    boolean result = service.delete(1L);

    assertThat(result).isTrue();
    verify(templateMappingRepository).deleteById(1L);
  }

  // ---- assignTemplate ----

  @Test
  void assignTemplate_gameNotFound_returnsFalse() throws Exception {
    when(gameService.getGame(1)).thenReturn(null);

    assertThat(service.assignTemplate(1, 10L, false, CardTemplateType.HIGSCORE_CARD)).isFalse();
  }

  @Test
  void assignTemplate_templateNotFound_returnsFalse() throws Exception {
    Game game = mock(Game.class);
    when(gameService.getGame(1)).thenReturn(game);
    when(templateMappingRepository.findById(10L)).thenReturn(Optional.empty());

    assertThat(service.assignTemplate(1, 10L, false, CardTemplateType.HIGSCORE_CARD)).isFalse();
  }
}
