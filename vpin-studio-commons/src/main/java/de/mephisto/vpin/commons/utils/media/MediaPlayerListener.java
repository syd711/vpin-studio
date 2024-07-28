package de.mephisto.vpin.commons.utils.media;

import javafx.scene.media.Media;

public interface MediaPlayerListener {

  void onReady(Media media);

  void onDispose();
}
