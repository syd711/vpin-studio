package de.mephisto.vpin.server.components;

import de.mephisto.githubloader.GithubRelease;
import de.mephisto.vpin.restclient.PreferenceNames;
import de.mephisto.vpin.restclient.components.ComponentRepresentation;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.popper.PinUPConnector;
import de.mephisto.vpin.server.preferences.PreferencesService;
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

    ComponentType componentType = component.getType();
    GithubRelease latestRelease = componentService.getLatestRelease(componentType);

    ComponentRepresentation representation = new ComponentRepresentation();
    representation.setType(componentType);
    representation.setUrl(latestRelease.getReleasesUrl());
    representation.setInstalledVersion(component.getInstalledVersion());
    representation.setLatestReleaseVersion(component.getLatestReleaseVersion());
    representation.setLastCheck(component.getLastCheck());
    representation.setArtifacts(latestRelease.getArtifacts().stream().map(a -> a.getName()).collect(Collectors.toList()));

    switch (componentType) {
      case vpinmame: {
        File setupExe = new File(pinUPConnector.getDefaultGameEmulator().getMameFolder(), "Setup.exe");
        if (systemPreset == null || systemPreset.equals(PreferenceNames.SYSTEM_PRESET_64_BIT)) {
          setupExe = new File(pinUPConnector.getDefaultGameEmulator().getMameFolder(), "Setup64.exe");
        }
        if (setupExe.exists()) {
          representation.setLastModified(new Date(setupExe.lastModified()));
        }
        break;
      }
      case vpinball: {
        File file = new File(pinUPConnector.getDefaultGameEmulator().getInstallationFolder(), "VisualPinballX64.exe");
        if (!file.exists()) {
          file = new File(pinUPConnector.getDefaultGameEmulator().getMameFolder(), "VisualPinballX.exe");
        }

        if (file.exists()) {
          representation.setLastModified(new Date(file.lastModified()));
        }
        break;
      }
      case b2sbackglass: {
        File file = new File(pinUPConnector.getDefaultGameEmulator().getTablesFolder(), "B2SBackglassServer.dll");
        if (file.exists()) {
          representation.setLastModified(new Date(file.lastModified()));
        }
        break;
      }
      case flexdmd: {
        File file = new File(pinUPConnector.getDefaultGameEmulator().getMameFolder(), "FlexDMD.dll");
        if (file.exists()) {
          representation.setLastModified(new Date(file.lastModified()));
        }
        break;
      }
      case freezy: {
        File file = new File(pinUPConnector.getDefaultGameEmulator().getMameFolder(), "DmdDevice64.dll");
        if (!file.exists()) {
          file = new File(pinUPConnector.getDefaultGameEmulator().getMameFolder(), "DmdDevice.dll");
        }

        if (file.exists()) {
          representation.setLastModified(new Date(file.lastModified()));
        }
        break;
      }
      case serum: {
        File file = new File(pinUPConnector.getDefaultGameEmulator().getMameFolder(), "serum.lib");
        if (file.exists()) {
          representation.setLastModified(new Date(file.lastModified()));
        }
        break;
      }
      default: {
        throw new UnsupportedOperationException("Invalid component type " + componentType);
      }
    }

    return representation;
  }
}
