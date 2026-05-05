package de.mephisto.vpin.server.emulators;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmulatorDetailsServiceTest {

  @Mock
  EmulatorDetailsRepository emulatorDetailsRepository;

  @InjectMocks
  private EmulatorDetailsService emulatorDetailsService;

  @Test
  void cloneScript_returnsEmptyScript_whenNull() {
    GameEmulatorScript result = emulatorDetailsService.cloneScript(null);

    assertNotNull(result);
    assertNull(result.getScript());
  }

  @Test
  void cloneScript_returnsDeepCopy_whenScriptProvided() {
    GameEmulatorScript original = new GameEmulatorScript();
    original.setScript("@echo off\nSTART something");

    GameEmulatorScript result = emulatorDetailsService.cloneScript(original);

    assertNotNull(result);
    assertEquals(original.getScript(), result.getScript());
    assertNotSame(original, result);
  }

  @Test
  void getGameEmulatorLaunchScript_returnsNull_whenNoDetailsExist() {
    when(emulatorDetailsRepository.findByEmulatorId(1)).thenReturn(Optional.empty());

    GameEmulatorScript result = emulatorDetailsService.getGameEmulatorLaunchScript(1);

    assertNull(result);
  }

  @Test
  void getGameEmulatorLaunchScript_returnsNull_whenStoredJsonIsEmpty() {
    EmulatorDetails details = new EmulatorDetails();
    details.setEmulatorId(1);
    details.setOriginalLaunchScript("");
    when(emulatorDetailsRepository.findByEmulatorId(1)).thenReturn(Optional.of(details));

    GameEmulatorScript result = emulatorDetailsService.getGameEmulatorLaunchScript(1);

    assertNull(result);
  }

  @Test
  void getGameEmulatorLaunchScript_returnsScript_whenValidJsonStored() throws Exception {
    GameEmulatorScript original = new GameEmulatorScript();
    original.setScript("@echo off");
    String json = JsonSettings.objectMapper.writeValueAsString(original);

    EmulatorDetails details = new EmulatorDetails();
    details.setEmulatorId(1);
    details.setOriginalLaunchScript(json);
    when(emulatorDetailsRepository.findByEmulatorId(1)).thenReturn(Optional.of(details));

    GameEmulatorScript result = emulatorDetailsService.getGameEmulatorLaunchScript(1);

    assertNotNull(result);
    assertEquals("@echo off", result.getScript());
  }

  @Test
  void saveEmulatorLaunchScript_createsNewDetails_whenNoneExist() {
    when(emulatorDetailsRepository.findByEmulatorId(5)).thenReturn(Optional.empty());
    when(emulatorDetailsRepository.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));

    GameEmulatorScript script = new GameEmulatorScript();
    script.setScript("START vpinball.exe");

    GameEmulatorScript result = emulatorDetailsService.saveEmulatorLaunchScript(5, script);

    assertSame(script, result);
    verify(emulatorDetailsRepository).saveAndFlush(any(EmulatorDetails.class));
  }

  @Test
  void saveEmulatorVRLaunchScript_updatesExistingDetails() {
    EmulatorDetails existing = new EmulatorDetails();
    existing.setEmulatorId(3);
    when(emulatorDetailsRepository.findByEmulatorId(3)).thenReturn(Optional.of(existing));
    when(emulatorDetailsRepository.saveAndFlush(any())).thenReturn(existing);

    GameEmulatorScript script = new GameEmulatorScript();
    script.setScript("VR LAUNCH SCRIPT");

    GameEmulatorScript result = emulatorDetailsService.saveEmulatorVRLaunchScript(3, script);

    assertSame(script, result);
    verify(emulatorDetailsRepository).saveAndFlush(existing);
  }

  @Test
  void getGameEmulatorVRLaunchScript_returnsNull_whenNoDetails() {
    when(emulatorDetailsRepository.findByEmulatorId(7)).thenReturn(Optional.empty());

    GameEmulatorScript result = emulatorDetailsService.getGameEmulatorVRLaunchScript(7);

    assertNull(result);
  }
}
