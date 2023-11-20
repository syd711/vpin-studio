package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.components.ComponentSummary;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class FreezySummarizer {
  private final static Logger LOG = LoggerFactory.getLogger(FreezySummarizer.class);


  public static ComponentSummary summarizeFreezy(@NonNull GameEmulator emulator) {
    ComponentSummary summary = new ComponentSummary();
    summary.setType(ComponentType.freezy);

    File iniFile = new File(emulator.getMameFolder(), "DmdDevice.ini");
    try {
      if (!iniFile.exists()) {
        summary.addEntry("DmdDevice.ini", null, false, "The file \"" + iniFile.getAbsolutePath() + "\" does not exist.");
        return summary;
      }

      INIConfiguration iniConfiguration = new INIConfiguration();
      iniConfiguration.setCommentLeadingCharsUsedInInput(";");
      iniConfiguration.setSeparatorUsedInOutput("=");
      iniConfiguration.setSeparatorUsedInInput("=");

      FileReader fileReader = new FileReader(iniFile);
      try {
        iniConfiguration.read(fileReader);
      } catch (Exception e) {
        LOG.error("Failed to read: " + iniFile.getAbsolutePath() + ": " + e.getMessage(), e);
        throw e;
      } finally {
        fileReader.close();
      }
      String vniKey = iniConfiguration.getString("vni..key");
      if (StringUtils.isEmpty(vniKey)) {
        summary.addEntry("VNI Key", null, false, "\"vni.key\" not set in " + iniFile.getAbsolutePath(), "The \"vni.key\" must be set so that .pac files can be decoded.");
      }
      else if(!vniKey.equals("f0ad135937ffa111c60b24d88ebb2e59")) {
        summary.addEntry("VNI Key", vniKey + " invalid, must be \"f0ad135937ffa111c60b24d88ebb2e59\"", false, null, "The \"vni.key\" must be set so that .pac files can be decoded.");
      }
      else {
        summary.addEntry("VNI Key", vniKey + " (pac file colorizations are enabled)", true, null, "The \"vni.key\" must be set so that .pac files can be decoded.");
      }

      int pluginIndex = 0;
      String key = "plugin.." + pluginIndex + "..path";
      List<String> plugins = new ArrayList<>();
      while (iniConfiguration.containsKey(key)) {
        plugins.add(iniConfiguration.getString(key));
        pluginIndex++;
        key = "plugin." + pluginIndex + ".path";
      }
      summary.addEntry("Plugins", plugins.isEmpty() ? "-" : String.join(", ", plugins), true, null, "The plugins that have been added in the DmdDevice.ini.");

      String env = System.getenv("DMDDEVICE_CONFIG");
      if (StringUtils.isEmpty(env)) {
        summary.addEntry("DMDDEVICE_CONFIG", null, false, "The environment variable 'DMDDEVICE_CONFIG' is not set and must point to the DmdDevice.ini file.");
      }
      else {
        File settings = new File(env);
        if (!settings.exists()) {
          summary.addEntry("DMDDEVICE_CONFIG", null, false, "Invalid path: " + settings.getAbsolutePath());
        }
        else {
          summary.addEntry("DMDDEVICE_CONFIG", settings.getAbsolutePath(), true, null, "This environment variable must point to the DmdDevice.ini file.");
        }
      }

      SubnodeConfiguration virtualDmdSection = iniConfiguration.getSection("virtualdmd");
      if (virtualDmdSection == null) {
        summary.addEntry("DmdDevice.ini", "Section \"virtualdmd\" not found in " + iniFile.getAbsolutePath());
      }
      else {
        String enabled = virtualDmdSection.getString("enabled");
        summary.addEntry("Virtual DMD Enabled", (enabled != null && enabled.equals("true")) ? "Yes" : "No");

        enabled = virtualDmdSection.getString("stayontop");
        summary.addEntry("Stay On Top", (enabled != null && enabled.equals("true")) ? "Yes" : "No", true, null, "If enabled, the virtual dmd stays on top of all other windows.");

        enabled = virtualDmdSection.getString("useregistry");
        summary.addEntry("Use Registry", (enabled != null && enabled.equals("true")) ? "Yes" : "No", true, null, "If enabled, DMD values (e.g. the position) will be written into the VPM's registry instead of the DmdDevice.ini.");
      }

    } catch (Exception e) {
      LOG.error("Failed to load " + iniFile.getAbsolutePath() + ": " + e.getMessage());
      summary.setError("Error creating freezy summary: " + e.getMessage());
    }
    return summary;
  }
}
