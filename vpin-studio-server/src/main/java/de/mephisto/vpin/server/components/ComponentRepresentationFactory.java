package de.mephisto.vpin.server.components;

import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.preferences.PreferencesService;
import de.mephisto.vpin.server.system.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.stream.Collectors;

@Service
public class ComponentRepresentationFactory {

  @Autowired
  private ComponentService componentService;

  @Autowired
  private PinUPConnector pinUPConnector;

  @Autowired
  private PreferencesService preferencesService;

  public ComponentRepresentation createComponentRepresentation(Component component) {
    String systemPreset = (String) preferencesService.getPreferenceValue(PreferenceNames.SYSTEM_PRESET);

    ComponentRepresentation representation = new ComponentRepresentation();
    ComponentType componentType = component.getType();
    representation.setType(componentType);
    representation.setInstalledVersion(component.getInstalledVersion());
    representation.setLatestReleaseVersion(component.getLatestReleaseVersion());
    representation.setLastCheck(component.getLastCheck());
    representation.setArtifacts(componentService.getLatestReleaseArtifacts(componentType).stream().map(a -> a.getName()).collect(Collectors.toList()));

    switch (componentType) {
      case vpinmame: {
        File setupExe = new File(pinUPConnector.getDefaultGameEmulator().getMameFolder(), "Setup.exe");
        if (systemPreset == null || systemPreset.equals(PreferenceNames.SYSTEM_PRESET_64_BIT)) {
          setupExe = new File(pinUPConnector.getDefaultGameEmulator().getMameFolder(), "Setup64.exe");
        }
        if (setupExe.exists()) {
          representation.setLastModified(new Date(setupExe.lastModified()));
        }
        representation.setUrl("https://github.com/vpinball/pinmame/releases");
        break;
      }
      default: {
//        throw new UnsupportedOperationException("Invalid component type " + componentType);
      }
    }

    return representation;
  }
}
