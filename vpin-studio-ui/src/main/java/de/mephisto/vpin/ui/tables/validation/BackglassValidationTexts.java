package de.mephisto.vpin.ui.tables.validation;

import de.mephisto.vpin.commons.utils.i18n.Messages;
import de.mephisto.vpin.ui.backglassmanager.DirectB2SModel;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import org.jspecify.annotations.NonNull;

import static de.mephisto.vpin.restclient.validation.BackglassValidationCode.*;

/**
 * See GameValidator
 */
public class BackglassValidationTexts {

  public static LocalizedValidation validate(int code, @NonNull DirectB2SModel model) {
    String label = null;
    String text = null;

    //Frontend frontend = Studio.client.getFrontendService().getFrontendCached();
      //FrontendUtil.replaceName("text with  [Frontend]", frontend);

    switch (code) {
      case CODE_NO_GAME: {
        label = Messages.get("validation.backglass.no_game.label", model.getName());
        text = Messages.get("validation.backglass.no_game.text");
        break;
      }
      case CODE_NOT_RUN_AS_EXE: {
        label = Messages.get("validation.backglass.not_run_as_exe.label", model.getName());
        text = Messages.get("validation.backglass.not_run_as_exe.text");
        break;
      }
      case CODE_NO_FULLDMD: {
        label = Messages.get("validation.backglass.no_fulldmd.label");
        text = Messages.get("validation.backglass.no_fulldmd.text");
        break;
      }
      case CODE_WRONG_FULLDMD_RATIO: {
        label = Messages.get("validation.backglass.wrong_fulldmd_ratio.label");
        //  + ", the resolution is " + model.getDmdWidth() + "x" + model.getDmdHeight();
        text = Messages.get("validation.backglass.wrong_fulldmd_ratio.text");
        break;
      }
      default: {
        throw new UnsupportedOperationException("unmapped validation state");
      }
    }

    return new LocalizedValidation(label, text);
  }
}
