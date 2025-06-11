package de.mephisto.vpin.ui.util;

import de.mephisto.vpin.restclient.frontend.Frontend;
import javafx.scene.control.Labeled;
import javafx.scene.control.Tooltip;

import static de.mephisto.vpin.ui.Studio.Features;

import org.apache.commons.lang3.StringUtils;

/**
 * ex: Stop all [Emulator] and [Frontend] processes 
 * for Standalone => Stop all VPX processes
 * for Others     => Stop all VPX and Pinup Popper processes
 *
 * ex: Open [Frontend] setup 
 * for Standalone => Open VPX setup
 * for Others     => Open Pinup Popper setup
 */
public class FrontendUtil {

    public static void replaceName(Labeled node, Frontend frontend) {
        node.setText(replaceName(node.getText(), frontend));
    }
    public static void replaceName(Tooltip tp, Frontend frontend) {
        tp.setText(replaceName(tp.getText(),frontend));
    }
    public static String replaceName(String text, Frontend frontend) {
        if (Features.IS_STANDALONE) {
            text = StringUtils.replace(text, " and [Frontend]", "");
        }
        return StringUtils.replace(text, "[Frontend]", frontend.getName());
    }

    public static void replaceNames(Labeled node, Frontend frontend, String emulator) {
        node.setText(replaceNames(node.getText(), frontend, emulator));
    }
    public static void replaceNames(Tooltip tp, Frontend frontend, String emulator) {
        tp.setText(replaceNames(tp.getText(),frontend, emulator));
    }
    public static String replaceNames(String text, Frontend frontend, String emulator) {
        return StringUtils.replace(replaceName(text, frontend), "[Emulator]", emulator);
    }

}
