package de.mephisto.vpin.ui.vps;

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
                  "The resources used for the playfield and other images are 4k (or higher). The term is probably outdated since VPX can handle different resolutions.";
          case "B2S" -> "Backglass";
          case "Color Rom" -> "Colorized Rom, needed to be placed in Visual Pinball/VPinMame/altcolor/<ROM>";
          case "FastFlips" -> "Process reducing lag in flippers.";
          case "Fleep" -> "Consists of various realistic mechanical sounds created by Fleep.";
          case "FLEXDMD" ->
                  "A folder containing DMD animations, mostly used in Original tables. Needs to be places in your Visual Pinball/Tables folder";
          case "FSS" -> "Full Single Screen tables";
          case "Hybrid" -> "Tables can do cabinet, desktop and VR modes.";
          case "Kids" -> "Children Friendly";
          case "LUT" -> "Color Grade Look Up Table. Change table's color values.";
          case "MediaPack" -> "Mostly consisted of images, sounds and / or videos that could be used with frontends.";
          case "MOD" -> "Modification.";
          case "NFOZZY" -> "Nfozzy (and Rothbauerw)'s Physics.";
          case "POV" -> "Point of View";
          case "ROM" ->
                  "Game Rom (Mame). Needs to be placed in placed in your Visual Pinball/VPinMame/<ROM> folder. Note: needs to be .zipped!";
          case "SSF" -> "Surround Sound Feedback";
          case "VR" -> "Virtual Reality, mostly used for VR glasses.";
          case "Sound" ->
                  "ALTernative Sound. Enhanced music, sounds or a completely different music track. To be placed in Visual Pinball/VPinMame/altsound";
          case "Wheel" -> "Wheel art. Can be regular or animated.";
          case "Topper" -> "Can be used to play extra videos when needed. For users that have more than 3 screens.";
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
