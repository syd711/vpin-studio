package de.mephisto.vpin.ui.mania.util;

import de.mephisto.vpin.commons.fx.ServerFX;
import de.mephisto.vpin.ui.Studio;
import javafx.scene.image.Image;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

public class ManiaAvatarCache {
  private final static Map<String, Image> avatarCache = new LinkedHashMap<>();

  public static void clear() {
    avatarCache.clear();
  }

  public static Image getAvatarImage(String accountUUID) {
    Image avatarImage = null;
    if (avatarCache.containsKey(accountUUID)) {
      return avatarCache.get(accountUUID);
    }

    InputStream in = Studio.client.getCachedUrlImage(Studio.maniaClient.getAccountClient().getAvatarUrl(accountUUID));
    if (in != null) {
      avatarCache.put(accountUUID, avatarImage);
      avatarImage = new Image(in);
    }
    else {
      avatarImage = new Image(ServerFX.class.getResourceAsStream("avatar-blank.png"));
    }
    avatarCache.put(accountUUID, avatarImage);
    return avatarImage;
  }
}
