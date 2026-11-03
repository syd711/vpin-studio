package de.mephisto.vpin.server.dmd;

import de.mephisto.vpin.server.util.ServerMessages;
import de.mephisto.vpin.restclient.components.ComponentSummary;
import de.mephisto.vpin.restclient.components.ComponentType;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class FreezySummarizer {
  private final static Logger LOG = LoggerFactory.getLogger(FreezySummarizer.class);


  public static ComponentSummary summarizeFreezy(File mameFolder) {
    ComponentSummary summary = new ComponentSummary();
    summary.setType(ComponentType.freezy);

    File iniFile = new File(mameFolder, "DmdDevice.ini");
    try {
      if (!iniFile.exists()) {
        summary.addEntry("DmdDevice.ini", null, false, ServerMessages.msg("freezy.ini_missing", iniFile.getAbsolutePath()));
        return summary;
      }

      String defaultEncoding = "UTF-8";
      FileInputStream in = new FileInputStream(iniFile);
      BOMInputStream bOMInputStream = BOMInputStream.builder()
                .setInputStream(in)
                .get();
      //deprecated use
      // BOMInputStream bOMInputStream = new BOMInputStream(in);
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
        LOG.error("Failed to read: {}: {}", iniFile.getAbsolutePath(), e.getMessage(), e);
        throw e;
      } finally {
        in.close();
        reader.close();
      }

      SubnodeConfiguration globalSection = iniConfiguration.getSection("global");
      String vniKey = globalSection.getString("vni..key");
      if (StringUtils.isEmpty(vniKey)) {
        summary.addEntry(ServerMessages.msg("freezy.vni_key"), null, false, ServerMessages.msg("freezy.vni_key_not_set", iniFile.getAbsolutePath()), ServerMessages.msg("freezy.vni_key_hint"));
      }
      else if (!vniKey.equals("f0ad135937ffa111c60b24d88ebb2e59")) {
        summary.addEntry(ServerMessages.msg("freezy.vni_key"), ServerMessages.msg("freezy.vni_key_invalid", vniKey), false, null, ServerMessages.msg("freezy.vni_key_hint"));
      }
      else {
        summary.addEntry(ServerMessages.msg("freezy.vni_key"), ServerMessages.msg("freezy.vni_key_valid", vniKey), true, null, ServerMessages.msg("freezy.vni_key_hint"));
      }

      int pluginIndex = 0;
      String key = "plugin.." + pluginIndex + "..path";
      while (iniConfiguration.containsKey(key)) {
        String path = iniConfiguration.getString(key);
        boolean exists = new File(path).exists();
        summary.addEntry(ServerMessages.msg("freezy.plugin_path", pluginIndex), path, exists, exists ? path : ServerMessages.msg("freezy.dll_missing", path));

        String key64 = "plugin.." + pluginIndex + "..path64";
        if (iniConfiguration.containsKey(key64)) {
          String path64 = iniConfiguration.getString(key64);
          exists = new File(path64).exists();
          summary.addEntry(ServerMessages.msg("freezy.plugin_path64", pluginIndex), path64, exists, exists ? path64 : ServerMessages.msg("freezy.dll_missing", path64));
        }

        String passthroughKey = "plugin.." + pluginIndex + "..passthrough";
        if (iniConfiguration.containsKey(passthroughKey)) {
          String passthrough = iniConfiguration.getString(passthroughKey);
          summary.addEntry(ServerMessages.msg("freezy.plugin_passthrough", pluginIndex), passthrough, true, "");
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
        summary.addEntry("DmdDevice.ini", ServerMessages.msg("freezy.section_missing", iniFile.getAbsolutePath()));
      }
      else {
        String enabled = virtualDmdSection.getString("enabled");
        summary.addEntry(ServerMessages.msg("freezy.virtual_dmd_enabled"), yesNo(enabled));

        enabled = virtualDmdSection.getString("stayontop");
        summary.addEntry(ServerMessages.msg("freezy.stay_on_top"), yesNo(enabled), true, null, ServerMessages.msg("freezy.stay_on_top_hint"));

        enabled = virtualDmdSection.getString("useregistry");
        summary.addEntry(ServerMessages.msg("freezy.use_registry"), yesNo(enabled), true, null, ServerMessages.msg("freezy.use_registry_hint"));
      }

    } catch (Exception e) {
      LOG.error("Failed to load {}: {}", iniFile.getAbsolutePath(), e.getMessage(), e);
      summary.setError(ServerMessages.msg("freezy.error", e.getMessage()));
    }
    return summary;
  }

  private static String yesNo(String value) {
    return "true".equals(value) ? ServerMessages.msg("component.summary.yes") : ServerMessages.msg("component.summary.no");
  }
}
