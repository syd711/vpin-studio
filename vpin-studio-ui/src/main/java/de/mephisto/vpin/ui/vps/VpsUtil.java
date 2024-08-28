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
    if (url.length() == 0) {
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
    switch (abb) {
      case "4K": {
        return "The resources used for the playfield and other images are 4k (or higher). The term is probably outdated since VPX can handle different resolutions.";
      }
      case "B2S": {
        return "Backglass";
      }
      case "Color Rom": {
        return "Colorized Rom, needed to be placed in Visual Pinball/VPinMame/altcolor/<ROM>";
      }
      case "FastFlips": {
        return "Process reducing lag in flippers.";
      }
      case "Fleep": {
        return "Consists of various realistic mechanical sounds created by Fleep.";
      }
      case "FLEXDMD": {
        return "A folder containing DMD animations, mostly used in Original tables. Needs to be places in your Visual Pinball/Tables folder";
      }
      case "FSS": {
        return "Full Single Screen tables";
      }
      case "Hybrid": {
        return "Tables can do cabinet, desktop and VR modes.";
      }
      case "Kids": {
        return "Children Friendly";
      }
      case "LUT": {
        return "Color Grade Look Up Table. Change table's color values.";
      }
      case "MediaPack": {
        return "Mostly consisted of images, sounds and / or videos that could be used with frontends.";
      }
      case "MOD": {
        return "Modification.";
      }
      case "NFOZZY": {
        return "Nfozzy (and Rothbauerw)'s Physics.";
      }
      case "POV": {
        return "Point of View";
      }
      case "ROM": {
        return "Game Rom (Mame). Needs to be placed in placed in your Visual Pinball/VPinMame/<ROM> folder. Note: needs to be .zipped!";
      }
      case "SSF": {
        return "Surround Sound Feedback";
      }
      case "VR": {
        return "Virtual Reality, mostly used for VR glasses.";
      }
      case "Sound": {
        return "ALTernative Sound. Enhanced music, sounds or a completely different music track. To be placed in Visual Pinball/VPinMame/altsound";
      }
      case "Wheel": {
        return "Wheel art. Can be regular or animated.";
      }
      case "Topper": {
        return "Can be used to play extra videos when needed. For users that have more than 3 screens.";
      }
      default: {
        return abb;
      }
    }
  }


  public static String getFeatureColor(String abb) {
    return "#338033";
  }
  public static String getFeatureColor(String abb, boolean selected) {
    return selected ? getFeatureColor(abb) : "#151515";
  }

  public static String getIconClass(String abb) {
    if (abb.equals("VPU")) {
      return "mdi2o-open-in-new";
    }

    if (abb.equals("Dropbox")) {
      return "mdi2d-dropbox";
    }

    if (abb.equals("VPF")) {
      return "mdi2o-open-in-new";
    }

    if (abb.equals("Mega")) {
      return "mdi2o-open-in-new";
    }

    if (abb.equals("YT")) {
      return "mdi2y-youtube";
    }

    return "mdi2o-open-in-new";
  }

  public static String getColor(String abb) {
    if (abb.equals("VPU")) {
      return "#3182ce";
    }

    if (abb.equals("Dropbox")) {
      return "#3182ce";
    }

    if (abb.equals("VPF")) {
      return "#dd6b20";
    }

    if (abb.equals("Mega")) {
      return "#e53e3e";
    }

    if (abb.equals("FP")) {
      return "#718096";
    }

    if (abb.equals("VPX")) {
      return "#718096";
    }

    if (abb.equals("YT")) {
      return "#ff4e45";
    }

    return "#718096";
  }
}
