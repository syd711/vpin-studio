package de.mephisto.vpin.ui.vps;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import java.util.List;

import de.mephisto.vpin.connectors.vps.model.VpsAuthoredUrls;

public class VpsUtil {

  public static boolean isDataAvailable(List<? extends VpsAuthoredUrls> entries) {
    if (entries == null) {
      return false;
    }
    for (VpsAuthoredUrls entry : entries) {
      if (entry.getUrls() != null && !entry.getUrls().isEmpty()) {
        return true;
      }
    }
    return false;
  }

  public static String abbreviate(String url) {
    if (url.isEmpty()) {
      return "";
    }

    if (url.contains("vpuniverse.com")) {
      return "VPU";
    }
    if (url.contains("mega.")) {
      return "Mega";
    }

    if (url.contains("vpforums.")) {
      return "VPF";
    }

    if (url.contains("dropbox.")) {
      return "Dropbox";
    }

    if (url.contains("youtube.")) {
      return "YT";
    }

    return "Ext";
  }

  public static String getFeatureColorTooltip(String abb) {
      return switch (abb) {
          case "4K" ->
                  Messages.get("dialog.vps_feature_4k");
          case "B2S" -> Messages.get("dialog.vps_feature_b2s");
          case "Color Rom" -> Messages.get("dialog.vps_feature_color_rom");
          case "FastFlips" -> Messages.get("dialog.vps_feature_fastflips");
          case "Fleep" -> Messages.get("dialog.vps_feature_fleep");
          case "FLEXDMD" ->
                  Messages.get("dialog.vps_feature_flexdmd");
          case "FSS" -> Messages.get("dialog.vps_feature_fss");
          case "Hybrid" -> Messages.get("dialog.vps_feature_hybrid");
          case "Kids" -> Messages.get("dialog.vps_feature_kids");
          case "LUT" -> Messages.get("dialog.vps_feature_lut");
          case "MediaPack" -> Messages.get("dialog.vps_feature_mediapack");
          case "MOD" -> Messages.get("dialog.vps_feature_mod");
          case "NFOZZY" -> Messages.get("dialog.vps_feature_nfozzy");
          case "POV" -> Messages.get("dialog.vps_feature_pov");
          case "ROM" ->
                  Messages.get("dialog.vps_feature_rom");
          case "SSF" -> Messages.get("dialog.vps_feature_ssf");
          case "VR" -> Messages.get("dialog.vps_feature_vr");
          case "Sound" ->
                  Messages.get("dialog.vps_feature_sound");
          case "Wheel" -> Messages.get("dialog.vps_feature_wheel");
          case "Topper" -> Messages.get("dialog.vps_feature_topper");
          default -> abb;
      };
  }


  public static String getFeatureColor(String abb) {
    return "#338033";
  }
  public static String getFeatureColor(String abb, boolean selected) {
    return selected ? getFeatureColor(abb) : "#151515";
  }

  public static String getIconClass(String abb) {
      return switch (abb) {
          case "VPU" -> "mdi2o-open-in-new";
          case "Dropbox" -> "mdi2d-dropbox";
          case "VPF" -> "mdi2o-open-in-new";
          case "Mega" -> "mdi2o-open-in-new";
          case "YT" -> "mdi2y-youtube";
          default -> "mdi2o-open-in-new";
      };

  }

  public static String getColor(String abb) {
      return switch (abb) {
          case "VPU" -> "#3182ce";
          case "Dropbox" -> "#3182ce";
          case "VPF" -> "#dd6b20";
          case "Mega" -> "#e53e3e";
          case "FP" -> "#718096";
          case "VPX" -> "#718096";
          case "YT" -> "#ff4e45";
          default -> "#718096";
      };

  }
}
