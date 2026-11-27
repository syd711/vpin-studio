package de.mephisto.vpin.ui.util;

import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.kordamp.ikonli.javafx.FontIcon;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Builder for context menus in the Studio look (UI-STYLEGUIDE.md, "Context menus", styles in stylesheet-menu.css):
 * a caption naming the selection, items with a 16px muted mdi2 icon, sections divided by separators,
 * secondary actions grouped in sub menus and destructive actions in red at the end.
 */
public final class ContextMenus {

  public static final String ICON = "ctx-icon";
  public static final String ICON_OK = "ctx-icon-ok";
  public static final String DANGER = "ctx-danger";
  public static final String CAPTION = "ctx-caption-item";

  private ContextMenus() {
  }

  public static FontIcon icon(String iconLiteral) {
    FontIcon icon = new FontIcon(iconLiteral);
    icon.getStyleClass().add(ICON);
    return icon;
  }

  public static MenuItem item(String text, @Nullable String iconLiteral, Runnable action) {
    return item(text, iconLiteral, null, action);
  }

  public static MenuItem item(String text, @Nullable String iconLiteral, @Nullable KeyCombination accelerator, Runnable action) {
    MenuItem item = new MenuItem(text);
    if (iconLiteral != null) {
      item.setGraphic(icon(iconLiteral));
    }
    item.setAccelerator(accelerator);
    item.setOnAction(e -> action.run());
    return item;
  }

  /**
   * The primary action of the menu (launch), green icon.
   */
  public static MenuItem primary(String text, String iconLiteral, @Nullable KeyCombination accelerator, Runnable action) {
    MenuItem item = item(text, iconLiteral, accelerator, action);
    item.getGraphic().getStyleClass().add(ICON_OK);
    return item;
  }

  /**
   * A destructive action, red text and icon.
   */
  public static MenuItem danger(String text, String iconLiteral, @Nullable KeyCombination accelerator, Runnable action) {
    MenuItem item = item(text, iconLiteral, accelerator, action);
    item.getStyleClass().add(DANGER);
    return item;
  }

  /**
   * Sub menu, null entries are skipped and separators are cleaned up. Returns null when no item is left.
   */
  @Nullable
  public static Menu menu(String text, @Nullable String iconLiteral, MenuItem... items) {
    Menu menu = new Menu(text);
    if (iconLiteral != null) {
      menu.setGraphic(icon(iconLiteral));
    }
    Arrays.stream(items).filter(Objects::nonNull).forEach(i -> menu.getItems().add(i));
    cleanSeparators(menu.getItems());
    return menu.getItems().isEmpty() ? null : menu;
  }

  /**
   * Header row of the menu naming the selection (12px bold caption), not selectable.
   */
  public static MenuItem caption(String text) {
    MenuItem item = new MenuItem(text);
    item.getStyleClass().add(CAPTION);
    item.setDisable(true);
    return item;
  }

  /**
   * Appends a section: the non null items, preceded by a separator when the menu already has entries.
   */
  public static void section(ContextMenu menu, MenuItem... items) {
    section(menu.getItems(), items);
  }

  public static void section(List<MenuItem> target, MenuItem... items) {
    List<MenuItem> entries = Arrays.stream(items).filter(Objects::nonNull).toList();
    if (entries.isEmpty()) {
      return;
    }
    if (!target.isEmpty() && !(target.getLast() instanceof SeparatorMenuItem) && !target.getLast().getStyleClass().contains(CAPTION)) {
      target.add(new SeparatorMenuItem());
    }
    target.addAll(entries);
  }

  /**
   * Removes leading, trailing and doubled separators.
   */
  public static void cleanSeparators(List<MenuItem> items) {
    List<MenuItem> copy = new ArrayList<>(items);
    List<MenuItem> result = new ArrayList<>();
    for (MenuItem item : copy) {
      boolean separator = item instanceof SeparatorMenuItem;
      if (separator && (result.isEmpty() || result.getLast() instanceof SeparatorMenuItem)) {
        continue;
      }
      result.add(item);
    }
    while (!result.isEmpty() && result.getLast() instanceof SeparatorMenuItem) {
      result.removeLast();
    }
    if (result.size() != copy.size()) {
      items.clear();
      items.addAll(result);
    }
  }

  /**
   * Fires the enabled item (sub menus included) whose accelerator matches the key event, the accelerators of a
   * context menu are not registered in the scene. Returns true when an item was fired.
   */
  public static boolean fireAccelerator(@Nullable ContextMenu menu, KeyEvent event) {
    if (menu == null || event.isConsumed()) {
      return false;
    }
    MenuItem item = findAccelerator(new ArrayList<>(menu.getItems()), event);
    if (item != null && item.getOnAction() != null) {
      item.getOnAction().handle(new ActionEvent(event.getSource(), event.getTarget()));
      event.consume();
      return true;
    }
    return false;
  }

  @Nullable
  private static MenuItem findAccelerator(List<MenuItem> items, KeyEvent event) {
    for (MenuItem item : items) {
      if (item.isDisable() || !item.isVisible()) {
        continue;
      }
      if (item instanceof Menu menu) {
        MenuItem match = findAccelerator(new ArrayList<>(menu.getItems()), event);
        if (match != null) {
          return match;
        }
      }
      else if (item.getAccelerator() != null && item.getAccelerator().match(event)) {
        return item;
      }
    }
    return null;
  }
}
