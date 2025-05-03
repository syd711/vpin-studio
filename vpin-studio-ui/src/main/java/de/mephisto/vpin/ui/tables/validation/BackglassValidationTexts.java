package de.mephisto.vpin.ui.tables.validation;

import de.mephisto.vpin.ui.backglassmanager.DirectB2SModel;
import de.mephisto.vpin.ui.util.LocalizedValidation;
import edu.umd.cs.findbugs.annotations.NonNull;

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
        label = "The backglass file \"" + model.getName() + "\" has no matching game file.";
        text = "Install a game in the tables tab or delete this backglass.";
        break;
      }
      case CODE_NOT_RUN_AS_EXE: {
        label = "The backglass file \"" + model.getName() + "\" is not set to run as Exe.";
        text = "Either set backglass to Run as Exe or use Standard and turn on in backglass preferences.";
        break;
      }
      case CODE_NO_FULLDMD: {
        label = "The backglass file does not contain a full DMD image.";
        text = "Upload a full DMD image on this backglass.";
        break;
      }
      case CODE_WRONG_FULLDMD_RATIO: {
        label = "The backglass file contains a full DMD image but not with a Full-DMD Aspect Ratio";
        //  + ", the resolution is " + model.getDmdWidth() + "x" + model.getDmdHeight();
        text = "Upload another full DMD image on this backglass.";
        break;
      }
      default: {
        throw new UnsupportedOperationException("unmapped validation state");
      }
    }

    return new LocalizedValidation(label, text);
  }
}
