package de.mephisto.vpin.server;

import de.mephisto.vpin.commons.utils.Updater;
import de.mephisto.vpin.restclient.util.ZipUtil;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CountDownLatch;

public class ServerUpdatePreProcessorUI {
  private final static Logger LOG = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static void initJavaFXToolkit() {
    try {
      Platform.startup(() -> {
      });
    }
    catch (IllegalStateException e) {
      // JavaFX toolkit already initialized
    }
    Platform.setImplicitExit(false);
  }

  private static long getContentLength(String downloadUrl) {
    try {
      URL url = new URL(downloadUrl);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("HEAD");
      connection.setInstanceFollowRedirects(true);
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);
      long contentLength = connection.getContentLengthLong();
      connection.disconnect();
      return contentLength;
    }
    catch (Exception e) {
      LOG.warn("Failed to get content length for {}: {}", downloadUrl, e.getMessage());
      return -1;
    }
  }

  public static void downloadWithProgressDialog(String downloadUrl, File targetFile, File extractFolder) {
    try {
      initJavaFXToolkit();
    }
    catch (Exception e) {
      LOG.warn("JavaFX not available, downloading without progress dialog: {}", e.getMessage());
      Updater.download(downloadUrl, targetFile);
      ZipUtil.unzip(targetFile, extractFolder, null);
      return;
    }

    CountDownLatch latch = new CountDownLatch(1);

    Platform.runLater(() -> {
      Stage stage = new Stage();
      stage.initStyle(StageStyle.UNDECORATED);
      stage.setTitle("VPin Studio - Resource Installation");

      Label titleLabel = new Label("VPin Studio Server Resource Installation");
      titleLabel.setTextFill(Color.WHITE);
      titleLabel.setFont(Font.font(14));

      ProgressBar progressBar = new ProgressBar(0);
      progressBar.setPrefWidth(560);
      progressBar.setPrefHeight(14);

      Label statusLabel = new Label("Downloading " + targetFile.getName() + "...");
      statusLabel.setTextFill(Color.WHITE);

      VBox vbox = new VBox(12, titleLabel, progressBar, statusLabel);
      vbox.setPadding(new Insets(24));
      vbox.setStyle("-fx-background-color: #222222;");

      Scene scene = new Scene(vbox, 610, 130);
      stage.setScene(scene);
      stage.setAlwaysOnTop(true);
      stage.setResizable(false);
      stage.show();

      new Service<Void>() {
        @Override
        protected Task<Void> createTask() {
          return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
              try {
                long contentLength = getContentLength(downloadUrl);
                LOG.info("Download content length for {}: {}", targetFile.getName(), contentLength);

                Thread downloadThread = new Thread(() -> {
                  Thread.currentThread().setName("ResourceDownloader");
                  Updater.download(downloadUrl, targetFile);
                });
                downloadThread.start();

                if (contentLength <= 0) {
                  Platform.runLater(() -> progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS));
                }

                File tmpFile = new File(Updater.getWriteableBaseFolder(), targetFile.getName() + ".bak");
                while (downloadThread.isAlive()) {
                  Thread.sleep(500);
                  if (contentLength > 0 && tmpFile.exists()) {
                    double progress = (double) tmpFile.length() / contentLength;
                    int pct = Math.min((int) (progress * 100), 99);
                    Platform.runLater(() -> {
                      progressBar.setProgress(Math.min(progress, 0.99));
                      statusLabel.setText("Downloading " + targetFile.getName() + "... " + pct + "%");
                    });
                  }
                }

                Platform.runLater(() -> {
                  progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
                  statusLabel.setText("Installing " + targetFile.getName() + "...");
                });

                ZipUtil.unzip(targetFile, extractFolder, null);

                Platform.runLater(() -> {
                  stage.close();
                  latch.countDown();
                });
              }
              catch (Exception e) {
                LOG.error("Download with progress failed: {}", e.getMessage(), e);
                Platform.runLater(() -> {
                  stage.close();
                  latch.countDown();
                });
              }
              return null;
            }
          };
        }
      }.start();
    });

    try {
      latch.await();
    }
    catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }
}
