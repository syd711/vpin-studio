package de.mephisto.vpin.server.emulators;

import de.mephisto.vpin.restclient.JsonSettings;
import de.mephisto.vpin.restclient.emulators.GameEmulatorScript;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.invoke.MethodHandles;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Service
public class EmulatorDetailsService {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  @Autowired
  public EmulatorDetailsRepository emulatorDetailsRepository;

  @NonNull
  public GameEmulatorScript cloneScript(@Nullable GameEmulatorScript script) {
    if (script == null) {
      return new GameEmulatorScript();
    }
    try {
      String json = JsonSettings.objectMapper.writeValueAsString(script);
      return JsonSettings.fromJson(GameEmulatorScript.class, json);
    }
    catch (Exception e) {
      LOG.error("Failed to clone emulator script: {}", e.getMessage());
    }
    return null;
  }

  public GameEmulatorScript saveEmulatorVRLaunchScript(int emulatorId, @NonNull GameEmulatorScript script) {
    return saveScript(emulatorId, script, EmulatorDetails::setVrLaunchScript, "VR");
  }

  public GameEmulatorScript saveEmulatorLaunchScript(int emulatorId, @NonNull GameEmulatorScript script) {
    return saveScript(emulatorId, script, EmulatorDetails::setOriginalLaunchScript, "original");
  }

  @Nullable
  public GameEmulatorScript getGameEmulatorLaunchScript(int emulatorId) {
    return readScript(emulatorId, EmulatorDetails::getOriginalLaunchScript);
  }

  @Nullable
  public GameEmulatorScript getGameEmulatorVRLaunchScript(int emulatorId) {
    return readScript(emulatorId, EmulatorDetails::getVrLaunchScript);
  }

  private GameEmulatorScript saveScript(int emulatorId, GameEmulatorScript script, BiConsumer<EmulatorDetails, String> setter, String label) {
    try {
      EmulatorDetails details = emulatorDetailsRepository.findByEmulatorId(emulatorId)
          .orElseGet(() -> {
            EmulatorDetails d = new EmulatorDetails();
            d.setEmulatorId(emulatorId);
            return d;
          });
      setter.accept(details, JsonSettings.objectMapper.writeValueAsString(script));
      emulatorDetailsRepository.saveAndFlush(details);
      return script;
    }
    catch (Exception e) {
      LOG.error("Failed to write {} script json from emulator details {}: {}", label, emulatorId, e.getMessage(), e);
    }
    return null;
  }

  @Nullable
  private GameEmulatorScript readScript(int emulatorId, Function<EmulatorDetails, String> getter) {
    return emulatorDetailsRepository.findByEmulatorId(emulatorId)
        .map(getter)
        .filter(json -> !StringUtils.isEmpty(json))
        .map(json -> {
          try {
            return JsonSettings.fromJson(GameEmulatorScript.class, json);
          }
          catch (Exception e) {
            LOG.error("Failed to read script json from emulator details {}: {}", emulatorId, e.getMessage(), e);
            return null;
          }
        })
        .orElse(null);
  }
}