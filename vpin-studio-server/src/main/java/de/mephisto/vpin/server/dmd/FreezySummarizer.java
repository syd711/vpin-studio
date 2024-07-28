package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.restclient.components.ComponentSummary;
import de.mephisto.vpin.restclient.components.ComponentType;
import de.mephisto.vpin.server.games.GameEmulator;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

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

      String defaultEncoding = "UTF-8";
      FileInputStream in = new FileInputStream(iniFile);
      BOMInputStream bOMInputStream = new BOMInputStream(in);
      ByteOrderMark bom = bOMInputStream.getBOM();
      String charsetName = bom == null ? defaultEncoding : bom.getCharsetName();
      InputStreamReader reader = new InputStreamReader(new BufferedInputStream(bOMInputStream), charsetName);

      INIConfiguration iniConfiguration = new INIConfiguration();
      iniConfiguration.setCommentLeadingCharsUsedInInput(";");
      iniConfiguration.setSeparatorUsedInOutput("=");
      iniConfiguration.setSeparatorUsedInInput("=");

      try {
        iniConfiguration.read(reader);
      } catch (Exception e) {
        LOG.error("Failed to read: " + iniFile.getAbsolutePath() + ": " + e.getMessage(), e);
        throw e;
      } finally {
        in.close();
        reader.close();
      }

      SubnodeConfiguration globalSection = iniConfiguration.getSection("global");
      String vniKey = globalSection.getString("vni..key");
      if (StringUtils.isEmpty(vniKey)) {
        summary.addEntry("VNI Key", null, false, "\"vni.key\" not set in " + iniFile.getAbsolutePath(), "The \"vni.key\" must be set so that .pac files can be decoded.");
      }
      else if (!vniKey.equals("f0ad135937ffa111c60b24d88ebb2e59")) {
        summary.addEntry("VNI Key", vniKey + " invalid, must be \"f0ad135937ffa111c60b24d88ebb2e59\"", false, null, "The \"vni.key\" must be set so that .pac files can be decoded.");
      }
      else {
        summary.addEntry("VNI Key", vniKey + " (pac file colorizations are enabled)", true, null, "The \"vni.key\" must be set so that .pac files can be decoded.");
      }

      int pluginIndex = 0;
      String key = "plugin.." + pluginIndex + "..path";
      while (iniConfiguration.containsKey(key)) {
        String path = iniConfiguration.getString(key);
        boolean exists = new File(path).exists();
        summary.addEntry("Plugin (" + pluginIndex + ") Path", path, exists, exists ? path : "The .dll file \"" + path + "\" does not exist, fix the path in the DmdDevice.ini.");

        String key64 = "plugin.." + pluginIndex + "..path64";
        if (iniConfiguration.containsKey(key64)) {
          String path64 = iniConfiguration.getString(key64);
          exists = new File(path64).exists();
          summary.addEntry("Plugin (" + pluginIndex + ") Path 64 ", path64, exists, exists ? path64 : "The .dll file \"" + path64 + "\" does not exist, fix the path in the DmdDevice.ini.");
        }

        String passthroughKey = "plugin.." + pluginIndex + "..passthrough";
        if (iniConfiguration.containsKey(passthroughKey)) {
          String passthrough = iniConfiguration.getString(passthroughKey);
          summary.addEntry("Plugin (" + pluginIndex + ") Passthrough", passthrough, true, "");
        }

        pluginIndex++;
        key = "plugin." + pluginIndex + ".path";
      }

//      String env = System.getenv("DMDDEVICE_CONFIG");
//      if (StringUtils.isEmpty(env)) {
//        summary.addEntry("DMDDEVICE_CONFIG", null, false, "The environment variable 'DMDDEVICE_CONFIG' is not set and must point to the DmdDevice.ini file.");
//      }
//      else {
//        File settings = new File(env);
//        if (!settings.exists()) {
//          summary.addEntry("DMDDEVICE_CONFIG", null, false, "Invalid path: " + settings.getAbsolutePath());
//        }
//        else {
//          summary.addEntry("DMDDEVICE_CONFIG", settings.getAbsolutePath(), true, null, "This environment variable must point to the DmdDevice.ini file.");
//        }
//      }

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
      LOG.error("Failed to load " + iniFile.getAbsolutePath() + ": " + e.getMessage(), e);
      summary.setError("Error creating freezy summary: " + e.getMessage());
    }
    return summary;
  }
}
