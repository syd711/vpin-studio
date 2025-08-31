package de.mephisto.vpin.commons.utils;

import de.mephisto.vpin.commons.fx.*;
import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import de.mephisto.vpin.commons.utils.media.*;
import de.mephisto.vpin.restclient.client.VPinStudioClient;
import de.mephisto.vpin.restclient.frontend.Frontend;
import de.mephisto.vpin.restclient.games.FrontendMediaItemRepresentation;
import de.mephisto.vpin.restclient.playlists.PlaylistRepresentation;
import de.mephisto.vpin.restclient.preferences.UISettings;
import de.mephisto.vpin.restclient.util.DateUtil;
import de.mephisto.vpin.restclient.util.FileUtils;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.apache.commons.io.FilenameUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;


public class WidgetFactory {
  private final static Logger LOG = LoggerFactory.getLogger(WidgetFactory.class);

  public static final String DISABLED_TEXT_STYLE = "-fx-font-color: #B0ABAB;-fx-text-fill:#B0ABAB;";
  public static final String DEFAULT_TEXT_STYLE = "-fx-font-color: #FFFFFF;-fx-text-fill:#FFFFFF;";
  public static final String DEFAULT_COLOR = "#FFFFFF";
  public static final String DISABLED_COLOR = "#767272";
  public static final String LOCAL_FAVS_COLOR = "#ffcc00";
  public static final String GLOBAL_FAVS_COLOR = "#cc6600";
  public static final String ERROR_COLOR = "#FF3333";
  public static final String ERROR_STYLE = "-fx-font-color: " + ERROR_COLOR + ";-fx-text-fill:" + ERROR_COLOR + ";";
  public static final String UPDATE_COLOR = "#CCFF66";
  public static final String TODO_COLOR = UPDATE_COLOR;
  public static final String OUTDATED_COLOR = "#FFCC66";
  public static final String OK_COLOR = "#66FF66";
  public static final String OK_STYLE = "-fx-font-color: " + OK_COLOR + ";-fx-text-fill:" + OK_COLOR + ";";
  public static final String MEDIA_CONTAINER_LABEL = "-fx-font-size: 14px;-fx-text-fill: #666666;";
  public static final int DEFAULT_ICON_SIZE = 18;


  public static Font scoreFont;
  public static Font scoreFontSmall;
  public static Font competitionScoreFont;
  public static Font scoreFontText;

  static {
    Font.loadFont(ServerFX.class.getResourceAsStream("MonospaceBold.ttf"), 22);
    Font.loadFont(ServerFX.class.getResourceAsStream("digital_counter_7.ttf"), 22);
    Font.loadFont(ServerFX.class.getResourceAsStream("impact.ttf"), 22);

    String SCORE_FONT_NAME = "Digital Counter 7";
    String SCORE_TEXT_FONT_NAME = "Monospace";

    scoreFont = Font.font(SCORE_FONT_NAME, FontPosture.findByName("regular"), 36);
    scoreFontSmall = Font.font(SCORE_FONT_NAME, FontPosture.findByName("regular"), 24);
    competitionScoreFont = Font.font(SCORE_FONT_NAME, FontPosture.findByName("regular"), 28);

    scoreFontText = Font.font(SCORE_TEXT_FONT_NAME, FontPosture.findByName("regular"), 16);
  }

  public static Font getCompetitionScoreFont() {
    return competitionScoreFont;
  }

  public static Font getScoreFont() {
    return scoreFont;
  }

  public static Font getScoreFontSmall() {
    return scoreFontSmall;
  }

  public static Font getScoreFontText() {
    return scoreFontText;
  }

  public static Label createDefaultLabel(String msg) {
    Label label = new Label(msg);
    label.setStyle("-fx-font-size: 14px;");
    return label;
  }

  public static File snapshot(Pane root) throws IOException {
    int offset = 14;
    SnapshotParameters snapshotParameters = new SnapshotParameters();
    Rectangle2D rectangle2D = new Rectangle2D(offset, offset, root.getWidth() - offset - offset, root.getHeight() - offset - offset);
    snapshotParameters.setViewport(rectangle2D);
    WritableImage snapshot = root.snapshot(snapshotParameters, null);
    BufferedImage bufferedImage = new BufferedImage((int) rectangle2D.getWidth(), (int) rectangle2D.getHeight(), BufferedImage.TYPE_INT_ARGB);
    File file = File.createTempFile("avatar", ".png");
    file.deleteOnExit();
    BufferedImage image = SwingFXUtils.fromFXImage(snapshot, bufferedImage);
    ImageIO.write(image, "png", file);
    LOG.info("Written avatar temp file " + file.getAbsolutePath() + " [" + FileUtils.readableFileSize(file.length()) + "]");
    return file;
  }

