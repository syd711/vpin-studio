package de.mephisto.vpin.server.emulators;

import de.mephisto.vpin.restclient.emulators.EmulatorValidation;
import de.mephisto.vpin.restclient.frontend.EmulatorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class EmulatorFactoryTest {

  @InjectMocks
  private EmulatorFactory emulatorFactory;

  // ---- create — simple paths that do not require Spring beans ----

  @Test
  void create_ZenFX2_returnsEnabledEmulatorWithCorrectType() {
    EmulatorValidation result = emulatorFactory.create(EmulatorType.ZenFX2);

    assertThat(result).isNotNull();
    assertThat(result.getGameEmulator()).isNotNull();
    assertThat(result.getGameEmulator().getType()).isEqualTo(EmulatorType.ZenFX2);
    assertThat(result.getGameEmulator().isEnabled()).isTrue();
  }

  @Test
  void create_PinballArcade_returnsEnabledEmulatorWithCorrectType() {
    EmulatorValidation result = emulatorFactory.create(EmulatorType.PinballArcade);

    assertThat(result).isNotNull();
    assertThat(result.getGameEmulator()).isNotNull();
    assertThat(result.getGameEmulator().getType()).isEqualTo(EmulatorType.PinballArcade);
    assertThat(result.getGameEmulator().isEnabled()).isTrue();
  }
}
