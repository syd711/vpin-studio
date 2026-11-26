package de.mephisto.vpin.ui.util;

import javafx.scene.control.Labeled;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java access to the central color palette defined in {@code stylesheet-colors.css}. The constants hold the hex value
 * of the matching {@code -c-*} token, e.g. {@link #ERROR} is {@code -c-error}. Don't hard code colors in Java code,
 * add a token to the stylesheet and a constant here instead.
 */
public final class ThemeColors {
  private static final Logger LOG = LoggerFactory.getLogger(ThemeColors.class);

  public static final String RESOURCE = "/de/mephisto/vpin/ui/stylesheet-colors.css";

  private static final Pattern TOKEN_PATTERN = Pattern.compile("(?<![\\w-])(-c-[\\w-]+)\\s*:\\s*([^;]+);");
  private static final Pattern HEX_PATTERN = Pattern.compile("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?");

  private static final Map<String, String> TOKENS = load();

  // Surfaces, dark to light (see UI-STYLEGUIDE.md, section 1)
  public static final String BLACK = hex("-c-black");
  public static final String DARKEST = hex("-c-darkest");
  public static final String FEATURE_OFF = hex("-c-feature-off");
  public static final String INSET = hex("-c-inset");
  public static final String FOOTER = hex("-c-footer");
  public static final String INSET_HOVER = hex("-c-inset-hover");
  public static final String CARD_BACKGROUND = hex("-c-card-background");
  public static final String PANEL = hex("-c-panel");
  public static final String ROW_HOVER = hex("-c-row-hover");
  public static final String SEPARATOR = hex("-c-separator");
  public static final String SELECTION_PANEL_HOVER = hex("-c-selection-panel-hover");
  public static final String TILE = hex("-c-tile");
  public static final String PANEL_BORDER = hex("-c-panel-border");
  public static final String CONTROL = hex("-c-control");
  public static final String BAR_BUTTON_BORDER = hex("-c-bar-button-border");
  public static final String SELECTED = hex("-c-selected");
  public static final String CONTROL_HOVER = hex("-c-control-hover");
  public static final String BORDER = hex("-c-border");
  public static final String BAR_BUTTON = hex("-c-bar-button");
  public static final String MONITOR_BORDER = hex("-c-monitor-border");
  public static final String BORDER_STRONG = hex("-c-border-strong");

  // Text and neutral icons
  public static final String DISABLED = hex("-c-disabled");
  public static final String ICON_DISABLED = hex("-c-icon-disabled");
  public static final String ICON_INACTIVE = hex("-c-icon-inactive");
  public static final String SUBTLE = hex("-c-subtle");
  public static final String TEXT_PLACEHOLDER = hex("-c-text-placeholder");
  public static final String MUTED = hex("-c-muted");
  public static final String TEXT_SECONDARY = hex("-c-text-secondary");
  public static final String TEXT_SOFT = hex("-c-text-soft");
  public static final String TEXT_LIGHT = hex("-c-text-light");
  public static final String WHITE = hex("-c-white");
  public static final String LINK = hex("-c-link");

  // Legacy warm grays (formerly -fx-base-1 .. -fx-base-5)
  public static final String WARM_WHITE = hex("-c-warm-white");
  public static final String WARM_GRAY_LIGHT = hex("-c-warm-gray-light");
  public static final String WARM_GRAY = hex("-c-warm-gray");
  public static final String WARM_GRAY_DARK = hex("-c-warm-gray-dark");
  public static final String WARM_GRAY_DARKER = hex("-c-warm-gray-darker");

  // Status colors (see UI-STYLEGUIDE.md, 'Accents and status')
  public static final String OK = hex("-c-ok");
  public static final String OK_BG = hex("-c-ok-bg");
  public static final String OK_BANNER = hex("-c-ok-banner");
  public static final String WARN = hex("-c-warn");
  public static final String WARN_HOVER = hex("-c-warn-hover");
  public static final String WARN_BG = hex("-c-warn-bg");
  public static final String WARN_BANNER = hex("-c-warn-banner");
  public static final String ON_WARN = hex("-c-on-warn");
  public static final String ERROR = hex("-c-error");
  public static final String ERROR_BG = hex("-c-error-bg");
  public static final String ERROR_BANNER = hex("-c-error-banner");
  public static final String ERROR_BORDER = hex("-c-error-border");
  public static final String ERROR_HOVER = hex("-c-error-hover");
  public static final String ERROR_TEXT = hex("-c-error-text");
  public static final String ERROR_TEXT_LIGHT = hex("-c-error-text-light");
  public static final String ERROR_ICON = hex("-c-error-icon");
  public static final String ERROR_DIM = hex("-c-error-dim");
  public static final String INFO = hex("-c-info");
  public static final String INFO_BANNER = hex("-c-info-banner");
  public static final String INFO_BORDER = hex("-c-info-border");

  // Achievements palettes (rarity, rank medals, striped rows)
  public static final String ROW_EVEN = hex("-c-row-even");
  public static final String ROW_ODD = hex("-c-row-odd");
  public static final String RARITY_COMMON = hex("-c-rarity-common");
  public static final String RARITY_RARE = hex("-c-rarity-rare");
  public static final String RARITY_EPIC = hex("-c-rarity-epic");
  public static final String MEDAL_GOLD = hex("-c-medal-gold");
  public static final String MEDAL_GOLD_DARK = hex("-c-medal-gold-dark");
  public static final String MEDAL_SILVER = hex("-c-medal-silver");
  public static final String MEDAL_BRONZE = hex("-c-medal-bronze");
  public static final String MEDAL_BRONZE_DARK = hex("-c-medal-bronze-dark");

  // Legacy accents (older views, not part of the style guide palette)
  public static final String PRIMARY = hex("-c-primary");
  public static final String PRIMARY_HOVER_BORDER = hex("-c-primary-hover-border");
  public static final String HIGHLIGHT = hex("-c-highlight");
  public static final String HIGHLIGHT_TRANSLUCENT = hex("-c-highlight-translucent");
  public static final String HIGHLIGHT_FAINT = hex("-c-highlight-faint");
  public static final String BAR_BUTTON_HOVER = hex("-c-bar-button-hover");
  public static final String EDIT_BUTTON = hex("-c-edit-button");
  public static final String EDIT_BUTTON_HOVER = hex("-c-edit-button-hover");
  public static final String FOCUS_BORDER = hex("-c-focus-border");
  public static final String LINK_BASE = hex("-c-link-base");
  public static final String DANGER = hex("-c-danger");
  public static final String DANGER_DARK = hex("-c-danger-dark");
  public static final String DANGER_DARKER = hex("-c-danger-darker");
  public static final String DANGER_BORDER = hex("-c-danger-border");
  public static final String INSTALLER_HOVER = hex("-c-installer-hover");
  public static final String DELETE_SOFT = hex("-c-delete-soft");
  public static final String RECORDING = hex("-c-recording");
  public static final String RECORDING_BG = hex("-c-recording-bg");
  public static final String SUCCESS = hex("-c-success");
  public static final String SUCCESS_LIGHT = hex("-c-success-light");
  public static final String SUCCESS_DARK = hex("-c-success-dark");
  public static final String SUCCESS_DARKER = hex("-c-success-darker");
  public static final String GREEN = hex("-c-green");
  public static final String GREEN_HOVER = hex("-c-green-hover");
  public static final String GREEN_BORDER = hex("-c-green-border");
  public static final String FEATURE = hex("-c-feature");
  public static final String TOGGLE_SELECTED = hex("-c-toggle-selected");
  public static final String TOGGLE_SELECTED_BORDER = hex("-c-toggle-selected-border");
  public static final String UPDATE = hex("-c-update");
  public static final String OUTDATED = hex("-c-outdated");
  public static final String ORANGE = hex("-c-orange");
  public static final String ORANGE_BRIGHT = hex("-c-orange-bright");
  public static final String AMBER = hex("-c-amber");
  public static final String ORANGE_NAMED = hex("-c-orange-named");
  public static final String YELLOW_LIGHT = hex("-c-yellow-light");
  public static final String FOLDER = hex("-c-folder");
  public static final String CYAN = hex("-c-cyan");
  public static final String TEAL = hex("-c-teal");
  public static final String WOL = hex("-c-wol");
  public static final String WOL_HOVER = hex("-c-wol-hover");
  public static final String SELECTION_HANDLE = hex("-c-selection-handle");
  public static final String DEBUG = hex("-c-debug");

  // Translucent row tints
  public static final String MEDIA_BUTTON_HOVER = hex("-c-media-button-hover");
  public static final String ROW_HOVER_FAINT = hex("-c-row-hover-faint");
  public static final String ROW_ODD_TINT = hex("-c-row-odd-tint");
  public static final String ROW_EVEN_TINT = hex("-c-row-even-tint");

  // VPS link and format badges
  public static final String VPS_BLUE = hex("-c-vps-blue");
  public static final String VPS_ORANGE = hex("-c-vps-orange");
  public static final String VPS_RED = hex("-c-vps-red");
  public static final String VPS_GRAY = hex("-c-vps-gray");
  public static final String VPS_YOUTUBE = hex("-c-vps-youtube");

  // Script editor syntax highlighting
  public static final String SYNTAX_KEYWORD = hex("-c-syntax-keyword");
  public static final String SYNTAX_PAREN = hex("-c-syntax-paren");
  public static final String SYNTAX_BRACKET = hex("-c-syntax-bracket");
  public static final String SYNTAX_STRING = hex("-c-syntax-string");
  public static final String EDITOR_CARET_LINE = hex("-c-editor-caret-line");

  // Aliases
  public static final String ROW_SELECTED = hex("-c-row-selected");
  public static final String RARITY_UNCOMMON = hex("-c-rarity-uncommon");
  public static final String RARITY_LEGENDARY = hex("-c-rarity-legendary");
  public static final String MEDAL_SILVER_DARK = hex("-c-medal-silver-dark");

  private ThemeColors() {
  }

  /**
   * @param token a -c-* token of stylesheet-colors.css, e.g. "-c-error"
   * @return the hex value of the token, aliases are resolved
   */
  public static String hex(String token) {
    String value = TOKENS.get(token);
    int depth = 0;
    while (value != null && value.startsWith("-c-") && depth++ < 10) {
      value = TOKENS.get(value);
    }
    if (value == null || !HEX_PATTERN.matcher(value).matches()) {
      throw new IllegalStateException("Color token " + token + " is not defined as hex color in " + RESOURCE);
    }
    return value;
  }

  /**
   * @param color a hex color, usually one of the constants
   */
  public static Color color(String color) {
    return Color.web(color);
  }

  /**
   * Sets the text color of a control, removing a c-text-* class of the FXML file first:
   * stylesheet classes would override a value set from code.
   */
  public static void setTextFill(Labeled labeled, String color) {
    labeled.getStyleClass().removeIf(c -> c.startsWith("c-text-"));
    labeled.setTextFill(Color.web(color));
  }

  /**
   * Sets the icon color, removing a c-icon-* class of the FXML file first:
   * stylesheet classes would override a value set from code.
   */
  public static void setIconColor(FontIcon icon, String color) {
    icon.getStyleClass().removeIf(c -> c.startsWith("c-icon-"));
    icon.setIconColor(Color.web(color));
  }

  public static Map<String, String> getTokens() {
    return Collections.unmodifiableMap(TOKENS);
  }

  private static Map<String, String> load() {
    Map<String, String> result = new HashMap<>();
    try (InputStream in = ThemeColors.class.getResourceAsStream(RESOURCE)) {
      if (in == null) {
        throw new IllegalStateException("Missing color stylesheet " + RESOURCE);
      }
      String css = new String(in.readAllBytes(), StandardCharsets.UTF_8).replaceAll("(?s)/\\*.*?\\*/", "");
      Matcher m = TOKEN_PATTERN.matcher(css);
      while (m.find()) {
        result.putIfAbsent(m.group(1), m.group(2).trim());
      }
    }
    catch (IOException e) {
      LOG.error("Failed to read color stylesheet {}: {}", RESOURCE, e.getMessage(), e);
      throw new IllegalStateException(e);
    }
    return result;
  }
}
