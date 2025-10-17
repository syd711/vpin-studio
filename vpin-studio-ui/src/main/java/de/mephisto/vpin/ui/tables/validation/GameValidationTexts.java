package de.mephisto.vpin.ui.tables.validation;

import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.FrontendUtil;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.util.List;

import static de.mephisto.vpin.restclient.validation.GameValidationCode.*;
import static de.mephisto.vpin.ui.Studio.Features;

/**
 * See GameValidator
 */
public class GameValidationTexts {

  private final static String NO_MEDIA_TEXT = "Dismiss this message if the table does not support this media or format.";

  public static LocalizedValidation validate(@NonNull GameRepresentation game) {
    String label = null;
    String text = null;

    ValidationState state = game.getValidationState();
    return getValidationResult(game, state);
  }

  @NonNull
  public static LocalizedValidation getValidationResult(@NonNull GameRepresentation game, ValidationState state) {
    Frontend frontend = Studio.client.getFrontendService().getFrontendCached();

    String text;
    String label;
    int code = state.getCode();
    switch (code) {
      case CODE_VPX_NOT_EXISTS: {
        label = "VPX file \"" + game.getGameFileName() + "\" does not exist.";
        text = FrontendUtil.replaceName("Fix the configuration for this table in [Frontend] or delete it.", frontend);
        break;
      }
      case CODE_NO_ROM: {
        label = "No ROM name could be resolved.";
        text = FrontendUtil.replaceName("Consider setting the ROM name in the \"Script Details\" section or [Frontend]" 
          + ". Otherwise no highscore can be evaluated for this table.", frontend);
        break;
      }
      case CODE_ROM_NOT_EXISTS: {
        label = "ROM file does not exist.";
        text = "Dismiss this message if this table does not require a ROM. Otherwise upload the required ROM in the \"Script Details\" section.";
        break;
      }
      case CODE_ROM_INVALID: {
        label = "ROM file is invalid.";
        text = "The VPinMAME ROM check failed for this ROM. Upload a correct ROM file for this table if you are having issues playing it.";
        break;
      }
      case CODE_SCREEN_SIZE_ISSUE: {
        label = "Invalid screen size configuration";
        text = "FILL THIS text here!";
        break;
      }
      case CODE_VR_DISABLED: {
        label = "The VR room setting is disabled.";
        text = "The table has VR support, but it is disabled. Change the VR room variable inside the script to enabled it.";
        break;
      }
      case CODE_NVOFFSET_MISMATCH: {
        label = "\"NVOffset\" mismatch found.";
        text = "This table has an \"NVOffset\" of \"" + state.getOptions().get(1) + "\" set, but table \"" + state.getOptions().get(0) + "\" has the NVOffset value \"" + state.getOptions().get(2) + "\".";
        break;
      }
      case CODE_NO_DIRECTB2S_OR_PUPPACK: {
        label = "No directb2s file found.";
        if(Features.PUPPACKS_ENABLED) {
          label = "No PUP pack and no directb2s file found.";
        }
        text = "Check the \"Virtual Pinball Spreadsheet\" section to download a \"directb2s\" file for this table.";
        break;
      }
      case CODE_NO_DIRECTB2S_AND_PUPPACK_DISABLED: {
        label = "PUP Pack not enabled.";
        text = "The table does not have a backglass and the PUP pack is not enabled.";
        break;
      }
      case CODE_NO_DMDFOLDER: {
        label = "DMD Folder not found.";
        text = "The table uses UltraDMD or FlexDMD but the needed DMD folder is not found.";
        break;
      }
      case CODE_NO_AUDIO: {
        label = invalidAssetMessage("Audio");
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_AUDIO_LAUNCH: {
        label = invalidAssetMessage("Audio Launch");
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_APRON: {
        label = invalidAssetMessage("Full DMD");
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_INFO: {
        label = invalidAssetMessage("Info");
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_HELP: {
        label = invalidAssetMessage("Help");
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_TOPPER: {
        label = invalidAssetMessage("Topper");
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_BACKGLASS: {
        label = invalidAssetMessage("Backglass");
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_DMD: {
        label = invalidAssetMessage("DMD");
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_LOADING: {
        label = invalidAssetMessage("Loading");
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_PLAYFIELD: {
        label = invalidAssetMessage("Playfield");
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_OTHER2: {
        label = invalidAssetMessage("Other2");
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_LOGO: {
        label = invalidAssetMessage("Logo");
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_NO_WHEEL_IMAGE: {
        label = invalidAssetMessage("Wheel");
        text = NO_MEDIA_TEXT;
        break;
      }
      case CODE_ALT_SOUND_NOT_ENABLED: {
        label = "ALT sound not enabled.";
        text = "The table has an ALT sound package, but \"Sound Mode\" is not checked in the \"VPin MAME\" settings.";
        break;
      }
      case CODE_ALT_SOUND_FILE_MISSING: {
        label = "ALT sound audio file(s) missing.";
        text = "Audio files of this ALT sound package are missing. Open the ALT sound editor for details.";
        break;
      }
      case CODE_FORCE_STEREO: {
        label = "Force Stereo not enabled.";
        text = "Enable \"Force Stereo\" for this table in the VPinMAME settings.";
        break;
      }
      case CODE_PUP_PACK_FILE_MISSING: {
        label = "PUP pack media file missing.";
        if (state.getOptions().size() > 1) {
          label = "PUP pack media files missing.";
        }
        if (state.getOptions().size() > 2) {
          List<String> entries = state.getOptions().subList(0, 2);
          text = "The trigger.pup file references invalid file(s): \"" + String.join("\", \"", entries) + "\" (+" + (state.getOptions().size() - 2) + " more entries)";
        }
        else {
          text = "The trigger.pup file references invalid file(s): \"" + String.join("\", \"", state.getOptions()) + "\"";
        }
        break;
      }
      case CODE_VPS_MAPPING_MISSING: {
        label = "No \"Virtual Pinball Spreadsheet\" match found.";
        text = "The table and its version has not been matched against the Virtual Pinball Spreadsheet.";
        break;
      }
      case CODE_VPS_ALTCOLOR_MISSING: {
        label = "No ALT color resource found.";
        text = "There is an ALT color resource available on the Virtual Pinball Spreadsheet for this table.";
        break;
      }
      case CODE_VPS_ALTSOUND_MISSING: {
        label = "No ALT sound bundle found.";
        text = "There is an ALT sound bundle available on the Virtual Pinball Spreadsheet for this table.";
        break;
      }
      case CODE_VPS_PUPPACK_MISSING: {
        label = "No PUP pack found.";
        text = "There is a PUP pack available on the Virtual Pinball Spreadsheet for this table.";
        break;
      }
      case CODE_ALT_COLOR_FILES_MISSING: {
        label = "ALT Color files missing.";
        text = "An ALT Color file is missing: " + state.getOptions().get(0);
        break;
      }
      case CODE_ALT_COLOR_EXTERNAL_DMD_NOT_ENABLED: {
        label = "External DMD not enabled.";
        text = "The table contains an ALT Color file, but the external DMD is not enabled in the VPin MAME settings.";
        break;
      }
      case CODE_ALT_COLOR_COLORIZE_DMD_ENABLED: {
        label = "Colorize DMD not enabled.";
        text = "The table contains an ALT Color file, but the DMD colorization is not enabled in the VPin MAME settings.";
        break;
      }
      case CODE_ALT_COLOR_DMDDEVICE_FILES_MISSING: {
        label = "DMD device files missing.";
        text = "Mandatory file not found to run ALT Color: " + state.getOptions().get(0);
        break;
      }
      case CODE_SCRIPT_CONTROLLER_STOP_MISSING: {
        label = "No \"Controller.stop\" call found in script.";
        text = "The VPX script has an exit method but does not call \"Controller.stop\". This call is required so that the tables nvram file is written.";
        break;
      }
      default: {
        throw new UnsupportedOperationException("unmapped validation state");
      }
    }


    return new LocalizedValidation(label, text);
  }

  private static String invalidAssetMessage(String name) {
    return "Assets for screen \"" + name + "\" do not match with the configured screen validator configuration.";
  }
}
