package de.mephisto.vpin.restclient.validation;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface GameValidationCode {
  int CODE_NO_ROM = 1;
  int CODE_ROM_NOT_EXISTS = 3;
  int CODE_VPX_NOT_EXISTS = 4;
  int CODE_NVOFFSET_MISMATCH = 5;
  int CODE_ROM_INVALID = 6;
  int CODE_VR_DISABLED = 7;
  int CODE_DUPLICATED_ROM = 8;

  int CODE_SCREEN_SIZE_ISSUE = 10;

  int CODE_NO_DIRECTB2S_OR_PUPPACK = 20;
  int CODE_NO_DIRECTB2S_AND_PUPPACK_DISABLED = 21;
  int CODE_NO_DMDFOLDER = 22;
  int CODE_BACKGLASS_AND_BACKGLASSES_DISABLED = 23;

  int CODE_NO_AUDIO = 30;
  int CODE_NO_AUDIO_LAUNCH = 31;
  int CODE_NO_APRON = 32;
  int CODE_NO_INFO = 33;
  int CODE_NO_HELP = 34;
  int CODE_NO_TOPPER = 35;
  int CODE_NO_BACKGLASS = 36;
  int CODE_NO_DMD = 37;
  int CODE_NO_PLAYFIELD = 38;
  int CODE_NO_LOADING = 39;
  int CODE_NO_OTHER2 = 40;
  int CODE_NO_WHEEL_IMAGE = 41;
  int CODE_NO_LOGO = 42;

  int CODE_ALT_SOUND_NOT_ENABLED = 50;
  int CODE_ALT_SOUND_FILE_MISSING = 51;
  int CODE_FORCE_STEREO = 52;

  int CODE_PUP_PACK_FILE_MISSING = 60;
  int CODE_MUSIC_FILE_MISSING = 61;

  int CODE_VPS_MAPPING_MISSING = 70;
  int CODE_VPS_ALTCOLOR_MISSING = 71;
  int CODE_VPS_ALTSOUND_MISSING = 72;
  int CODE_VPS_PUPPACK_MISSING = 73;

  int CODE_ALT_COLOR_COLORIZE_DMD_ENABLED = 80;
  int CODE_ALT_COLOR_EXTERNAL_DMD_NOT_ENABLED = 81;
  int CODE_ALT_COLOR_FILES_MISSING = 82;
  int CODE_ALT_COLOR_DMDDEVICE_FILES_MISSING = 84;

  int CODE_SCRIPT_CONTROLLER_STOP_MISSING = 90;
  int CODE_SCRIPT_FILES_MISSING = 91;

  static String name(int code) {
      return switch (code) {
          case CODE_NO_ROM -> "No ROM name resolved";
          case CODE_ROM_NOT_EXISTS -> "ROM file does not exist";
          case CODE_VPX_NOT_EXISTS -> "VPX file does not exist";
          case CODE_NVOFFSET_MISMATCH -> "NVOffset mismatch";
          case CODE_ROM_INVALID -> "ROM file is invalid";
          case CODE_VR_DISABLED -> "VR room setting found, but is disabled";
          case CODE_SCREEN_SIZE_ISSUE -> "Invalid screen size configuration";
          case CODE_NO_DIRECTB2S_OR_PUPPACK -> "No directb2s or PUP pack found";
          case CODE_NO_DIRECTB2S_AND_PUPPACK_DISABLED -> "PUP pack not enabled";
          case CODE_NO_DMDFOLDER -> "DMD folder not found";
          case CODE_BACKGLASS_AND_BACKGLASSES_DISABLED -> "Backglasses disabled";
          case CODE_NO_AUDIO -> "Audio asset missing";
          case CODE_NO_AUDIO_LAUNCH -> "Audio Launch asset missing";
          case CODE_NO_APRON -> "Full DMD asset missing";
          case CODE_NO_INFO -> "Info asset missing";
          case CODE_NO_HELP -> "Help asset missing";
          case CODE_NO_TOPPER -> "Topper asset missing";
          case CODE_NO_BACKGLASS -> "Backglass asset missing";
          case CODE_NO_DMD -> "DMD asset missing";
          case CODE_NO_PLAYFIELD -> "Playfield asset missing";
          case CODE_NO_LOADING -> "Loading asset missing";
          case CODE_NO_OTHER2 -> "Other2 asset missing";
          case CODE_NO_WHEEL_IMAGE -> "Wheel image missing";
          case CODE_NO_LOGO -> "Logo asset missing";
          case CODE_ALT_SOUND_NOT_ENABLED -> "ALT sound not enabled";
          case CODE_ALT_SOUND_FILE_MISSING -> "ALT sound files are missing in the installation";
          case CODE_FORCE_STEREO -> "Force Stereo not enabled";
          case CODE_PUP_PACK_FILE_MISSING -> "PUP pack media file missing";
          case CODE_MUSIC_FILE_MISSING -> "Music file missing";
          case CODE_VPS_MAPPING_MISSING -> "VP-Spreadsheet: Table mapping is missing";
          case CODE_VPS_ALTCOLOR_MISSING -> "VP-Spreadsheet: ALT color files available";
          case CODE_VPS_ALTSOUND_MISSING -> "VP-Spreadsheet: ALT sound bundles available";
          case CODE_VPS_PUPPACK_MISSING -> "VP-Spreadsheet: PUP pack available";
          case CODE_ALT_COLOR_COLORIZE_DMD_ENABLED -> "Colorize DMD not enabled";
          case CODE_ALT_COLOR_EXTERNAL_DMD_NOT_ENABLED -> "External DMD not enabled";
          case CODE_ALT_COLOR_FILES_MISSING -> "ALT color files are missing in the installation";
          case CODE_ALT_COLOR_DMDDEVICE_FILES_MISSING -> "DMD device files missing";
          case CODE_SCRIPT_CONTROLLER_STOP_MISSING -> "Controller.stop call missing";
          case CODE_SCRIPT_FILES_MISSING -> "Included script missing";
          default -> "Unknown validation code (" + code + ")";
      };
  }

  static List<Integer> values() {
    return Arrays.stream(GameValidationCode.class.getFields())
        .filter(f -> f.getType() == int.class)
        .map(f -> { try { return f.getInt(null); } catch (Exception e) { return -1; } })
        .filter(v -> v >= 0)
        .collect(Collectors.toList());
  }
}
