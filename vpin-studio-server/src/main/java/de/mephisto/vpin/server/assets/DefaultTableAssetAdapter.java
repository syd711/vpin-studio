package de.mephisto.vpin.server.assets;

import de.mephisto.vpin.connectors.assets.TableAsset;
import de.mephisto.vpin.connectors.assets.TableAssetSource;
import de.mephisto.vpin.connectors.assets.TableAssetsAdapter;
import de.mephisto.vpin.restclient.frontend.VPinScreen;
import de.mephisto.vpin.server.games.Game;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

abstract public class DefaultTableAssetAdapter implements TableAssetsAdapter<Game> {

  @NonNull
  protected final TableAssetSource source;

  public DefaultTableAssetAdapter(@NonNull TableAssetSource source) {
    this.source = source;
  }

  protected boolean matches(String screenSegment, String path) {
    path = path.toLowerCase();
    screenSegment = screenSegment.toLowerCase();

    if (path.contains(screenSegment)) {
      return true;
    }

    if (screenSegment.equals("menu")) {
      if (path.contains("fulldmd") || path.contains("apron")) {
        return true;
      }
    }

    if (screenSegment.equals("gameinfo")) {
      if (path.contains("flyer")) {
        return true;
      }
    }

    if (screenSegment.equals("gamehelp")) {
      if (path.contains("help")) {
        return true;
      }
    }

    return false;
  }

  protected void writeUrlAsset(@NonNull OutputStream outputStream, @NonNull TableAsset tableAsset) throws Exception {
    String urlString = tableAsset.getUrl();
    URL url = new URL(urlString.replaceAll(" ", "%20"));
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setDoOutput(true);
    try (BufferedInputStream in = new BufferedInputStream(url.openStream())) {
      byte dataBuffer[] = new byte[1024];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
        outputStream.write(dataBuffer, 0, bytesRead);
      }
    }
  }
}