  public static FontIcon createCheckIcon() {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE);
    fontIcon.setIconColor(Paint.valueOf("#66FF66"));
    fontIcon.setIconLiteral("bi-check-circle");
    return fontIcon;
  }

  public static HBox createCheckAndUpdateIcon(String tooltip) {
    return addUpdateIcon(createCheckIcon(DEFAULT_COLOR), tooltip);
  }

  public static HBox createCheckAndIgnoredIcon(String tooltip) {
    return addIgnoredIcon(createCheckIcon(DEFAULT_COLOR), tooltip);
  }

  public static FontIcon createUpdateStar() {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(14);
    fontIcon.setIconColor(Paint.valueOf(UPDATE_COLOR));
    fontIcon.setIconLiteral("mdi2f-flare");
    return fontIcon;
  }


  public static FontIcon createUpdateIcon() {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE);
    fontIcon.setIconColor(Paint.valueOf(UPDATE_COLOR));
    fontIcon.setIconLiteral("mdi2a-arrow-up-thick");
    return fontIcon;
  }


  public static Label createUpdateIcon(String tooltip) {
    Label label = new Label();
    label.setTooltip(new Tooltip(tooltip));
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE);
    fontIcon.setIconColor(Paint.valueOf(UPDATE_COLOR));
    fontIcon.setIconLiteral("mdi2a-arrow-up-thick");
    label.setGraphic(createUpdateIcon());
    return label;
  }


  public static FontIcon createCheckIcon(@Nullable String color) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE);
    fontIcon.setIconLiteral("bi-check-circle");
    fontIcon.setIconColor(Paint.valueOf("#66FF66"));
    if (color != null) {
      fontIcon.setIconColor(Paint.valueOf(color));
    }
    return fontIcon;
  }

  public static FontIcon createBotIcon() {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE);
    fontIcon.setIconColor(Paint.valueOf("#5865F2"));
    fontIcon.setIconLiteral("mdi2r-robot");
    return fontIcon;
  }

  public static FontIcon createAlertIcon(String s) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE);
    fontIcon.setIconColor(Paint.valueOf(ERROR_COLOR));
    fontIcon.setIconLiteral(s);
    return fontIcon;
  }

  public static FontIcon createGreenIcon(String s) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE);
    fontIcon.setIconColor(Paint.valueOf("#66FF66"));
    fontIcon.setIconLiteral(s);
    return fontIcon;
  }

  public static FontIcon createIcon(String s) {
    return createIcon(s, null);
  }

  public static FontIcon createIcon(String s, String color) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE);
    fontIcon.setIconColor(Paint.valueOf(color != null ? color : DEFAULT_COLOR));
    fontIcon.setIconLiteral(s);
    return fontIcon;
  }

  public static FontIcon createIcon(String s, int size, String color) {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(size);
    fontIcon.setIconColor(Paint.valueOf(color != null ? color : DEFAULT_COLOR));
    fontIcon.setIconLiteral(s);
    return fontIcon;
  }

  public static FontIcon createCheckboxIcon() {
    return createIcon("bi-check-circle", DEFAULT_ICON_SIZE, null);
  }

  public static FontIcon createCheckboxIcon(@Nullable String color) {
    return createIcon("bi-check-circle", DEFAULT_ICON_SIZE, color);
  }

  public static Label createCheckboxIcon(@Nullable String color, @NonNull String tooltip) {
    Label label = new Label();
    label.setTooltip(new Tooltip(tooltip));
    FontIcon fontIcon = createCheckboxIcon(color);
    label.setGraphic(fontIcon);
    return label;
  }

  public static FontIcon createUnsupportedIcon() {
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE);
    fontIcon.setIconColor(Paint.valueOf("#FF9933"));
    fontIcon.setIconLiteral("bi-x-circle");
    return fontIcon;
  }

  public static FontIcon createExclamationIcon() {
    return createExclamationIcon(null);
  }

  public static FontIcon createExclamationIcon(@Nullable String color) {
    return createIcon("bi-exclamation-circle-fill", DEFAULT_ICON_SIZE, color != null ? color : ERROR_COLOR);
  }

  public static FontIcon createWarningIcon(@Nullable String color) {
    return createIcon("bi-exclamation-circle", DEFAULT_ICON_SIZE, color);
  }

  public static HBox addUpdateIcon(FontIcon icon, String tooltip) {
    HBox root = new HBox(3);
    root.setAlignment(Pos.CENTER);
    Label icon1 = new Label();
    icon1.setGraphic(icon);
    Label icon2 = new Label();
    icon2.setTooltip(new Tooltip(tooltip));
    icon2.setGraphic(createUpdateIcon());

    root.getChildren().addAll(icon2, icon1);
    return root;
  }

  public static HBox addIgnoredIcon(FontIcon icon, String tooltip) {
    HBox root = new HBox(3);
    root.setAlignment(Pos.CENTER);
    Label icon1 = new Label();
    icon1.setGraphic(icon);
    Label icon2 = new Label();
    icon2.setTooltip(new Tooltip(tooltip));
    icon2.setGraphic(createIcon("mdi2b-bell-cancel-outline"));

    root.getChildren().addAll(icon2, icon1);
    return root;
  }

  public static Label wrapIcon(FontIcon icon, @NonNull String tooltip) {
    Label label = new Label();
    label.setTooltip(new Tooltip(tooltip));
    label.setGraphic(icon);
    return label;
  }


  public static String hexColor(Integer color) {
    String hex = "FFFFFF";
    if (color != null) {
      if (color == 0) {
        hex = "000000";
      }
      else {
        hex = "" + Integer.toHexString(color);
      }
    }
    while (hex.length() < 6) {
      hex = "0" + hex;
    }
    return "#" + hex;
  }

  public static void createHelpIcon(Label label, String tooltip) {
    label.setText("");
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE);
    fontIcon.setIconColor(Paint.valueOf(DEFAULT_COLOR));
    fontIcon.setIconLiteral("mdi2h-help-circle-outline");
    Tooltip tt = new Tooltip(tooltip);
    tt.setWrapText(true);
    tt.setMaxWidth(350);
    label.setTooltip(tt);
    label.setGraphic(fontIcon);
  }

  public static Label createPlaylistIcon(@Nullable PlaylistRepresentation playlist, @NonNull UISettings uiSettings) {
    return createPlaylistIcon(playlist, uiSettings, false);
  }

  public static Label createPlaylistIcon(@Nullable PlaylistRepresentation playlist, @NonNull UISettings uiSettings, boolean disabled) {
    Label label = new Label();
    FontIcon fontIcon = new FontIcon();
    fontIcon.setIconSize(DEFAULT_ICON_SIZE + 2);

    String nameLower = playlist.getName().toLowerCase();
    String iconLiteral = "mdi2v-view-list";
    String iconColor = hexColor(playlist.getMenuColor());

    // Special ID cases
    if (playlist.getId() == PlaylistRepresentation.PLAYLIST_FAVORITE_ID) {
      iconLiteral = "mdi2s-star";
      iconColor = uiSettings.getLocalFavsColor();
    }
    else if (playlist.getId() == PlaylistRepresentation.PLAYLIST_GLOBALFAV_ID) {
      iconLiteral = "mdi2s-star";
      iconColor = uiSettings.getGlobalFavsColor();
    }
    else if (playlist.getId() == PlaylistRepresentation.PLAYLIST_JUSTADDED_ID) {
      iconLiteral = "mdi2d-database-clock";
      iconColor = uiSettings.getJustAddedColor();
    }
    else if (playlist.getId() == PlaylistRepresentation.PLAYLIST_MOSTPLAYED_ID) {
      iconLiteral = "mdi2p-play-box-multiple-outline";
      iconColor = uiSettings.getMostPlayedColor();
    }
    else {
      if (!(uiSettings.isHideCustomIcons())) {
        try {
          iconLiteral = determineIconLiteral(nameLower);
        }
        catch (Exception e) {
          LOG.error("Error loading icon literal: " + iconLiteral, e);
          iconLiteral = "mdi2v-view-list";
        }
      }
      else {
        if (playlist.getName().contains("Visual Pinball X")) {
          iconLiteral = "mdi2a-alpha-x-circle";
        }
        else if (playlist.getName().contains("VPX")) {
          iconLiteral = "mdi2a-alpha-x-circle";
        }
        else if (playlist.getName().contains("Future")) {
          iconLiteral = "mdi2a-alpha-f-circle";
        }
        else if (playlist.getName().contains("FX3")) {
          iconLiteral = "mdi2n-numeric-3-circle";
        }
        else if (playlist.getName().contains("Just Added")) {
          iconLiteral = "mdi2a-alpha-j-circle";
        }
        else if (playlist.getName().contains("Most Played")) {
          iconLiteral = "mdi2a-alpha-m-circle";
        }
        else if (playlist.getName().contains("Home")) {
          iconLiteral = "mdi2a-alpha-h-circle";
        }
        else if (playlist.getName().contains("VPW")) {
          iconLiteral = "mdi2a-alpha-v-circle";
        }
        else if (playlist.getName().endsWith(" M")) {
          iconLiteral = "mdi2a-alpha-m-circle";
        }
      }
    }

    //set the icon
    fontIcon.setIconLiteral(iconLiteral);
    fontIcon.setIconColor(Paint.valueOf(disabled ? WidgetFactory.DISABLED_COLOR : iconColor));
    label.setGraphic(fontIcon);
    label.setTooltip(new Tooltip(playlist.getName()));
    return label;
  }

  public static enum MatchType {
    EXACT, PREFIX, ANYWHERE
  }

  public static class KeywordRule {
    private final String keyword;
    private final String icon;
    private final MatchType type;

    public KeywordRule(String keyword, String icon, MatchType type) {
      this.keyword = keyword;
      this.icon = icon;
      this.type = type;
    }

    public String getKeyword() {
      return keyword;
    }

    public String getIcon() {
      return icon;
    }

    public MatchType getType() {
      return type;
    }
  }

  private static final List<KeywordRule> keywordRules = List.of(
      new KeywordRule("visual pinball x", "customicon-vpx_icon", MatchType.EXACT),
      new KeywordRule("vpx", "customicon-vpx_icon", MatchType.EXACT),
      new KeywordRule("future", "customicon-futurepinball_icon", MatchType.PREFIX),
      new KeywordRule("just added", "mdi2d-database-clock", MatchType.EXACT),
      new KeywordRule("added", "mdi2d-database-clock", MatchType.ANYWHERE),
      new KeywordRule("most played", "mdi2p-play-box-multiple-outline", MatchType.EXACT),
      new KeywordRule("recently played", "customicon-recentlyplayed_icon", MatchType.EXACT),
      new KeywordRule("home", "mdi2h-home-circle", MatchType.EXACT),
      new KeywordRule("vpw", "customicon-vpw_icon", MatchType.EXACT),
      new KeywordRule("updated", "mdi2u-update", MatchType.ANYWHERE),
      new KeywordRule("music", "customicon-music_icon", MatchType.EXACT),
      new KeywordRule("movie", "customicon-movie_icon", MatchType.PREFIX),
      new KeywordRule("star wars", "customicon-star_wars_icon", MatchType.EXACT),
      new KeywordRule("adult", "customicon-adult_icon", MatchType.EXACT),
      new KeywordRule("over 18", "customicon-adult_icon", MatchType.EXACT),
      new KeywordRule("top 10", "customicon-top_10_icon", MatchType.EXACT),
      new KeywordRule("pup", "customicon-pup_icon", MatchType.PREFIX),
      new KeywordRule("soccer", "customicon-soccer_icon", MatchType.EXACT),
      new KeywordRule("nfozzy", "customicon-nfozzy_icon", MatchType.EXACT),
      new KeywordRule("super", "customicon-superhero_icon", MatchType.PREFIX),
      new KeywordRule("tv", "mdi2t-television-classic", MatchType.EXACT),
      new KeywordRule("television", "mdi2t-television-classic", MatchType.EXACT),
      new KeywordRule("mame", "customicon-mame_icon", MatchType.EXACT),
      new KeywordRule("bally", "customicon-bally_icon", MatchType.EXACT),
      new KeywordRule("atari", "customicon-atari_icon", MatchType.EXACT),
      new KeywordRule("sega", "customicon-sega_icon", MatchType.EXACT),
      new KeywordRule("zaccaria", "customicon-zaccaria_icon", MatchType.EXACT),
      new KeywordRule("east", "customicon-dataeast_icon", MatchType.ANYWHERE),
      new KeywordRule("midway", "customicon-midway_icon", MatchType.EXACT),
      new KeywordRule("gottlieb", "customicon-gottlieb_icon", MatchType.EXACT),
      new KeywordRule("williams", "customicon-williams_icon", MatchType.EXACT),
      new KeywordRule("stern", "customicon-stern_icon", MatchType.EXACT),
      new KeywordRule("chicago", "customicon-chicago_icon", MatchType.EXACT),
      new KeywordRule("50", "customicon-fifties_icon", MatchType.ANYWHERE),
      new KeywordRule("fift", "customicon-fifties_icon", MatchType.PREFIX),
      new KeywordRule("60", "customicon-sixties_icon", MatchType.ANYWHERE),
      new KeywordRule("sixt", "customicon-sixties_icon", MatchType.PREFIX),
      new KeywordRule("70", "customicon-seventies_icon", MatchType.ANYWHERE),
      new KeywordRule("seven", "customicon-seventies_icon", MatchType.PREFIX),
      new KeywordRule("80", "customicon-eighties_icon", MatchType.ANYWHERE),
      new KeywordRule("eight", "customicon-eighties_icon", MatchType.PREFIX),
      new KeywordRule("90", "customicon-nineties_icon", MatchType.ANYWHERE),
      new KeywordRule("nine", "customicon-nineties_icon", MatchType.PREFIX),
      new KeywordRule("00", "customicon-aughts_icon", MatchType.ANYWHERE),
      new KeywordRule("aught", "customicon-aughts_icon", MatchType.PREFIX),
      new KeywordRule("tens", "customicon-tens_icon", MatchType.PREFIX),
      new KeywordRule("10", "customicon-tens_icon", MatchType.ANYWHERE),
      new KeywordRule("fx3", "customicon-fx3_icon", MatchType.EXACT),
      new KeywordRule("fx", "customicon-fx_icon", MatchType.EXACT),
      new KeywordRule("vr", "customicon-vr_icon", MatchType.EXACT),
      new KeywordRule("capcom", "customicon-capcom_icon", MatchType.EXACT),
      new KeywordRule("black", "customicon-bw_icon", MatchType.PREFIX),
      new KeywordRule("b&w", "customicon-bw_icon", MatchType.EXACT),
      new KeywordRule("kids", "customicon-kids_icon", MatchType.EXACT),
      new KeywordRule("under 18", "customicon-kids_icon", MatchType.EXACT),
      new KeywordRule("children", "customicon-kids_icon", MatchType.EXACT),
      new KeywordRule("mod", "customicon-mod_icon", MatchType.PREFIX),
      new KeywordRule("solid", "customicon-ss_icon", MatchType.PREFIX),
      new KeywordRule("ss", "customicon-ss_icon", MatchType.EXACT),
      new KeywordRule("em", "customicon-em_icon", MatchType.EXACT),
      new KeywordRule("electro", "customicon-em_icon", MatchType.PREFIX),
      new KeywordRule("iscore", "customicon-iscored_icon", MatchType.PREFIX),
      new KeywordRule("tourn", "mdi2t-trophy-variant", MatchType.PREFIX),
      new KeywordRule("compet", "mdi2t-trophy-variant", MatchType.PREFIX),
      new KeywordRule("pinball m", "customicon-pinballm_icon", MatchType.EXACT),
      new KeywordRule(" m", "customicon-pinballm_icon", MatchType.ANYWHERE),
      new KeywordRule("a to z", "customicon-atoz_icon", MatchType.EXACT),
      new KeywordRule("atoz", "customicon-atoz_icon", MatchType.EXACT),
      new KeywordRule("a2z", "customicon-atoz_icon", MatchType.EXACT),
      new KeywordRule("a-z", "customicon-atoz_icon", MatchType.EXACT),
      new KeywordRule("a - z", "customicon-atoz_icon", MatchType.EXACT),
      new KeywordRule("alphabet", "customicon-atoz_icon", MatchType.PREFIX),
      new KeywordRule("#", "mdi2m-music-accidental-sharp", MatchType.ANYWHERE)
  );

  private static String determineIconLiteral(String nameLower) {
    for (KeywordRule rule : keywordRules) {
      String pattern;

      switch (rule.getType()) {
        case EXACT:
          pattern = "\\b" + Pattern.quote(rule.getKeyword()) + "\\b";
          break;
        case PREFIX:
          pattern = "\\b" + Pattern.quote(rule.getKeyword());
          break;
        case ANYWHERE:
          pattern = Pattern.quote(rule.getKeyword());
          break;
        default:
          throw new IllegalStateException("Unexpected match type: " + rule.getType());
      }

      if (Pattern.compile(pattern).matcher(nameLower).find()) {
        return rule.getIcon();
      }
    }

    char firstChar = nameLower.charAt(0);
    // Default fallback: alphabet letter, number, or standard list
    if (Character.isLetter(firstChar)) {
      return "mdi2a-alpha-" + nameLower.charAt(0) + "-circle";
    }
    else if (Character.isDigit(firstChar)) {
      return "mdi2n-numeric-" + nameLower.charAt(0) + "-circle";
    }
    else {
      // Do something for symbols, punctuation, etc.
      return "mdi2v-view-list";
    }


  }

  public static Stage createStage() {
    Stage stage = new Stage();
    stage.getIcons().add(new Image(ServerFX.class.getResourceAsStream("logo-64.png")));
    return stage;
  }

  public static Stage createDialogStage(Class clazz, Stage owner, String title, String fxml) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    String stateId = FilenameUtils.getBaseName(fxml);
    return createDialogStage(stateId, fxmlLoader, owner, title, null);
  }

  public static Stage createDialogStage(String stateId, Class clazz, Stage owner, String title, String fxml) {
    FXMLLoader fxmlLoader = new FXMLLoader(clazz.getResource(fxml));
    return createDialogStage(stateId, fxmlLoader, owner, title, null);
  }

  public static void addToTextListener(Label label) {
    label.managedProperty().bindBidirectional(label.visibleProperty());
    label.textProperty().addListener(new ChangeListener<String>() {
      @Override
      public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
        if (!label.isVisible()) {
          TextField textarea = (TextField) label.getUserData();
          if (textarea != null) {
            ((Pane) label.getParent()).getChildren().remove(textarea);
            label.setVisible(true);
          }
        }
      }
    });

    label.setOnMouseClicked(new EventHandler<MouseEvent>() {
      @Override
      public void handle(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {
          if (mouseEvent.getClickCount() == 2) {
            label.setVisible(false);
            TextField textarea = new TextField(label.getText());
            textarea.setEditable(false);
            textarea.setPrefHeight(label.getHeight());
            textarea.setStyle("-fx-font-size: 14px;");
            label.setUserData(textarea);
            int i = ((Pane) label.getParent()).getChildren().indexOf(label);
            ((Pane) label.getParent()).getChildren().add(i, textarea);
            Platform.runLater(() -> {
              textarea.requestFocus();
              textarea.selectAll();
            });

            textarea.setOnKeyPressed(event -> {
              if (event.getCode().toString().equals("ENTER") || event.getCode().toString().equalsIgnoreCase("ESCAPE")) {
                ((Pane) label.getParent()).getChildren().remove(textarea);
                label.setVisible(true);
              }
            });
          }
        }
      }
    });
  }

  public static Stage createDialogStage(String stateId, FXMLLoader fxmlLoader, Stage owner, String title) {
    return createDialogStage(stateId, fxmlLoader, owner, title, null);
  }

  public static Stage createDialogStage(String stateId, FXMLLoader fxmlLoader, Stage owner, String title, String modalStateId) {
    Parent root = null;

    try {
      root = fxmlLoader.load();
    }
    catch (IOException e) {
      LOG.error("Error loading: " + e.getMessage(), e);
    }

    DialogController controller = fxmlLoader.getController();
    final Stage stage = createStage();

    Node header = root.lookup("#header");
    Object userData = header.getUserData();
    if (userData instanceof DialogHeaderController) {
      DialogHeaderController dialogHeaderController = (DialogHeaderController) userData;
      dialogHeaderController.setStage(stage);
      dialogHeaderController.setTitle(title);
      dialogHeaderController.setModal(modalStateId == null || LocalUISettings.isModal(modalStateId));
      stage.setOnShowing(new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent event) {
          Platform.runLater(() -> {
            dialogHeaderController.enableStateListener(stage, controller, stateId);
          });
        }
      });
    }
    else if (userData instanceof DialogHeaderResizeableController) {
      DialogHeaderResizeableController dialogHeaderController = (DialogHeaderResizeableController) userData;
      dialogHeaderController.setStateId(stateId);
      dialogHeaderController.setTitle(title);
      dialogHeaderController.setMaximizeable(LocalUISettings.isMaximizeable(stateId));
    }

    if (modalStateId != null && !LocalUISettings.isModal(modalStateId)) {
      stage.initModality(Modality.NONE);
    }
    else {
      stage.initOwner(owner);
      stage.initModality(Modality.APPLICATION_MODAL);
    }

    stage.initStyle(StageStyle.UNDECORATED);
    stage.setTitle(title);
    stage.setUserData(controller);

    if (stateId != null && !isIgnoredState(stateId)) {
      stage.setResizable(true);

      Rectangle position = LocalUISettings.getPosition(stateId);
      if (position != null) {
        //let dialog open on the screen the main window is
        stage.setX(position.getX());
        stage.setY(position.getY());

        if (position.getWidth() > 0 && position.getHeight() > 0) {
          stage.setWidth(position.getWidth());
          stage.setHeight(position.getHeight());
        }
      }
    }

    stage.initOwner(owner);
    Scene scene = new Scene(root);
    stage.setScene(scene);
    scene.getRoot().setStyle("-fx-border-width: 1;-fx-border-color: #605E5E;");
    scene.addEventHandler(KeyEvent.KEY_PRESSED, t -> {
      if (t.getCode() == KeyCode.ESCAPE) {
        if (controller != null) {
          controller.onDialogCancel();
        }
        t.consume();
        stage.close();
      }
      else {
        if (controller != null) {
          controller.onKeyPressed(t);
        }
      }
    });

    scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
          public void handle(KeyEvent ke) {
            if (ke.getCode() == KeyCode.S && ke.isAltDown() && ke.isControlDown()) {
              LOG.info("Stage Size " + stage.getWidth() + " x " + stage.getHeight());
            }
          }
        }
    );

    return stage;
  }

  private static boolean isIgnoredState(String stateId) {
    if (stateId == null) {
      return true;
    }

    if ("defaultModal".equals(stateId)) {
      return true;
    }
    return false;
  }

  public static Optional<ButtonType> showConfirmation(Stage owner, String text) {
    return showConfirmation(owner, text, null, null);
  }

  public static Optional<ButtonType> showConfirmation(Stage owner, String text, String help1) {
    return showConfirmation(owner, text, help1, null);
  }

  public static Optional<ButtonType> showConfirmation(Stage owner, String text, String help1, String help2) {
    return showConfirmation(owner, text, help1, help2, null);
  }

  public static Optional<ButtonType> showConfirmationWithOption(Stage owner, String text, String help1, String help2, String btnText, String optionText) {
    Stage stage = createDialogStage("defaultModal", ConfirmationDialogWithOptionController.class, owner, "Confirmation", "dialog-confirmation-with-option.fxml");
    ConfirmationDialogWithOptionController controller = (ConfirmationDialogWithOptionController) stage.getUserData();
    controller.initDialog(stage, optionText, btnText, text, help1, help2);
    stage.showAndWait();
    return controller.getResult();
  }

  public static Optional<ButtonType> showConfirmation(Stage owner, String text, String help1, String help2, String btnText) {
    Stage stage = createDialogStage("defaultModal", ConfirmationDialogController.class, owner, "Confirmation", "dialog-confirmation.fxml");
    ConfirmationDialogController controller = (ConfirmationDialogController) stage.getUserData();
    controller.initDialog(stage, null, btnText, text, help1, help2);
    stage.showAndWait();
    return controller.getResult();
  }

  public static Optional<ButtonType> showYesNoConfirmation(Stage owner, String text, String help) {
    return showYesNoConfirmation(owner, text, help, null);
  }

  public static Optional<ButtonType> showYesNoConfirmation(Stage owner, String text, String help1, String help2) {
    Optional<ButtonType> result = showConfirmationWithOption(owner, text, help1, help2, "Yes", "No");
    if (result.isPresent()) {
      if (ButtonType.APPLY.equals(result.get())) {
        return Optional.of(ButtonType.NO);
      }
      else if (ButtonType.OK.equals(result.get())) {
        return Optional.of(ButtonType.YES);
      }
    }
    return result;
  }

  public static Optional<ButtonType> showInformation(Stage owner, String text, String help1) {
    return showInformation(owner, text, help1, null);
  }

  public static Optional<ButtonType> showInformation(Stage owner, String text, String help1, String help2) {
    Stage stage = createDialogStage("defaultModal", ConfirmationDialogController.class, owner, "Information", "dialog-confirmation.fxml");
    ConfirmationDialogController controller = (ConfirmationDialogController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, text, help1, help2);
    stage.showAndWait();
    return controller.getResult();
  }

  public static void showAlert(Stage owner, String msg) {
    showAlert(owner, msg, null, null);
  }

  public static void showAlert(Stage owner, String msg, String help1) {
    showAlert(owner, msg, help1, null);
  }

  public static void showAlert(Stage owner, String msg, String help1, String help2) {
    Stage stage = createDialogStage("defaultModal", ConfirmationDialogController.class, owner, "Information", "dialog-alert.fxml");
    ConfirmationDialogController controller = (ConfirmationDialogController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, msg, help1, help2);
    stage.showAndWait();
  }

  public static Optional<ButtonType> showAlertOption(Stage owner, String msg, String altOptionText, String okText, String help1, String help2) {
    Stage stage = createDialogStage("defaultModal", ConfirmationDialogController.class, owner, "Information", "dialog-alert-option.fxml");
    ConfirmationDialogController controller = (ConfirmationDialogController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, altOptionText, okText, msg, help1, help2);
    stage.showAndWait();
    return controller.getResult();
  }

  public static ConfirmationResult showAlertOptionWithCheckbox(Stage owner, String msg, String altOptionText, String okText, String help1, String help2, String checkBoxText) {
    return showAlertOptionWithCheckbox(owner, msg, altOptionText, okText, help1, help2, checkBoxText, true);
  }

  public static ConfirmationResult showAlertOptionWithCheckbox(Stage owner, String msg, String altOptionText, String okText, String help1, String help2, String checkBoxText, boolean checked) {
    Stage stage = createDialogStage("defaultModal", ConfirmationDialogWithCheckboxController.class, owner, "Information", "dialog-alert-option-with-checkbox.fxml");
    ConfirmationDialogWithCheckboxController controller = (ConfirmationDialogWithCheckboxController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, altOptionText, okText, msg, help1, help2, checkBoxText);
    controller.setChecked(checked);
    stage.showAndWait();
    return controller.getResult();
  }

  public static ConfirmationResult showConfirmationWithCheckbox(Stage owner, String msg, String okText, String help1, String help2, String checkBoxText, boolean checked) {
    Stage stage = createDialogStage("defaultModal", ConfirmationDialogWithCheckboxController.class, owner, "Information", "dialog-confirmation-with-checkbox.fxml");
    ConfirmationDialogWithCheckboxController controller = (ConfirmationDialogWithCheckboxController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, null, okText, msg, help1, help2, checkBoxText);
    controller.setChecked(checked);
    stage.showAndWait();
    return controller.getResult();
  }

  public static ConfirmationResult showConfirmationWithCheckbox(Stage owner, String msg, String okText, String altText, String help1, String help2, String checkBoxText, boolean checked) {
    Stage stage = createDialogStage("defaultModal", ConfirmationDialogWithCheckboxController.class, owner, "Information", "dialog-confirmation-with-checkbox.fxml");
    ConfirmationDialogWithCheckboxController controller = (ConfirmationDialogWithCheckboxController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, altText, okText, msg, help1, help2, checkBoxText);
    controller.setChecked(checked);
    stage.showAndWait();
    return controller.getResult();
  }

  public static ConfirmationResult showAlertOptionWithMandatoryCheckbox(Stage owner, String msg, String altOptionText, String okText, String help1, String help2, String checkBoxText) {
    Stage stage = createDialogStage("defaultModal", ConfirmationDialogWithCheckboxController.class, owner, "Information", "dialog-alert-option-with-checkbox.fxml");
    ConfirmationDialogWithCheckboxController controller = (ConfirmationDialogWithCheckboxController) stage.getUserData();
    controller.hideCancel();
    controller.initDialog(stage, altOptionText, okText, msg, help1, help2, checkBoxText);
    controller.setCheckboxMandatory();
    stage.showAndWait();
    return controller.getResult();
  }

  public static String showInputDialog(Stage owner, String dialogTitle, String innerTitle, String description, String helpText, String defaultValue) {
    Stage stage = createDialogStage("defaultModal", InputDialogController.class, owner, dialogTitle, "dialog-input.fxml");
    InputDialogController controller = (InputDialogController) stage.getUserData();
    controller.initDialog(stage, innerTitle, description, helpText, defaultValue);
    stage.showAndWait();
    Optional<ButtonType> result = controller.getResult();
    if (result.get().equals(ButtonType.OK)) {
      return controller.getText();
    }

    return null;
  }

  public static void showOutputDialog(Stage owner, String dialogTitle, String innerTitle, String description, String defaultValue) {
    Stage stage = createDialogStage("textoutput", OutputDialogController.class, owner, dialogTitle, "dialog-output.fxml");
    OutputDialogController controller = (OutputDialogController) stage.getUserData();
    controller.initDialog(stage, innerTitle, description, defaultValue);
    stage.showAndWait();
  }

  public static class RationListCell extends ListCell<String> {
    protected void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      setText(null);
      if (item != null) {
        setText(item
            .replaceAll("_", " ")
            .replaceAll("ATIO", "atio")
            .replaceAll("x", " x ")
        );
      }
    }
  }

  public static void createMediaContainer(VPinStudioClient client, BorderPane parent, FrontendMediaItemRepresentation mediaItem, boolean previewEnabled) {
    disposeMediaPane(parent);

    if (mediaItem == null) {
      createNoMediaLabel(parent);
    }

    if (!previewEnabled) {
      Label label = new Label("Preview disabled");
      if (mediaItem == null) {
        label.setText("No media found");
      }
      label.setUserData(mediaItem);
      label.setStyle(MEDIA_CONTAINER_LABEL);

      if (mediaItem != null) {
        label.setStyle("-fx-font-color: #33CC00;-fx-text-fill:#33CC00; -fx-font-weight: bold;");
      }
      parent.setCenter(label);
    }

    if (previewEnabled && mediaItem != null) {
      addMediaItemToBorderPane(client, mediaItem, parent);
      Tooltip.install(parent, createMediaItemTooltip(mediaItem));
    }
  }

  public static void createNoMediaLabel(BorderPane parent) {
    Label label = new Label("No media found");
    label.setStyle("-fx-font-size: 14px;-fx-text-fill: #444444;");
    parent.setCenter(label);
  }

  public static AssetMediaPlayer addMediaItemToBorderPane(String url, BorderPane parent) {
    Image image = new Image(url);
    return addMediaItemToBorderPane(image, parent);
  }
  public static AssetMediaPlayer addMediaItemToBorderPane(Image image, BorderPane parent) {
    ImageViewer imageViewer = new ImageViewer();
    parent.setCenter(imageViewer);
    parent.setUserData(imageViewer);
    imageViewer.render(image);
    return imageViewer;
  }

  public static AssetMediaPlayer addMediaItemToBorderPane(VPinStudioClient client, FrontendMediaItemRepresentation mediaItem, BorderPane parent) {
    return addMediaItemToBorderPane(client, mediaItem, parent, null, null);
  }

  public static AssetMediaPlayer addMediaItemToBorderPane(VPinStudioClient client, FrontendMediaItemRepresentation mediaItem, BorderPane parent, MediaPlayerListener listener, MediaOptions mediaOptions) {
      return addMediaItemToBorderPane(client, mediaItem, parent, listener, mediaOptions, false);
  }

  public static AssetMediaPlayer addMediaItemToBorderPane(VPinStudioClient client, FrontendMediaItemRepresentation mediaItem, BorderPane parent, 
          MediaPlayerListener listener, MediaOptions mediaOptions, boolean noLoading) {
    String mimeType = mediaItem.getMimeType();
    if (mimeType == null) {
      LOG.info("Failed to resolve mime type for " + mediaItem);
      return null;
    }

    boolean audioOnly = parent.getId().equalsIgnoreCase("screenAudioLaunch") || parent.getId().equalsIgnoreCase("screenAudio");
    String baseType = mimeType.split("/")[0];
    String url = client.getURL(mediaItem.getUri());
    url += "/" + URLEncoder.encode(mediaItem.getName(), Charset.defaultCharset());

    Frontend frontend = client.getFrontendService().getFrontendCached();

    if (baseType.equals("image") && !audioOnly) {
      Image image = new Image(url);
      ImageViewer imageViewer = new ImageViewer();
      imageViewer.setNoLoading(noLoading);
      parent.setCenter(imageViewer);
      parent.setUserData(imageViewer);
      imageViewer.render(mediaItem, image, frontend.isPlayfieldMediaInverted());
      return imageViewer;
    }
    else if (baseType.equals("audio")) {
      AudioMediaPlayer audioMediaPlayer = new AudioMediaPlayer();
      audioMediaPlayer.setMediaOptions(mediaOptions);
      audioMediaPlayer.setNoLoading(noLoading);
      parent.setCenter(audioMediaPlayer);
      if (listener != null) {
        audioMediaPlayer.addListener(listener);
      }
      audioMediaPlayer.render(mediaItem, url);
      return audioMediaPlayer;
    }
    else if (baseType.equals("video") && !audioOnly) {
      VideoMediaPlayer videoMediaPlayer = new VideoMediaPlayer(mimeType, frontend.isPlayfieldMediaInverted());
      videoMediaPlayer.setMediaOptions(mediaOptions);
      videoMediaPlayer.setNoLoading(noLoading);
      parent.setCenter(videoMediaPlayer);
      if (listener != null) {
        videoMediaPlayer.addListener(listener);
      }
      videoMediaPlayer.render(mediaItem, url);
      return videoMediaPlayer;
    }
    else {
      LOG.error("Invalid media mime type " + mimeType + " of asset used for media panel " + parent.getId());
    }

    return null;
  }

  public static Tooltip createMediaItemTooltip(FrontendMediaItemRepresentation item) {
    StringBuilder builder = new StringBuilder(item.getName());
    builder.append("\n");
    builder.append("Size: ");
    builder.append(FileUtils.readableFileSize(item.getSize()));
    builder.append("\n");
    builder.append("Last Modified: ");
    builder.append(DateUtil.formatDateTime(item.getModificationDate()));
    return new Tooltip(builder.toString());
  }

  public static class HighscoreBackgroundImageListCell extends ListCell<String> {
    private final VPinStudioClient client;

    public HighscoreBackgroundImageListCell(VPinStudioClient client) {
      this.client = client;
    }

    protected void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(null);
      setText(null);
      if (item != null) {
        Image image = new Image(client.getHighscoreCardsService().getHighscoreBackgroundImage(item));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(80);

        int percentageWidth = (int) (80 * 100 / image.getWidth());
        int height = (int) (image.getHeight() * percentageWidth / 100);
        imageView.setFitHeight(height);
        setGraphic(imageView);
        setText(item);
      }
    }
  }

  public static class VpsTableListCell extends ListCell<String> {

    private final String comment;
    private final List<String> authors;
    private final String version;
    private final List<String> features;

    public VpsTableListCell(String comment, List<String> authors, String version, List<String> features) {
      this.comment = comment;
      this.authors = authors;
      this.version = version;
      this.features = features;
    }

    protected void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(null);
      setText(null);
      if (item != null) {
        VBox root = new VBox();
        root.setStyle("-fx-padding: 3 3 3 3");

        if (comment != null) {
          Label label = new Label(comment);
          root.getChildren().add(label);
        }

        setGraphic(root);
        setText(item);
      }
    }
  }

  public static void scrollTo(ScrollPane scrollPane, Node node) {
    Bounds viewport = scrollPane.getViewportBounds();
    double contentHeight = scrollPane.getContent().localToScene(scrollPane.getContent().getBoundsInLocal()).getHeight();
    double nodeMinY = node.localToScene(node.getBoundsInLocal()).getMinY();
    double nodeMaxY = node.localToScene(node.getBoundsInLocal()).getMaxY();

    double vValueDelta = 0;
    double vValueCurrent = scrollPane.getVvalue();

    if (nodeMaxY < 0) {
      // currently located above (remember, top left is (0,0))
      vValueDelta = (nodeMinY - viewport.getHeight()) / contentHeight;
    }
    else if (nodeMinY > viewport.getHeight()) {
      // currently located below
      vValueDelta = (nodeMinY + viewport.getHeight()) / contentHeight;
    }
    scrollPane.setVvalue(vValueCurrent + vValueDelta);
  }

  public static void disposeMediaPane(BorderPane parent) {
    Tooltip.uninstall(parent, null);
    if (parent.getCenter() != null) {
      Node node = parent.getCenter();
      if (node instanceof AssetMediaPlayer) {
        ((AssetMediaPlayer) node).disposeMedia();
      }
      else if (node instanceof ImageViewer) {
        ((ImageViewer) node).disposeImage();
      }
      parent.setCenter(null);
    }
  }
}
