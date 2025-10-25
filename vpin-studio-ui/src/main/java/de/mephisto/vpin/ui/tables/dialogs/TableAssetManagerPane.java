package de.mephisto.vpin.ui.tables.dialogs;

import java.util.Arrays;
import java.util.List;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.ui.tables.drophandler.TableMediaFileDropEventHandler;
import de.mephisto.vpin.ui.util.FileDragEventHandler;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class TableAssetManagerPane<P extends TableAssetManagerPane.MediaPane> extends Pane {

  @FunctionalInterface
  public interface MediaPaneItemFactory<T> {
    T createPane(TableAssetManagerPane<?> rootPane, String text, VPinScreen screen, String... suffixes);
  }

  public static class MediaPane extends BorderPane {
    protected VPinScreen screen;
    protected String[] suffixes;
        
    public MediaPane(TableAssetManagerPane<?> rootPane, String text, VPinScreen screen, String... suffix) {
      this.screen = screen;
      this.suffixes = suffix;
      rootPane.getChildren().add(this);
    }

    public VPinScreen getScreen() {
      return screen;
    }

    public String[] getSuffixes() {
      return suffixes;
    }
  }


  P audioLaunch;
  P fullDmd;
  P gameInfo;
  P help;
  P topper;
  P backglass;
  P dmd;
  P playfield;
  P audio;
  P loading;
  P other2;
  P wheel;
  P logo;

  List<P> allpanes;

  public TableAssetManagerPane() {
    super();
  }

  public void createPanes(MediaPaneItemFactory<P> factory, boolean embeddedMode) {
    audioLaunch = factory.createPane(this, embeddedMode ? "Audio L." : "Audio Launch", VPinScreen.AudioLaunch, "mp3");
    fullDmd = factory.createPane(this, embeddedMode ? "Full DMD" : "Apron/Full DMD", VPinScreen.Menu, "mp4", "png", "jpg");
    gameInfo = factory.createPane(this, "Info / Flyer", VPinScreen.GameInfo, "mp4", "png", "jpg");
    help = factory.createPane(this, "Help", VPinScreen.GameHelp, "mp4", "png", "jpg");
    topper = factory.createPane(this, "Topper", VPinScreen.Topper, "mp4", "png", "jpg");
    backglass = factory.createPane(this, "Backglass", VPinScreen.BackGlass, "mp4", "png", "jpg");
    dmd = factory.createPane(this, "DMD", VPinScreen.DMD, "mp4", "png", "jpg");
    playfield = factory.createPane(this, "Playfield", VPinScreen.PlayField, "mp4", "png", "jpg");
    audio = factory.createPane(this, "Audio", VPinScreen.Audio, "mp3");
    loading = factory.createPane(this, "Loading", VPinScreen.Loading, "mp4");
    other2 = factory.createPane(this, "Other2", VPinScreen.Other2, "mp4", "png", "jpg");
    wheel = factory.createPane(this, "Wheel", VPinScreen.Wheel, "apng", "png", "jpg");
    logo = factory.createPane(this, "Logo", VPinScreen.Logo, "png", "jpg");

    allpanes = Arrays.asList(
      audioLaunch, fullDmd, gameInfo, help, topper, backglass, dmd, playfield, audio, loading, other2, wheel, logo
    );
  }

  public List<P> getMediaPanes() {
    return allpanes;
  }

  @Override protected double computeMinWidth(double height) {
    final Insets insets = getInsets();
    double width = 100;
    return insets.getLeft() + width + insets.getRight();
  }

  @Override protected double computeMinHeight(double width) {
    final Insets insets = getInsets();
    double height = 150;
    return insets.getTop() + height + insets.getBottom();
  }

  @Override protected double computePrefWidth(double height) {
    final Insets insets = getInsets();
    double prefWidth = 520;
    return insets.getLeft() + prefWidth + insets.getRight();
  }

  @Override protected double computePrefHeight(double width) {
    final Insets insets = getInsets();
    double prefHeight = 800;
    return insets.getTop() + prefHeight + insets.getBottom();
  }

  @Override protected void layoutChildren() {
    final Insets insets = getInsets();
    double width = getWidth();
    double height = getHeight();

    final double minWidth = minWidth(-1);
    final double minHeight = minHeight(-1);
    width = width < minWidth ? minWidth : width;
    height = height < minHeight ? minHeight : height;

    final double insideX = insets.getLeft();
    final double insideY = insets.getTop();
    double insideWidth = width - insideX - insets.getRight() - 1;
    double insideHeight = height - insideY - insets.getBottom() - 1;

    int gutterX = 7;
    int gutterY = 9;

    insideWidth -= 2 * gutterX;
    double col13Height = insideHeight - 8 * gutterY;
    double col2Height = insideHeight - 3 * gutterY;

    double w13 = snapSizeX(insideWidth * 31.0 / 100.0);
    double w2  = snapSizeX(insideWidth * 38.0 / 100.0);

    double x1 = snapPositionX(insideX);
    double x2 = snapPositionX(insideX + w13 + gutterX);
    double x3 = snapPositionX(insideX + w13 + gutterX + w2 + gutterX);

    //------------------------------
    // First column
    double y = snapPositionY(insideY + 2 * gutterY);
    double h = snapSizeY(col13Height * 16.0 / 100.0);

    audioLaunch.resize(w13, h);
    audioLaunch.relocate(x1, y);

    y = snapPositionY(y + h + gutterY);
    h = snapSizeY(col13Height * 24.0 / 100.0);

    fullDmd.resize(w13, h);
    fullDmd.relocate(x1, y);

    y = snapPositionY(y + h + gutterY);

    gameInfo.resize(w13, h);
    gameInfo.relocate(x1, y);

    y = snapPositionY(y + h + gutterY);

    help.resize(w13, h);
    help.relocate(x1, y);

    y = snapPositionY(y + h + gutterY);
    h = snapSizeY(col13Height * 12.0 / 100.0 - gutterY);

    logo.resize(w13, h);
    logo.relocate(x1, y);

    //------------------------------
    // Second column
    y = snapPositionY(insideY);
    h = snapSizeY(col2Height * 20.5 / 100.0);

    topper.resize(w2, h);
    topper.relocate(x2, y);

    y = snapPositionY(y + h + gutterY);

    backglass.resize(w2, h);
    backglass.relocate(x2, y);

    y = snapPositionY(y + h + gutterY);
    h = snapSizeY(col2Height * 16.0 / 100.0);

    dmd.resize(w2, h);
    dmd.relocate(x2, y);

    y = snapPositionY(y + h + gutterY);
    h = snapSizeY(col2Height * 43.0 / 100.0);

    playfield.resize(w2, h);
    playfield.relocate(x2, y);

    //------------------------------
    // Third column
    y = snapPositionY(insideY + 2 * gutterY);
    h = snapSizeY(col13Height * 16.0 / 100.0);

    audio.resize(w13, h);
    audio.relocate(x3, y);

    y = snapPositionY(y + h + gutterY);
    h = snapSizeY(col13Height * 36.0 / 100.0);

    loading.resize(w13, h);
    loading.relocate(x3, y);

    y = snapPositionY(y + h + gutterY);
    h = snapSizeY(col13Height * 24.0 / 100.0);

    other2.resize(w13, h);
    other2.relocate(x3, y);

    y = snapPositionY(y + h + gutterY);

    wheel.resize(w13, h);
    wheel.relocate(x3, y);
  }

  public void addListeners(TableAssetManagerDialogController controller) {
    for (MediaPane pane : allpanes) {
      pane.hoverProperty().addListener((observableValue, aBoolean, t1) -> controller.updateState(pane.screen, pane, t1, false));
      pane.setOnMouseClicked(mouseEvent -> controller.updateState(pane.screen, pane, true, true));
    }
  }

  public void setPanesVisibility(List<VPinScreen> supportedScreens) {
    for (MediaPane pane : allpanes) {
      pane.setVisible(supportedScreens.contains(pane.screen));
    }
  }

  public void installFileDragEventHandlers(TableAssetManagerDialogController controller, boolean embeddedMode) {
    for (MediaPane pane : allpanes) {
      FileDragEventHandler.install(this, pane, false, pane.suffixes)
        .setOnDragDropped(new TableMediaFileDropEventHandler(controller, pane.screen, pane.suffixes))
        .setEmbeddedMode(embeddedMode);
    }
  }

  public void refreshPanes(TableAssetManagerDialogController controller, VPinScreen screen) {
    for (MediaPane pane : allpanes) {
      controller.updateState(pane.screen, pane, false, screen.equals(pane.screen));
    }
  }
}
