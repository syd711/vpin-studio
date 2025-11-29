package de.mephisto.vpin.commons.fx.pausemenu;

import javafx.animation.*;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

/**
 * Utility class for creating transitions with default values.
 */
public class TransitionUtil {

  public static final int FADER_DEFAULT = 200;

  /**
   * Creates a fade in effect without playing it
   */
  public static FadeTransition createInFader(Node node) {
    long duration = FADER_DEFAULT;
    if (node.getOpacity() == 1) {
      duration = 0;
    }
    return createInFader(node, duration);
  }

  public static FadeTransition createInFader(Node node, long duration) {
    FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
    fadeTransition.setFromValue(0);
    fadeTransition.setToValue(1);
    applyDefaults(node, fadeTransition);
    return fadeTransition;
  }

  public static FadeTransition createInFader(Node node, double opacity, long duration) {
    FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
    fadeTransition.setFromValue(0);
    fadeTransition.setToValue(opacity);
    applyDefaults(node, fadeTransition);
    return fadeTransition;
  }

  public static FadeTransition createOutFader(Node node, long duration) {
    FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
    fadeTransition.setFromValue(1);
    fadeTransition.setToValue(0);
    applyDefaults(node, fadeTransition);
    return fadeTransition;
  }

  public static FadeTransition createOutFader(Node node) {
    long duration = FADER_DEFAULT;
    if (node.getOpacity() == 0) {
      duration = 0;
    }

    FadeTransition fadeTransition = new FadeTransition(Duration.millis(duration), node);
    fadeTransition.setFromValue(1);
    fadeTransition.setToValue(0);
    applyDefaults(node, fadeTransition);
    return fadeTransition;
  }

  /**
   * Creates a blink out effect without playing it
   */
  public static Transition createBlink(Node node) {
    SequentialTransition sequentialTransition = new SequentialTransition(node);
    sequentialTransition.setCycleCount(3);

    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), node);
    fadeOut.setFromValue(0.1);
    fadeOut.setCycleCount(1);
    fadeOut.setToValue(1);

    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), node);
    fadeIn.setFromValue(1);
    fadeIn.setCycleCount(1);
    fadeIn.setToValue(0.1);

    sequentialTransition.getChildren().addAll(fadeIn, fadeOut);

    applyDefaults(node, sequentialTransition);
    return sequentialTransition;
  }

  public static Transition createScaleTransition(Node node, double increase, long duration) {
    ScaleTransition transition = new ScaleTransition(Duration.millis(duration), node);
    transition.setByX(increase);
    transition.setByY(increase);
    transition.setCycleCount(0);
    applyDefaults(node, transition);
    return transition;
  }

  /**
   * Expands the width of the given Pane to the target width.
   *
   * @param node   the pane to expand
   * @param offset the offset width of the pane
   */
  public static Transition createMaxWidthTransition(final Pane node, final int originalWidth, final int offset, final boolean increase) {
    final Transition transition = new Transition() {
      {
        setCycleDuration(Duration.millis(200));
      }

      @Override
      protected void interpolate(double v) {
        double percent = v * 100;
        double newWidthOffset = offset * percent / 100;

        if (increase) {
          node.setMinWidth(originalWidth + newWidthOffset);
        }
        else {
          node.setMinWidth(originalWidth - newWidthOffset);
        }
      }
    };
    transition.setAutoReverse(false);
    transition.setInterpolator(Interpolator.EASE_BOTH);
    applyDefaults(node, transition);
    return transition;
  }

  /**
   * Translate transition used for scrolling
   */
  public static TranslateTransition createTranslateByXTransition(Node node, long duration, int width) {
    TranslateTransition translateTransition = new TranslateTransition(Duration.millis(duration), node);
    translateTransition.setByX(width);
    applyDefaults(node, translateTransition);
    return translateTransition;
  }

  /**
   * Translate transition used for scrolling
   */
  public static TranslateTransition createTranslateByYTransition(Node node, long duration, int height) {
    TranslateTransition translateTransition = new TranslateTransition(Duration.millis(duration), node);
    translateTransition.setByY(height);
    applyDefaults(node, translateTransition);
    return translateTransition;
  }

  /**
   * Applies common settings for transitions and their nodes.
   *
   * @param node       the node the transition is working on
   * @param transition the transition to apply the defaults for
   */
  private static void applyDefaults(Node node, Transition transition) {
    transition.setAutoReverse(false);
    transition.setInterpolator(Interpolator.EASE_BOTH);

    //apply speed as default cache strategy.
//    if (!node.getCacheHint().equals(CacheHint.SCALE_AND_ROTATE)) {
//      node.setCache(true);
//      node.setCacheHint(CacheHint.SCALE_AND_ROTATE);
//    }
  }
}
