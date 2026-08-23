package de.mephisto.vpin.ui.tables.validation;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.GameRepresentation;
import de.mephisto.vpin.restclient.validation.ValidationState;
import de.mephisto.vpin.ui.Studio;
import de.mephisto.vpin.ui.util.FrontendUtil;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import org.jspecify.annotations.NonNull;

import java.util.List;

import static de.mephisto.vpin.restclient.validation.GameValidationCode.*;
import static de.mephisto.vpin.ui.Studio.Features;

/**
 * See GameValidator
 */
public class GameValidationTexts {

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
        label = Messages.get("validation.game.vpx_not_exists.label", game.getGameFileName());
        text = FrontendUtil.replaceName(Messages.get("validation.game.vpx_not_exists.text"), frontend);
        break;
      }
      case CODE_NO_ROM: {
        label = Messages.get("validation.game.no_rom.label");
        text = FrontendUtil.replaceName(Messages.get("validation.game.no_rom.text"), frontend);
        break;
      }
      case CODE_ROM_NOT_EXISTS: {
        label = Messages.get("validation.game.rom_not_exists.label");
        text = Messages.get("validation.game.rom_not_exists.text");
        break;
      }
      case CODE_ROM_INVALID: {
        label = Messages.get("validation.game.rom_invalid.label");
        text = Messages.get("validation.game.rom_invalid.text");
        break;
      }
      case CODE_SCREEN_SIZE_ISSUE: {
        label = Messages.get("validation.game.screen_size_issue.label");
        text = Messages.get("validation.game.screen_size_issue.text");
        break;
      }
      case CODE_VR_DISABLED: {
        label = Messages.get("validation.game.vr_disabled.label");
        text = Messages.get("validation.game.vr_disabled.text");
        break;
      }
      case CODE_NVOFFSET_MISMATCH: {
        label = Messages.get("validation.game.nvoffset_mismatch.label");
        text = Messages.get("validation.game.nvoffset_mismatch.text",
            state.getOptions().get(1), state.getOptions().getFirst(), state.getOptions().get(2));
        break;
      }
      case CODE_NO_DIRECTB2S_OR_PUPPACK: {
        label = Messages.get("validation.game.no_directb2s_or_puppack.label");
        if (Features.PUPPACKS_ENABLED) {
          label = Messages.get("validation.game.no_directb2s_or_puppack.label_puppacks");
        }
        text = Messages.get("validation.game.no_directb2s_or_puppack.text");
        break;
      }
      case CODE_BACKGLASS_AND_BACKGLASSES_DISABLED: {
        label = Messages.get("validation.game.backglass_and_backglasses_disabled.label");
        text = Messages.get("validation.game.backglass_and_backglasses_disabled.text");
        break;
      }
      case CODE_NO_DIRECTB2S_AND_PUPPACK_DISABLED: {
        label = Messages.get("validation.game.no_directb2s_and_puppack_disabled.label");
        text = Messages.get("validation.game.no_directb2s_and_puppack_disabled.text");
        break;
      }
      case CODE_NO_DMDFOLDER: {
        label = Messages.get("validation.game.no_dmdfolder.label");
        text = Messages.get("validation.game.no_dmdfolder.text", state.getOption());
        break;
      }
      case CODE_NO_AUDIO: {
        label = invalidAssetMessage("Audio");
        text = Messages.get("validation.game.no_media_text");
        break;
      }
      case CODE_NO_AUDIO_LAUNCH: {
        label = invalidAssetMessage("Audio Launch");
        text = Messages.get("validation.game.no_media_text");
        break;
      }
      case CODE_NO_APRON: {
        label = invalidAssetMessage("Full DMD");
        text = Messages.get("validation.game.no_media_text");
        break;
      }
      case CODE_NO_INFO: {
        label = invalidAssetMessage("Info");
        text = Messages.get("validation.game.no_media_text");
        break;
      }
      case CODE_NO_HELP: {
        label = invalidAssetMessage("Help");
        text = Messages.get("validation.game.no_media_text");
        break;
      }
      case CODE_NO_TOPPER: {
        label = invalidAssetMessage("Topper");
        text = Messages.get("validation.game.no_media_text");
        break;
      }
      case CODE_NO_BACKGLASS: {
        label = invalidAssetMessage("Backglass");
        text = Messages.get("validation.game.no_media_text");
        break;
      }
      case CODE_NO_DMD: {
        label = invalidAssetMessage("DMD");
        text = Messages.get("validation.game.no_media_text");
        break;
      }
      case CODE_NO_LOADING: {
        label = invalidAssetMessage("Loading");
        text = Messages.get("validation.game.no_media_text");
        break;
      }
      case CODE_NO_PLAYFIELD: {
        label = invalidAssetMessage("Playfield");
        text = Messages.get("validation.game.no_media_text");
        break;
      }
      case CODE_NO_OTHER2: {
        label = invalidAssetMessage("Other2");
        text = Messages.get("validation.game.no_media_text");
        break;
      }
      case CODE_NO_LOGO: {
        label = invalidAssetMessage("Logo");
        text = Messages.get("validation.game.no_media_text");
        break;
      }
      case CODE_NO_WHEEL_IMAGE: {
        label = invalidAssetMessage("Wheel");
        text = Messages.get("validation.game.no_media_text");
        break;
      }
      case CODE_ALT_SOUND_NOT_ENABLED: {
        label = Messages.get("validation.game.alt_sound_not_enabled.label");
        text = Messages.get("validation.game.alt_sound_not_enabled.text");
        break;
      }
      case CODE_ALT_SOUND_FILE_MISSING: {
        label = Messages.get("validation.game.alt_sound_file_missing.label");
        text = Messages.get("validation.game.alt_sound_file_missing.text");
        break;
      }
      case CODE_FORCE_STEREO: {
        label = Messages.get("validation.game.force_stereo.label");
        text = Messages.get("validation.game.force_stereo.text");
        break;
      }
      case CODE_PUP_PACK_FILE_MISSING: {
        label = Messages.get("validation.game.pup_pack_file_missing.label_single");
        if (state.getOptions().size() > 1) {
          label = Messages.get("validation.game.pup_pack_file_missing.label_plural");
        }
        if (state.getOptions().size() > 2) {
          List<String> entries = state.getOptions().subList(0, 2);
          text = Messages.get("validation.game.pup_pack_file_missing.text_more",
              String.join("\", \"", entries), state.getOptions().size() - 2);
        }
        else {
          text = Messages.get("validation.game.pup_pack_file_missing.text", String.join("\", \"", state.getOptions()));
        }
        break;
      }
      case CODE_MUSIC_FILE_MISSING: {
        label = Messages.get("validation.game.music_file_missing.label_single");
        if (state.getOptions().size() > 1) {
          label = Messages.get("validation.game.music_file_missing.label_plural");
        }
        if (state.getOptions().size() > 2) {
          List<String> entries = state.getOptions().subList(0, 2);
          text = Messages.get("validation.game.music_file_missing.text_more",
              String.join("\", \"", entries), state.getOptions().size() - 2);
        }
        else {
          text = Messages.get("validation.game.music_file_missing.text", String.join("\", \"", state.getOptions()));
        }
        break;
      }
      case CODE_VPS_MAPPING_MISSING: {
        label = Messages.get("validation.game.vps_mapping_missing.label");
        text = Messages.get("validation.game.vps_mapping_missing.text");
        break;
      }
      case CODE_VPS_ALTCOLOR_MISSING: {
        label = Messages.get("validation.game.vps_altcolor_missing.label");
        text = Messages.get("validation.game.vps_altcolor_missing.text");
        break;
      }
      case CODE_VPS_ALTSOUND_MISSING: {
        label = Messages.get("validation.game.vps_altsound_missing.label");
        text = Messages.get("validation.game.vps_altsound_missing.text");
        break;
      }
      case CODE_VPS_PUPPACK_MISSING: {
        label = Messages.get("validation.game.vps_puppack_missing.label");
        text = Messages.get("validation.game.vps_puppack_missing.text");
        break;
      }
      case CODE_ALT_COLOR_FILES_MISSING: {
        label = Messages.get("validation.game.alt_color_files_missing.label");
        text = Messages.get("validation.game.alt_color_files_missing.text", state.getOption());
        break;
      }
      case CODE_ALT_COLOR_EXTERNAL_DMD_NOT_ENABLED: {
        label = Messages.get("validation.game.alt_color_external_dmd_not_enabled.label");
        text = Messages.get("validation.game.alt_color_external_dmd_not_enabled.text");
        break;
      }
      case CODE_ALT_COLOR_COLORIZE_DMD_ENABLED: {
        label = Messages.get("validation.game.alt_color_colorize_dmd_enabled.label");
        text = Messages.get("validation.game.alt_color_colorize_dmd_enabled.text");
        break;
      }
      case CODE_ALT_COLOR_DMDDEVICE_FILES_MISSING: {
        label = Messages.get("validation.game.alt_color_dmddevice_files_missing.label");
        text = Messages.get("validation.game.alt_color_dmddevice_files_missing.text", state.getOption());
        break;
      }
      case CODE_SCRIPT_CONTROLLER_STOP_MISSING: {
        label = Messages.get("validation.game.script_controller_stop_missing.label");
        text = Messages.get("validation.game.script_controller_stop_missing.text");
        break;
      }
      case CODE_SCRIPT_FILES_MISSING: {
        label = Messages.get("validation.game.script_files_missing.label");
        text = Messages.get("validation.game.script_files_missing.text", state.getOption());
        break;
      }
      default: {
        throw new UnsupportedOperationException("unmapped validation state");
      }
    }

    return new LocalizedValidation(label, text);
  }

  private static String invalidAssetMessage(String name) {
    return Messages.get("validation.game.invalid_asset.label", name);
  }
}
