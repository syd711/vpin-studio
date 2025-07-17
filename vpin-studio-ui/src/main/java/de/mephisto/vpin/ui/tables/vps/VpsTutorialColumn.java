package de.mephisto.vpin.ui.tables.vps;

import de.mephisto.vpin.commons.utils.WidgetFactory;
import de.mephisto.vpin.connectors.vps.model.*;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.util.HttpUtils;
import de.mephisto.vpin.restclient.vps.VpsSettings;
import de.mephisto.vpin.ui.Studio;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VpsTutorialColumn extends HBox {
  private final static Logger LOG = LoggerFactory.getLogger(VpsTutorialColumn.class);

  public VpsTutorialColumn(@Nullable String vpsTableId) {
    super(0);
    try {
      int iconSize = 22;
      this.setAlignment(Pos.CENTER_LEFT);

      VpsTable vpsTable = Studio.client.getVpsService().getTableById(vpsTableId);

      List<VpsTutorialUrls> tutorialFiles = vpsTable.getTutorialFiles();
      Collections.sort(tutorialFiles, new Comparator<VpsTutorialUrls>() {
        @Override
        public int compare(VpsTutorialUrls o1, VpsTutorialUrls o2) {
          return String.join(",", o1.getAuthors()).compareTo(String.join(",", o2.getAuthors()));
        }
      });

      for (VpsTutorialUrls tutorialFile : tutorialFiles) {
        Button btn = new Button();
        btn.getStyleClass().add("table-media-button");
        String authors = String.join(", ", tutorialFile.getAuthors());

        if (tutorialFile.getAuthors().contains("Kongedam")) {
          FontIcon icon = WidgetFactory.createIcon("mdi2a-alpha-k-box-outline");
          icon.setIconColor(Paint.valueOf(WidgetFactory.UPDATE_COLOR));
          icon.setIconSize(iconSize);
          String videoUrl = "https://assets.vpin-mania.net/tutorials/kongedam/" + vpsTable.getId() + ".mp4";
          boolean check = HttpUtils.check(videoUrl);
          if (check) {
            btn.setTooltip(new Tooltip("Michael Kongedam tutorial, available on vpin-mania.net"));
            icon.setIconColor(Paint.valueOf(WidgetFactory.OK_COLOR));
          }
          else {
            btn.setTooltip(new Tooltip("The Kongedam tutorial video has not been uploaded to VPin Mania yet."));
            icon.setIconColor(Paint.valueOf(WidgetFactory.ERROR_COLOR));
          }
          btn.setGraphic(icon);
        }
        else {
          FontIcon icon = WidgetFactory.createIcon("mdi2v-video-box");
          if(authors.contains("PAPA")) {
            icon = WidgetFactory.createIcon("mdi2a-alpha-p-box-outline");
          }
          else if(authors.contains("Majestic")) {
            icon = WidgetFactory.createIcon("mdi2a-alpha-m-box-outline");
          }
          else if(authors.contains("Digital")) {
            icon = WidgetFactory.createIcon("mdi2a-alpha-d-box-outline");
          }
          if (tutorialFile.getAuthors() != null) {
            btn.setTooltip(new Tooltip(authors));
          }
          icon.setIconSize(iconSize);
          btn.setGraphic(icon);
        }

        if (tutorialFile.getUrls() != null && !tutorialFile.getUrls().isEmpty()) {
          VpsUrl vpsUrl = tutorialFile.getUrls().get(0);
          if (!vpsUrl.isBroken()) {
            btn.setOnAction((actionEvent) -> {
              Studio.browse(vpsUrl.getUrl());
            });
            this.getChildren().add(btn);
          }
        }
      }

    }
    catch (Exception e) {
      LOG.error("Failed to render VPS table container: " + e.getMessage(), e);
      this.getChildren().add(new Label("ERROR"));
    }
  }

  public static boolean isFiltered(VpsSettings vpsSettings, VPSChange change) {
    if (vpsSettings != null) {
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.b2s) && !vpsSettings.isVpsBackglass()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.topper) && !vpsSettings.isVpsToppper()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.pov) && !vpsSettings.isVpsPOV()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.rom) && !vpsSettings.isVpsRom()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.altColor) && !vpsSettings.isVpsAltColor()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.altSound) && !vpsSettings.isVpsAltSound()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.pupPack) && !vpsSettings.isVpsPUPPack()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.sound) && !vpsSettings.isVpsSound()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.wheel) && !vpsSettings.isVpsWheel()) {
        return true;
      }
      if (change.getDiffType() != null && change.getDiffType().equals(VpsDiffTypes.tutorial) && !vpsSettings.isVpsTutorial()) {
        return true;
      }
    }
    return false;
  }
}
