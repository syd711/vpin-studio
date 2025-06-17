package de.mephisto.vpin.ui.tables.dialogs;

import java.util.List;

import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.ui.tables.drophandler.TableMediaFileDropEventHandler;
import de.mephisto.vpin.ui.util.FileDragEventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class TableAssetManagerPane extends Pane {

  public static class MediaPane extends BorderPane {
    VPinScreen screen;
    String[] suffixes;
    Label label;
    BorderPane borderPane;
        
    public MediaPane(TableAssetManagerPane rootPane, String text, VPinScreen screen, String... suffix) {
      this.screen = screen;
      this.suffixes = suffix;
      this.getStyleClass().add("media-container");

      label = new Label(text);
      label.setTextFill(Color.WHITE);
      this.setBottom(label);
      BorderPane.setAlignment(label, Pos.CENTER);

      borderPane = new BorderPane();
      this.setTop(borderPane);
      BorderPane.setAlignment(borderPane, Pos.TOP_RIGHT);

      rootPane.getChildren().add(this);
    }
  }

  MediaPane audioLaunch;
  MediaPane fullDmd;
  MediaPane gameInfo;
  MediaPane help;
  MediaPane topper;
  MediaPane backglass;
  MediaPane dmd;
  MediaPane playfield;
  MediaPane audio;
  MediaPane loading;
  MediaPane other2;
  MediaPane wheel;

  MediaPane[] allpanes;

  public TableAssetManagerPane() {
    super();
    audioLaunch = new MediaPane(this, "Audio Launch", VPinScreen.AudioLaunch, "mp3");
    fullDmd = new MediaPane(this, "Apron/Full DMD", VPinScreen.Menu, "mp4", "png", "jpg");
    gameInfo = new MediaPane(this, "Info / Flyer", VPinScreen.GameInfo, "mp4", "png", "jpg");
    help = new MediaPane(this, "Help", VPinScreen.GameHelp, "mp4", "png", "jpg");
    topper = new MediaPane(this, "Topper", VPinScreen.Topper, "mp4", "png", "jpg");
    backglass = new MediaPane(this, "Backglass", VPinScreen.BackGlass, "mp4", "png", "jpg");
    dmd = new MediaPane(this, "DMD", VPinScreen.DMD, "mp4", "png", "jpg");
    playfield = new MediaPane(this, "Playfield", VPinScreen.PlayField, "mp4");
    audio = new MediaPane(this, "Audio", VPinScreen.Audio, "mp3");
    loading = new MediaPane(this, "Loading", VPinScreen.Loading, "mp4");
    other2 = new MediaPane(this, "Other2", VPinScreen.Other2, "mp4", "png", "jpg");
    wheel = new MediaPane(this, "Wheel", VPinScreen.Wheel, "apng", "png", "jpg");

    allpanes = new MediaPane[] {
      audioLaunch, fullDmd, gameInfo, help, topper, backglass, dmd, playfield, audio, loading, other2, wheel
    };
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
    double col13Height = insideHeight - 11 * gutterY;
    double col2Height = insideHeight - 3 * gutterY;

    double w13 = snapSizeX(insideWidth * 31.0 / 100.0);
    double w2  = snapSizeX(insideWidth * 38.0 / 100.0);

    double x1 = snapPositionX(insideX);
    double x2 = snapPositionX(insideX + w13 + gutterX);
    double x3 = snapPositionX(insideX + w13 + gutterX + w2 + gutterX);

    // Start first and third column
    double y = snapPositionY(insideY + 4 * gutterY);
    double h = snapSizeY(col13Height * 20.0 / 100.0);

    audioLaunch.resize(w13, h);
    audioLaunch.relocate(x1, y);

    audio.resize(w13, h);
    audio.relocate(x3, y);

    y = snapPositionY(y + h + gutterY);
    h = snapSizeY(col13Height * 27.0 / 100.0);

    fullDmd.resize(w13, h);
    fullDmd.relocate(x1, y);

    loading.resize(w13, h);
    loading.relocate(x3, y);

    y = snapPositionY(y + h + gutterY);
    h = snapSizeY(col13Height * 26.0 / 100.0);

    gameInfo.resize(w13, h);
    gameInfo.relocate(x1, y);

    other2.resize(w13, h);
    other2.relocate(x3, y);

    y = snapPositionY(y + h + gutterY);

    help.resize(w13, h);
    help.relocate(x1, y);

    wheel.resize(w13, h);
    wheel.relocate(x3, y);

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

  public void setEmbeddedMode() {
    audioLaunch.label.setText("Audio L.");
    fullDmd.label.setText("Full DMD");
  }
}
