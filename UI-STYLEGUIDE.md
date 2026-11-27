# VPin Studio UI Style Guide

Visual rules for the redesigned views (Achievements, System Manager). New or reworked screens follow these rules
so that colors, sizes, badges and buttons stay the same everywhere. **Reuse an existing token or class before
adding a new one**; a new color or font size needs a reason that none of the existing ones covers.

- CSS classes: `sm-*` in `vpin-studio-ui/src/main/resources/de/mephisto/vpin/ui/`.
  `stylesheet.css` only `@import`s one file per widget type (`stylesheet-textfield.css`, `stylesheet-tabpane.css`, …);
  put new rules into the matching file. Control classes go into the control's file (`sm-button*` → `-button`,
  `sm-text-field`/`sm-log-text`/`sm-script-text` → `-textfield`, `sm-log`/`sm-keyword-list` → `-listview`,
  `sm-link` → `-hyperlink`, `sm-map`/`sm-transparent-scroll` → `-scrollpane`); the generic building blocks have their
  own files, imported last: `-panels` (tile, panel, inset, divider, footer, list row), `-typography` (title, text,
  hint, small, caption, version), `-pills` (pills, chips, filter pills), `-alerts` (alerts, field errors, banners),
  `-toolbars` (action bar, main / floating toolbar). The import order is the cascade order, keep containers (combo box, date
  picker, spinner) after the controls they contain
- **Colors are only defined in `stylesheet-colors.css`** as `-c-*` tokens (`-c-error`, `-c-tile`, …). CSS and inline
  styles use the tokens, FXML uses the classes `c-text-<token>`, `c-icon-<token>` and `c-fill-<token>` instead of the
  `textFill` / `iconColor` / `fill` attributes, Java uses the `ThemeColors` constants (read from that file) and
  `ThemeColors.setTextFill()` / `setIconColor()` for FXML nodes. A new color needs a token there first;
  `ThemeColorsTest` fails on hard coded colors in the UI module.
- Java builders and color constants: `vpin-studio-ui/.../ui/achievements/AchievementsUI.java`
- Prefer `styleClass="sm-…"` over inline `style="…"`. Inline styles are only fine for values computed at
  runtime (a category color, a progress fraction) and must still use the tokens below.

## 1. Colors

The token names below are the `-c-*` tokens of `stylesheet-colors.css` (e.g. `inset` is `-c-inset`, primary text is
`-c-white`, body light is `-c-text-light`, the pill / banner backgrounds are `-c-ok-bg`, `-c-ok-banner`, …).

### Surfaces (dark → light)

| Token | Hex | Use |
|-------|-----|-----|
| inset | `#161616` | Inset boxes inside a tile (`sm-inset`), logs, text areas, progress track (`AchievementsUI.TRACK`), monitor map |
| footer | `#1C1C1C` | Pinned action footer of a panel (`sm-footer`), log group rows |
| panel | `#1A1A1A` | Page background, panels of a split view (`sm-panel`), chips, selected detail boxes |
| tile | `#232323` | Cards / tiles on the page background (`sm-tile`, `AchievementsUI.CARD`) |
| button | `#FFFFFF08` (3% white) | Buttons, toggle buttons, combo boxes: translucent, `#2A2A2A` on a tile, `#232323` on the page |
| button border | `#FFFFFF12` (7% white) | Their 1px border; hover = `#FFFFFF12` fill + `#FFFFFF1F` border |
| control | `#333333` | List-row hover, neutral pill background |
| control hover | `#3D3D3D` | Hover of other controls (tabs) |
| selected | `#3A3A3A` | Selected list row, borders of panels / dividers |
| border | `#444444` | Chip and text-field borders |

Table-like row lists in the Achievements view use `ROW_EVEN #283335` / `ROW_ODD #303733` / `ROW_SELECTED #333344`
with a 3px `#8C8CE0` left border for the selected row (`AchievementsUI.row()`). Do not mix this striped style with
the `sm-list-row` style inside one view.

Every `TableView` / `TreeTableView` gets the same flat look from `stylesheet-tableview.css` without any class:
one `-c-table-background` surface (= `-c-tile #232323`) for the rows under a darker `-c-table-header-background #121212` header row (separated by a 1px `-c-table-header-border #404040` line),
uppercase 11px bold `#B8B8B8` header labels (white on hover) (uppercased by `StudioTableViewSkin` / `StudioTreeTableViewSkin`, set via
`-fx-skin`; a short 1px `-c-table-header-border` divider on the right edge of each header cell marks the resize handle, `-c-border-strong` on hover) with the sort arrow in the accent color `-c-warn`, 42px rows separated by a
1px `-c-table-divider` line (no stripes), hover `-c-table-hover`, selected row `-c-table-selected` amber tint with a 3px
`-c-warn` left bar, check boxes in cells (selection column) in the accent color.
Don't set a background on the table in FXML. `sm-table` = roomy variant (60px rows, 12px padding); column classes
`sm-table-bold`, `sm-table-mono`, `sm-table-center`.

### Text

| Token | Hex | Use |
|-------|-----|-----|
| primary | `white` | Titles, values, body text (`sm-text`, `sm-title`) |
| body light | `#E0E0E0` | Log and script text, monospace lists |
| muted | `#BDBDBD` | Hints, secondary text, inactive tab labels, neutral icons (`sm-hint`, `AchievementsUI.MUTED`) |
| subtle | `#9A9A9A` | Small labels, captions, timestamps (`sm-small`, `sm-caption`) |
| disabled | `#7A7A7A` | Disabled / hidden entries, separator arrows (`AchievementsUI.DISABLED`) |
| link | `#7FB2FF` | Hyperlinks (`sm-link`, `AchievementsUI.BLUE`) |

### Accents and status — the only signal colors

| Meaning | Text / icon | Pill background | Banner background | Constant |
|---------|-------------|-----------------|-------------------|----------|
| OK / success / up to date | `#5CC46A` | `#1F3A26` | `#17231A` | `GREEN` |
| Warning / update available / primary action | `#DD9646` | `#3D2B17` | `#241D14` | `ORANGE` |
| Error / missing / invalid | `#FF6B70` | `#3A1C1F` | `#34191B` (border `#6E2B30`) | `RED` |
| Info / selection / frontend | `#8C8CE0` | – | `#1C1D2B` (border `#333344`) | `ACCENT` |
| Unknown / neutral | `#BDBDBD` | `#333333` | – | `MUTED` |

Rules:
- Orange `#DD9646` is the brand accent: the primary button, the selected tab underline, level / prestige.
- Never introduce another red, green or orange shade. Hover shades are the only variants (`#E8A55C` for the accent button).
- Dedicated palettes exist **only** for: rarity (`RARITY_COLORS`: Common `#EDEDED`, Uncommon `#5CC46A`, Rare `#5AA9FF`,
  Epic `#C77DFF`, Legendary `#DD9646`), badge categories (`BadgeMedal.CATEGORY_COLORS`) and the gold/silver/bronze
  rank medals (`AchievementsUI.rank()`). Reuse those maps, don't copy their values.
- Every error message box is an `sm-alert` (see section 5a). Never use a filled red background (`#FF3333`, the removed
  `error-container` / `error-message` / `error-title` classes or the `#validationError` / `#validationContainer` id styles).

## 2. Typography

Default font size is **14px**. Allowed sizes:

| Size | Weight | Use | Class / helper |
|------|--------|-----|----------------|
| 26px | bold, `Monospaced` | Big numbers in KPI tiles | `AchievementsUI.big()` |
| 20px | bold | Display name of a detail header (selected table, level name) | inline |
| 16px | bold | Tile / panel titles, names of a selected item | `sm-title`, `AchievementsUI.headline()` |
| 14px | regular / bold | Body text, values, buttons, tab labels; `Consolas` for scripts and keyword lists (`sm-script-text`, `sm-keyword-list`) | `sm-text`, `sm-hint`, `AchievementsUI.text()/bold()/muted()` |
| 12px | regular / bold | Captions, small labels, pills, chips, small buttons | `sm-small`, `sm-caption`, `sm-pill`, `sm-chip`, `sm-button-small` |

Don't use 11, 13, 15, 17 or 18px. In particular, labels are never 13px; use 14px (or 12px for captions). Section captions above form groups are `sm-caption` (12px bold `#9A9A9A`),
field labels above inputs are `sm-small`.

## 3. Spacing & shape

- Tile padding `14 16 14 16`; split-view panel content padding `18`; inset padding `10 12 10 12`.
- Gaps: 14 between tiles / panels, 22 between sections in a panel, 12 inside a section, 6–8 between label and field and between buttons.
- Radius: 6 for tiles, panels, alerts; 4 for insets, buttons, text fields, list rows; fully round (≥ 11) for pills and chips.
- Icons: Ikonli `mdi2`, 18px in buttons, 16px in pills / small buttons / filter pills, 14px next to links.
  Icon color matches the text color of its control (white on `sm-button`, `#1A1305` on `sm-accent-button`).

## 4. Buttons

There are exactly these button types. Every `Button`, `MenuButton` and `SplitMenuButton` gets one of them in its
`styleClass` (icon-only buttons: `sm-button, sm-button-icon`; icon-only delete / stop: `sm-danger-button, sm-button-icon`).
As a safety net, a button without any class (e.g. created in Java) already gets the `sm-button` colors from the
global `.button` / `.menu-button` / `.split-menu-button` rules at the top of `stylesheet-button.css` — but not its 14px font
and 30px height, so always set the class. `sm-button` on a MenuButton / SplitMenuButton works too (padding moves to its parts).

| Class | Look | Use |
|-------|------|-----|
| `sm-button` | `-c-button` fill, `-c-button-border` border, white 14px, padding `5 12` | Default action, also every button in an `sm-actionbar` (styled automatically) |
| `sm-button, sm-button-icon` | same, square padding `5 7` | Icon-only actions (refresh, copy) next to inputs |
| `sm-collapsible-button` | default button look, 10px font, default padding (no fixed height) | Icon actions in a collapsible header, optionally with `folder-component` / `external-component` / `link-component` |
| `sm-button, sm-button-small` | same look, 12px, padding `3 9` | Secondary inline actions next to small text / pills, e.g. "Set as installed", "Reset version", "Ignore version" |
| `sm-accent-button` | orange fill `#DD9646`, dark text `#1A1305`, bold, padding `5 14` | The **one** primary action of a panel (install, save) — at most one per panel, usually in the `sm-footer` |
| `submit-button` | same as `sm-accent-button` | Primary button of a dialog footer (OK, Save, Save & close); also the commons alert / confirmation dialogs |
| `default-button` | same as `sm-button` | Secondary buttons of a dialog footer (Cancel, alternative save actions) |
| `sm-simulate-button` / `sm-install-button` | the original System Manager component tab look: neutral button with padding `5 12` / yellow `-c-warn` fill, dark bold text, no border, padding `8 14`; no fixed height | Only the "Simulate" and "Install <version>" buttons of the System Manager component tabs |
| `sm-danger-button` | `#3A1C1F` fill, `#6E2B30` border (`#FF6B70` on hover, fill `#4A2326`), `#FF6B70` bold 14px text and icons, padding `5 12` | Destructive actions (restart server, shutdown system, delete cabinet) and buttons flagging a problem (asset filter with excluded assets). Never a filled bright red. |
| `sm-button, folder-component` | `#CC9933` fill (yellow), white text and icon | Opens a **folder** in the file explorer, icon `mdi2f-folder-open` |
| `sm-button, external-component` | `#338800` fill (green), white text and icon | Starts a **system program** (setup exe, config tool), icon `mdi2o-open-in-new` |
| `sm-button, link-component` | `#3399FF` fill (blue), white text and icon | Opens a **website or file** (ini editor), files use icon `mdi2c-card-text-outline` |
| `sm-alert-button` / `sm-alert-close` | transparent with red border | Only inside `sm-alert` |
| `sm-filter-pill` (ToggleButton) | round, 14px, faint `#FFFFFF0A` outline; hover `-c-button`, selected = `-c-button-hover` fill + border, bold white label | Status filters above a list (built with `FilterPills`: dot + label + `#9A9A9A` count), option toggles |
| `sm-chip` | round `#222222` with border, 12px | Small clickable tags (e.g. screen shortcuts) |
| `sm-link` (Hyperlink) | `#7FB2FF`, underline on hover | External links, with `mdi2o-open-in-new` 14px icon |
| `sm-section-header` | transparent, hand cursor | Show / hide toggle of a section inside a card (chevron + `sm-text` + `sm-small` state); whole collapsible panels use the common look of section 6 |

The three "open" colors mean the same everywhere (tables sidebar uses the plain classes). Large variant = icon (18px) + text,
in an `sm-actionbar` the `sm-button` class is implied; small variant = `sm-button, sm-button-icon, <color class>` with a 16px icon,
e.g. the open-folder buttons next to the emulator folder fields. Choosing a folder (`…` / "Change folder") stays a plain `sm-button`.

Borderless text buttons are not used any more (the former `sm-ghost-button` was removed).

**Control height:** buttons and combo boxes share one height so they line up in a row: `sm-button` (also
`sm-button-icon`), `sm-accent-button`, `sm-danger-button`, the buttons of an `sm-actionbar` / `floating-toolbar` and every
`.combo-box-base` (ComboBox, DatePicker, ColorPicker) are **30px** high; `sm-button-small` is 24px. The clear button over a
search field (`ghost-icon-button`) keeps its computed size. Change all of them together.

### Text inputs

Every `TextField`, `PasswordField` and `TextArea` is dark — styled globally via `.text-input` in `stylesheet-textfield.css`,
no class needed: `#161616` fill, `#444444` 1px border, radius 4, white 14px text, `#7A7A7A` prompt text,
`#7A7A7A` border when focused, `#333366` selection. Don't override it with inline white backgrounds.
`sm-text-field` is the same look and stays valid for explicitness. Full-width log / script areas use
`sm-log-text` / `sm-script-text`.

`ComboBox`, `DatePicker` and `ColorPicker` (`.combo-box-base`) use the button look: `-c-button` fill, `-c-button-border` border,
radius 4, white 14px text, `#BDBDBD` arrow (white on hover), `-c-button-hover` fill + border on hover, `#7A7A7A` border when focused or open.
The popup list is `#161616` with white 14px cells (padding `4 8`, regular weight also when selected) and `#333366` for
hover / selection, without the focus border of a regular `ListView`. Editable combos have no extra
inner box — the editor is transparent inside the combo's border.

`Spinner` uses the same box with a transparent editor; the arrow buttons are transparent with `#BDBDBD` arrows
(white arrow + `#333333` fill on hover). `CheckBox` boxes are `#161616` with a `#444444` border (radius 3,
`#7A7A7A` on hover/focus) and a white check mark; `sm-accent-checkbox` = orange `#DD9646` box with a dark mark when selected (the "Auto-save" check box of dialog headers); `RadioButton` circles look the same with a white dot and have
the same outer size as the check box (~21px at 14px font) — keep both in sync when changing paddings.
`ScrollBar`: 10px, no arrow buttons, transparent track, rounded `#444444` thumb (`#5A5A5A` hover, `#7A7A7A` drag).
`ToggleButton` has the `sm-button` look (`-c-button`, `-c-button-border`, `-c-button-hover`); selected = `#333366` fill
with an `#8C8CE0` border. Add `sm-button` for the 30px height. Every on/off button uses this one "on" look (VR mode,
cabinet monitor, asset manager view, filter button): prefer a ToggleButton, a plain Button showing an "on" state adds
`toggle-button-selected` (same look). No other "on" colors; only a ToggleButton flagging a problem (maintenance mode) adds `sm-danger-button`. `sm-filter-pill` toggles keep their round outline look.
`Slider`: `#161616` track with `#444444` border, round `#BDBDBD` thumb (white on hover/drag, `#8C8CE0` border when
focused), tick marks `#444444`, tick labels 12px `#9A9A9A`.
`ListView`: `#161616` box with `#444444` border (radius 4, `#7A7A7A` when focused), transparent white 14px cells with
padding `4 8`, `#262626` hover, `#3A3A3A` + bold for the selected row, `#7A7A7A` placeholder text. No striping.
`TreeView` looks the same as `ListView`, with `#BDBDBD` disclosure arrows (white on hover).
`sm-log` / `sm-keyword-list` refine this look; the striped `AchievementsUI.row()` lists are built from VBoxes.
`ProgressBar`: `#161616` track with `#444444` border (radius 4), flat `#8C8CE0` bar inset by 2px.
`AchievementsUI.progress()` remains the borderless variant for colored progress inside tiles. All of these are global — no style class needed; disabled
controls are dimmed to 60 % opacity.

## 5. Badges, pills and status text

| Element | Class / helper | Rule |
|---------|----------------|------|
| Status pill | `sm-pill` + `sm-pill-ok` / `-warn` / `-error` / `-unknown` / `-info` (link blue on `-c-info-border`, e.g. "Custom" in the designer list) | 12px bold, round, colored text on the tinted background of section 1 |
| Large status pill | add `sm-pill-large` | 14px, page-level status (Overview status row) with a 16px icon |
| Version text | `sm-version-ok` / `sm-version-warn` | Colors a version label green / orange, nothing else |
| Banner | `sm-banner` + `sm-banner-ok` / `-warn` / `-error` / `-info` | Full-width result line on top of a log / list (icon + text) |
| Outline chip | `AchievementsUI.chip(text, color)`, `rarityChip()` | 12px bold, colored border + text, for rarity and categories |
| Status dot | `AchievementsUI.dot(color, r)` | 4px radius in list rows (enabled = green, disabled = `#7A7A7A`) |
| Count badge | filled round label, 12px bold, padding `0 5` | "+N" counters, e.g. stacked screens on the monitor map |

Always pick the pill variant by meaning (OK/warn/error/unknown); never color a pill per component or per item.

## 5a. Error messages

All error containers in the Studio use the Screen Manager layout (`screens/tab-screens.fxml`,
`emulators/tab-emulators.fxml`). Don't build another variant.

**Error box** (`sm-alert`) — validation errors of a table / sidebar section / view, failed checks in dialogs,
configuration conflicts:

```xml
<HBox fx:id="errorBox" spacing="12.0" styleClass="sm-alert">          <!-- VBox/BorderPane: wrap the same HBox -->
   <children>
      <StackPane maxHeight="24.0" maxWidth="24.0" minHeight="24.0" minWidth="24.0" styleClass="sm-alert-icon">
         <children>
            <FontIcon iconColor="WHITE" iconLiteral="mdi2e-exclamation-thick" iconSize="14" />
         </children>
      </StackPane>
      <VBox spacing="2.0" HBox.hgrow="ALWAYS">
         <children>
            <Label fx:id="errorTitle" styleClass="sm-alert-title" wrapText="true" />
            <Label fx:id="errorText" styleClass="sm-alert-text" wrapText="true" />
         </children>
      </VBox>
      <HBox alignment="TOP_RIGHT" spacing="6.0"><!-- optional actions --></HBox>
   </children>
   <padding><Insets bottom="10.0" left="12.0" right="12.0" top="10.0" /></padding>
</HBox>
```

- Colors: `#34191B` fill, `#6E2B30` border, radius 6; round `#E5484D` icon (24px, 14px `mdi2e-exclamation-thick`);
  title 14px bold white (`sm-alert-title`), text 14px `#E6C3C5` (`sm-alert-text`). Several messages = several
  `sm-alert-text` labels (or bullet labels as in `tab-overview.fxml`). A box with only one line may skip the title.
- Actions sit top right: dismiss = `sm-alert-close` with `mdi2c-close` 16px `#E6C3C5` and a tooltip; text or icon
  actions ("Dismiss All", validator settings cog) = `sm-alert-button` (icons `#F3D6D7`, 16px). No hyperlinks.
- Page-level risk warnings (`tab-overview.fxml`) are the same box with a 30px icon and padding `14 12 14 16`.
- The box must line up with the content around it: give it the same left / right inset as its siblings (in dialogs
  usually `VBox.margin` / `BorderPane.margin` `left="12" right="12"`) — never let it run edge to edge while the
  rest of the dialog is indented. Spacing to the following content: 12 (sidebars / dialogs), 14 above split views.
- Examples: table sidebars (`scene-tables-sidebar-*.fxml`), table overview / backglass manager validation bar,
  `dialog-table-delete.fxml`, `dialog-new-emulator.fxml`, `preference-pinvol.fxml`.

**Inline field error** (`sm-field-error`) — a short message directly next to / below a single form field
(invalid folder, invalid URL): Label, 14px `#FF6B70`, with a `mdi2a-alert-circle-outline` 16px `#FF6B70` graphic,
`wrapText="true"`, no background.

**Error tag** in a list or table cell: `sm-pill, sm-pill-error`. Result line above a log: `sm-banner-error`.

## 6. Tiles and layout building blocks

### Collapsible panels (`stylesheet-titledpane.css`)
Every `Accordion` / `TitledPane` gets this look automatically:
- **Collapsed** = a transparent row with a 1px `-c-panel-border` divider below, hover `-c-row-hover`, thin `-c-subtle`
  chevron on the left (points right), hand cursor.
- **Expanded** = a `-c-row-hover` card (1px `-c-selected` border, radius 8, 4px air above and below), chevron points
  down, the header becomes a darker `-c-collapsible-header` band (hover `-c-collapsible-header-hover`, radius
  `7 7 0 0`) with a 1px `-c-selected` divider below, so it stands apart from the transparent toolbars of the content,
  and its leading `c-icon-muted` icon turns orange (`-c-warn`). The content is transparent (scroll panes and `accordion-container` inside included), so
  the card color shows through.
- **Header** = a Region in the `graphic`, never the plain `text` attribute (the text can not be made bold):
  `HBox` (spacing 10, `CENTER_LEFT`) with an 18px `c-icon-muted` icon + `Label` `sm-collapsible-title` (14px bold white),
  then an `HBox.hgrow` spacer and the actions right-aligned (check boxes, icon buttons with `sm-collapsible-button`,
  10px font and default padding, optionally combined with `folder-component` / `external-component` / `link-component`).
  Header padding `6 12`.
  Call `CollapsibleHeaders.fill(accordion)` (or `fill(titledPane)`) in the controller so the header spans the pane width.
- The `Accordion` gets an 8px padding inside its tile; the sidebar around it is an `sm-tile, sm-tile-bordered`. A fixed
  content width grows the sidebar by 18px (8 + 8 padding, 1 + 1 border), e.g. tables sidebar 620px for 600px panels.
- Collapsibles built in Java (`tables/vps/VpsSection`) use the same look: VBox `sm-collapsible` with the `:expanded`
  pseudo class, header HBox `sm-collapsible-header` (padding `6 12`) + `sm-collapsible-title` + 16px `c-icon-subtle`
  chevron. Nested in an expanded card, an expanded collapsible turns `-c-footer` to stay visible.
- Designer only (`designer-layers`): eye / lock header buttons, struck-through hidden layers, 12px `-c-subtle` field labels.

### Achievements view
- **KPI tile** (`overview/tile-unlocked.fxml`, `tile-cloud-rank.fxml`): `#2A2A2A` card, 16px title, 26px monospace number,
  14px colored delta line below (green for positive progress).
- **Level tile** (`overview/tile-level.fxml`, `badges/tile-level.fxml`): `levelCircle()` with orange ring, 20px level name,
  14px orange prestige text, `progress()` bar to the next level.
- **List tile** (`tile-recent-unlocks`, `tile-latest-scores`, `tile-ranking`, `tables/tile-table-list`): headline + striped
  `AchievementsUI.row()` rows, rank medals via `rank()`, avatars via `initials()`. Exception: the cabinet ranking of
  `tile-ranking` embeds the commons `widget-player-rank` table, switched to the flat Studio table look with
  `WidgetPlayerRankController#useStudioStyle()` (the overlays keep the big widget look); the cloud top 5 is a plain
  `TableView` in the same look (own entry = selected row, appended when outside the top 5); Cabinet / Cloud is an
  `sm-segmented` toggle.
- **Badge grid** (`badges/tile-badges.fxml`, `badge-group.fxml`): `medal()` per badge, selected badge shown in a `#222222`
  detail box with an `#8C8CE0` left border and a 16px bold name.
- **Progress row** (`tile-closest.fxml`, `leaderboards/tile-table.fxml`): label, 12px `#9A9A9A` caption, flat `progress()` bar
  on `#161616`.
- **Score tile** (`players/widget-highscore.fxml`): `sm-panel` with the table background image (cover) under a
  `sm-score-tile-shade`; 72px wheel, `sm-title` table name + `sm-hint` "Manufacturer · Year", rank pill
  (`sm-pill` + `sm-pill-rank-1/2/3` medal colors, `sm-pill-unknown` otherwise) + `sm-text` player + `sm-hint`
  relative date (full date as tooltip), score font right-aligned.
- Cards built in code use `AchievementsUI.card()` (VBox, spacing 8, padding 14/16, `#2A2A2A`, radius 6).

### System Manager
- **Component card** (`component-short-summary-panel.fxml`): `sm-tile`; header row = `sm-title` + status pill right-aligned;
  `sm-inset` grid with `sm-hint` keys and `sm-text` values (Installed / Latest); flexible spacer; footer row = `sm-link`
  (left, grows) + `sm-button` "Manage ›" (chevron as right graphic). Cards sit in a 3-column `GridPane`, gap 14.
- **Status row** (`tab-overview.fxml`): large pill with icon + `sm-small` "last checked" + spacer + `sm-button` action.
- **Risk alert** (`tab-overview.fxml`): `sm-alert` HBox, round red `sm-alert-icon` (30px), title + bullet texts,
  close button top right, `sm-alert-button`s bottom right. Dismissable, one per view at most.
- **Split view** (`component-update-panel.fxml`, `screens/tab-screens.fxml`, `emulators/tab-emulators.fxml`): HBox of
  `sm-panel`s, gap 14, page padding `12 16 14 16`. Fixed-width side columns (290px list, 390–430px settings), the
  right-most panel grows. The Screens and Emulators views use `sm-tile, sm-tile-bordered` (lighter `-c-tile` background, `-c-panel-border` hairline) instead of
  `sm-panel`s, with the same layout.
- **Panel header**: HBox with `sm-divider` (bottom border), padding `10 12 10 14`: `sm-title` + `sm-small` meta +
  spacer + filters / search (`sm-text-field`) / icon buttons.
- **Panel footer** (`sm-footer`): pinned below a `sm-transparent-scroll` ScrollPane, holds the buttons
  (`sm-button` + one `sm-accent-button`, equal width via `HBox.hgrow`) and an info-icon hint row.
- **Selectable list** (Screens / Emulators lists): rows built in code with `sm-list-row`, selection via
  `sm-list-row-selected`; row = status dot or icon + 14px name (bold when selected, `#7A7A7A` when disabled) + pill.
- **Version section** (`component-summary-panel.fxml`): `sm-caption` heading, `sm-inset` grid "installed → latest" with a
  `#7A7A7A` arrow, status pill + `sm-small` timestamp, then the `sm-button-small` actions in a FlowPane (gap 6).
- **Structured log** (`sm-log`, `sm-log-row`, `sm-log-group`, `sm-banner-*`): ListView on `#161616`, collapsible group
  rows on `#1C1C1C`, filter pills + search in the panel header; plain text logs use `sm-log-text`.
- **Monitor map** (`sm-map`): `#161616` canvas, monitors with `#4A4A4A` border, screens colored by source
  (frontend = `#8C8CE0`), `sm-chip`s for quick selection, legend below.
- **Inspector** (Screens): `sm-panel` VBox, spacing 16, sections with `sm-caption` headings.
- **Tabs** (`stylesheet-tabpane.css`): every TabPane in the Studio gets `styleClass="primary-tabs"` — underlined tabs,
  14px bold `#BDBDBD` labels, white when selected/hovered, 2px orange underline.
  Section tab panes (Competitions, Achievements, Designer) sit in a `sm-tile, sm-tile-bordered` wrapper (1px
  `-c-panel-border` hairline) and get `primary-tabs, tile-tabs`: text-only tabs (no icons), header padding `8 16 0 16`,
  a full-width `-c-panel-border` divider and `14 16 14 16` page padding. Tabs without pages (Designer) add
  `tile-tabs-bare` and the wrapper lays out the content itself. `sm-tile`s inside the wrapper are drawn as panels
  (`-c-panel` + hairline) so cards stay visible on the tile background; tiles on an `sm-panel` keep the tile look.
  The Tables view selects its pages through the navigation: its TabPane adds `tile-tabs-headless` (no tab header) and
  the tile starts with a `sm-divider` header row (`sm-title` page name + settings / sidebar icon buttons).
  The only exception is the System Manager's root TabPane, which uses `parent-tabs`: flat folder tabs on a header strip
  one step lighter than the content, 5px top radius, the selected tab takes the content color (`#222222` page inside a
  subpanel) so it merges with the page. Strip/content colors come from `-sm-tab-strip` / `-sm-tab-content`.
- **Navigation sidebar** (`scene-navigation.fxml`, `nav-*` classes): 230px `#222222` panel (64px when collapsed,
  persisted in `LocalUISettings`), header with 40px round avatar + `sm-title` + `sm-small` system name,
  12px bold `#9A9A9A` section captions, items = 18px icon (`#BDBDBD`) + 14px label, `#333333` hover, selected =
  `#3D2B17` fill with orange icon and bold label; count badge in warn colors; the item list scrolls on small window
  heights, Collapse is pinned at the bottom (the preferences are opened from the toolbar).
  The preference navigations (`scene-preferences.fxml`, `mania/scene-mania-preferences.fxml`) use the same look:
  `pref-nav` ScrollPane panel (280px, like the sidebar but **without border**), `preference-section` captions, `preference-button`
  items without icons (`preference-button-selected`). The rules are scoped `.pref-nav .button.preference-button` so the
  global `.button` / `.scroll-pane` defaults (imported later) do not override them.
- **Toolbars**: every `ToolBar` gets `styleClass="floating-toolbar"` (flat `#222222`, no border, radius 6; `floating-toolbar-inverted`
  on a `#222222` background). Every toolbar that sits directly on a tile, in particular all toolbars of a `tile-tabs`
  section (Tables, Competitions, Achievements, Designer), also gets `floating-toolbar-transparent`: no strip, flush with
  the tab content, 12px gap below. `sm-actionbar-flush` / `-inset` rows use the same inset. Spacing is identical in all of
  them: 8px between items, vertical `Separator`s get 4px on each side from the CSS and carry no `prefWidth` / `prefHeight`
  in FXML; an `HBox` group inside a toolbar uses `spacing="8.0"`. Search field / combo groups are framed by separators
  the same way as in the table overview (filter, reload | search | emulator | playlist). This includes the toolbars in the table sidebar collapsibles (`floating-toolbar, floating-toolbar-transparent`, inset `8 12 4 12` on the card; `tables/scene-tables-sidebar-*.fxml`, no fixed
  `prefWidth` / `prefHeight`); the only exception is `sm-actionbar`.
- **Action bar** (`sm-actionbar`): top bar of a component tab and of the table overview; any Button and SplitMenuButton
  inside gets the `sm-button` look automatically. Destructive buttons add `sm-danger-button`, the clear button of a
  search field keeps `ghost-icon-button`.
- **View actions** (pages of the Tables section): no page header, the breadcrumb names the page. The gear (view
  preferences) and, while the details panel is hidden, the "show details panel" button sit at the right end of the first
  toolbar row behind a vertical `Separator` (`TablesController#attachViewActions`). The details panel starts with an
  `sm-divider` header: `sm-caption` "TABLE DETAILS", `sm-title` name of the selection, hide button right.

### Context menus (`stylesheet-menu.css`, `util/ContextMenus`)
- Every popup menu (context menus, MenuButton / SplitMenuButton drop-downs) gets the look globally: `-c-tile` popup
  with a 1px `-c-border` frame, radius 6, padding 4 and a `-c-shadow` drop shadow; rows padding `6 12 6 10`, radius 4,
  14px white labels, `-c-control` on hover / focus / open sub menu, disabled rows `-c-disabled`; shortcuts 12px
  `-c-subtle` on the right; separators are a 1px `-c-panel-border` line.
- Build context menus with `ContextMenus` (examples: `TableOverviewContextMenu`, `BackglassManagerController#refreshContextMenu`):
  `caption()` first (12px bold `-c-subtle`, the name of the selection or "N tables selected"), then `section()`s of
  `item()`s with a 16px muted mdi2 icon (`ctx-icon`, white on the focused row). Order: primary action (`primary()`,
  green icon, e.g. Launch), edit actions, online / navigation, files, maintenance, and the destructive action last
  (`danger()`, red text and icon, `-c-error-bg` on focus). Secondary or long lists go into a `menu()` sub menu
  (Upload, Maintenance, VPin Mania, Backglass Image / DMD Image) so the menu stays short.
- Rebuild the menu when it opens or the selection changes and disable, rather than hide, actions that don't apply.
  Accelerators of a context menu are not registered in the scene: forward key events with `ContextMenus.fireAccelerator()`
  (sub menus included, disabled items are skipped).

### Filter panel (`tables/scene-tables-overview-filter.fxml`, `backglassmanager/scene-directb2s-admin-filter.fxml`, `vps/scene-vps-tables-filter.fxml`)
- Slides in left of the table (`BaseFilterController`): an `sm-panel` of 286px, the table moves by `FILTER_WIDTH` 300px,
  so the 14px split view gap stays between panel and table.
- Header like the details panel: `sm-divider` row (padding `10 10 10 16`) with `sm-caption` "FILTER", `sm-button-small`
  "Reset All" (`resetBtn`, disabled while no filter is set) and an `sm-button-icon` `mdi2a-arrow-expand-left` hide button.
- Body: `sm-transparent-scroll` ScrollPane (`fitToWidth`), content padding `16 16 18 16`, 22 between sections. A
  section = `sm-caption` heading (upper-cased by `BaseFilterController`, the bundles keep title case) + its controls
  with spacing 8; check boxes are `sm-text` (`wrapText`), field labels above inputs `sm-small` (6 to the field), inputs and
  combos fill the width. Help icons next to a label are 14px `c-icon-muted` with a tooltip.
- Clickable VPS feature filters are `sm-pill`s with a hand cursor: `sm-pill-ok` when selected, `sm-pill-unknown` otherwise.

### VPS sidebar (`vps/scene-vps-tables-sidebar.fxml`)
- `sm-panel` with a `sm-divider` header: `sm-caption` "TABLE DETAILS", 20px `vps-sidebar-title` name, `sm-hint`
  "Manufacturer · Year · Type", `sm-button-icon` actions top right.
- Feature pills = `sm-pill-ok` (`sm-pill-unknown` when filtered out); facts in a two-column `sm-inset, vps-facts` grid
  of `vps-fact` cells (`sm-caption` key + `sm-text` value); IPDB row = `sm-inset` with `vps-tag` + `sm-link`.
- Download sections (`tables/vps/VpsSection`, also used by the tables sidebar and the VPS assets dialog): common
  collapsible (`sm-collapsible`, see "Collapsible panels"), header = 18px icon + title + `vps-count` + chevron, skeleton bars while loading; rows
  (`VpsEntry`) = orange Consolas `vps-entry-version` + authors (green `vps-entry-installed` link when installed) +
  `vps-link-pill` (source dot, abbreviation, arrow) and an `sm-small` "Updated …" line.
- VPS table version combos (`tables/vps/VpsTableVersionCell`): popup rows like a download entry (version + authors,
  `sm-small` date + `sm-pill-ok` features, comment); the button cell is `new VpsTableVersionCell(true)`, one line that
  fits the 30px combo. Never put a multi-line graphic into a combo's button cell.
- Table data manager (`tables/dialogs/dialog-table-data*.fxml`, `tables/panels/propper-renaming.fxml`): every tab is
  an `sm-transparent-scroll` page (padding `12 12 14 12`) of `sm-tile` cards (gap 14). A card = header row (`sm-title`,
  optional `sm-hint` lines below it, the card's action right-aligned: `sm-button`, destructive ones `sm-danger-button`)
  + a 3-column grid (hgap / vgap 8): 200px `sm-text` labels, growing field column, 180px column for 16px muted help
  icons and `sm-button-icon` link / folder buttons; `sm-caption` sub-headings and `sm-small` notes below a field.
  The auto-naming options are `sm-filter-pill` toggles, name previews / comment tips / screen check boxes sit in an
  `sm-inset`. Tabs with a page action instead of a card (Options, Statistics) start with an `sm-title` row + buttons
  right; the statistics are three equal-width `alx-tile-entry` KPI tiles in one row.

### Backglass manager (`backglassmanager/scene-directb2s-admin.fxml`, `scene-directb2s-sidebar.fxml`)
- Split view as `SplitPane` with `sm-split` (transparent, the divider is the 14px gap) holding two `sm-panel`s.
- List panel: two `sm-actionbar` HBox rows (filters / search / emulator / playlist, then the backglass actions + `sm-hint`
  count right-aligned), a row of `sm-filter-pill`s (`FilterPills`: All, Ready `-c-ok`, Needs attention `-c-error`,
  Disabled `-c-disabled` dot; counts over the filter panel result, the selected pill narrows the list), then the table.
- Sidebar: `sm-divider` header like the VPS sidebar (`sm-caption`, 20px `vps-sidebar-title`, `sm-hint` file name or a version
  combo, "Active" check box in an `sm-button` box + colored icon buttons); then the `sm-alert` of the selected backglass,
  two `sm-tile` image tiles (`sm-caption`, `sm-inset` image box with an empty state, `sm-small` resolution +
  `sm-button-icon` actions) and `primary-tabs` whose pages are `sm-tile` cards: `sm-divider` header with `sm-title`
  (+ `sm-small` hint right), body grid of `sm-text` labels and right-aligned 240px controls, or an `sm-inset` fact grid
  (`sm-hint` keys, `sm-text` values).

### Status footer (`vpxz/scene-vpxz.fxml`)
- Status of a page below its table: one `sm-inset` HBox row (12px gap above, padding `8 8 8 12`, spacing 8): muted 18px
  icon + `sm-text` subject + `sm-pill-ok` / `sm-pill-error` state, vertical `Separator`, `sm-hint` key + `sm-text` value,
  spacer, `sm-hint` item count and an `sm-button-icon` settings button. The state is a pill class, never an inline
  background color.

### Media recorder (`recorder/scene-recorder.fxml`, `recorder/panels/screen-recorder-panel.fxml`)
- Same split view as the backglass manager: table list with two `sm-actionbar` rows (filter / reload / search /
  emulator / playlist, then the screens menu, auto-select, table actions, `sm-danger-button` "Start" (recording stays red), play button,
  `sm-danger-button` stop and the `sm-hint` selection count right-aligned), then the table.
- Settings `sm-panel`: `sm-divider` header (`sm-caption` + `sm-hint`, preview refresh spinner right), then one `sm-tile`
  card per enabled screen: `sm-divider` header (muted monitor icon, `sm-title` screen name, `sm-small` resolution),
  option rows (150px `sm-text` label, help icon tooltips, `sm-button-small` "Edit command") and an `sm-caption`
  "PREVIEW" `sm-inset` that is hidden when the card is narrower than 560px. No enabled screen = `sm-inset` info row. The tab has no sidebar, so `TablesController` hides the sidebar toggle.

### Designer (`cards/scene-highscore-cards.fxml`, `cards/panels/template-editor.fxml`)
- Three borderless `sm-tile`s (like the Achievements and Players tiles) in an `sm-split`: table list (search field `designer-search` with a 18px `mdi2m-magnify` icon,
  filter / reload icon buttons, emulator combo with a muted "Emulator" prefix, two columns TABLE / TEMPLATE where custom
  designs show an `sm-pill-info` "Custom" pill, `sm-footer` with the table actions and the count), canvas tile and
  a 420px layers tile.
- Page header like the Achievements view: text-only `tile-tabs` (Highscore cards / Wheels) at the top of the wrapping
  tile, then one `floating-toolbar` with the `sm-segmented` Template / Custom toggle, the template combo and its icon actions,
  preview / folder buttons, `sm-button` "Generate this card" and `sm-accent-button` "Generate all" (both live in
  `template-editor.fxml` and are moved above the tiles by `HighscoreCardsController`).
- Canvas tile: an info row (`c-icon-info` icon + `sm-hint` text, `sm-small` resolution) says what the changes apply to;
  the design area below is `-c-black` (`designer-canvas`, background set in `TemplateEditorController`). The selected
  layer box is orange with an `sm-selection-caption` tag "Name · W × H" (`PositionResizer.setCaption`).
- **Segmented control** (`sm-segmented`): HBox of ToggleButtons of one ToggleGroup on an inset box, selected =
  `-c-control` fill, bold white (icons orange); always call `SegmentedToggles.keepSelected`.
- Layers panel: `sm-caption` "LAYERS" + `sm-hint`, then the `designer-layers` Accordion (common collapsible panels, see
  above), top layer first. Each header = 18px muted icon + bold `designer-layer-name` + bare `designer-layer-button` eye /
  lock icons (locked = orange `mdi2l-lock-outline`); hidden layers get `designer-layer-hidden` (struck through,
  `-c-disabled`). Field labels inside are 12px `-c-subtle`, values `sm-text`.
  Missing media = dashed `sm-inset, designer-empty-state` box with icon, `sm-hint` text and an `sm-button-small` action.

### Upload dialogs (`tables/dialogs/dialog-table-upload.fxml`)
- Numbered `sm-caption` sections ("1 · FILE", "2 · WHAT SHOULD HAPPEN?"). The file section is one `sm-tile, sm-tile-bordered`:
  `sm-small` field labels above emulator combo / file field (`sm-mono`) / `sm-button` "Choose file…"; a one-row
  `sm-inset, vps-facts` grid with what was found (table file, VPS match in `sm-version-ok` with a check icon, README with
  an `sm-button-small` that only shows when the archive has one); the bundled assets as `sm-pill-unknown` pills in an
  `sm-inset` row (hidden when there are none) with the `sm-button-small` asset filter; then `sm-accent-checkbox` options,
  each with an `sm-small, sm-checkbox-hint` description below.
- **Option cards** (`sm-option-card`): the choices of one radio group as equal-width cards (HBox, gap 14). Header row =
  `sm-accent-radio` + 18px `c-icon-muted, sm-option-card-icon` + `sm-title`; the content below gets `sm-option-card-body`
  (aligned with the title). Selected card = `sm-option-card-selected` (warn banner fill, orange border and icon), a click
  anywhere on the card selects it. Options that only apply to one choice live inside its card below an `sm-divider`;
  cards that don't apply are disabled as a whole. This is the only style for radio choice panels (table upload, .dif
  patch, backglass upload, asset source folder, recorder); the old blue `selection-panel` is gone.
- Footer = `sm-footer` with an `sm-hint` one-line summary of what will happen, `default-button` Cancel and a
  `submit-button` whose text names the chosen action ("Upload & replace").

### Bulk importer (`tables/dialogs/dialog-bulk-importer.fxml`)
- Four numbered `sm-tile, sm-tile-bordered` sections like the upload dialog: "1 · SOURCE" (emulator combo, `sm-mono`
  folder field, `sm-button` "Select Folder…", `sm-accent-checkbox`), "2 · MATCHING" and "3 · AFTER IMPORT" side by
  side (form grids with `sm-text` labels; mode / structure choices are `sm-segmented` toggles; an `sm-small, sm-mono`
  line previews the move target path), "4 · REVIEW" grows with the table.
- Review header: `sm-caption` + `FilterPills` (All, Good match `-c-ok`, Check `-c-warn`, Needs a table `-c-error`),
  then right-aligned `sm-hint` "Preview" + `sm-segmented` Off / S / M / L, `sm-button`s and an `sm-danger-button,
  sm-button-icon` remove button.
- Rows: preview thumbnail or `am-format-badge`; file = `sm-mono` name over an `sm-small` folder; match = 64px bar on
  `-c-inset` + bold mono percentage, both green (≥ 80 % or manual), orange (check) or red (no table).
- Footer = `sm-footer`: `sm-hint` "N files found · M selected", a `-c-warn` skip note, `default-button` Cancel and a
  `submit-button` "Import N files".

### Asset manager (`tables/dialogs/dialog-table-asset-manager.fxml`)
- Selection bar (`sm-divider` row): `sm-segmented` Tables / Playlists, prev / next `sm-button-icon`s around the table
  combo, "VPS assets" `sm-button`, `sm-chip, am-frontend-chip` with a `-c-info` dot right-aligned.
- Split view of `sm-panel`s (gap 14): Screens panel (`sm-caption` + `sm-small` "N of M have media", the screen map, a
  legend of `am-legend` swatches) and a column with the search and the installed panels.
- Screen tiles (`tables/dialogs/ScreenTile`, shared by the dialog, the asset view and the tables sidebar media panel):
  `am-screen-tile` = `-c-panel-border` fill (one step lighter than the tile it sits on, `-c-control` on hover) with a
  dashed `-c-border` outline; `am-screen-tile-filled` = `-c-ok-bg` fill, status line in `-c-ok`;
  `am-screen-tile-selected` = 2px orange border (`-c-warn-bg` fill when empty); `am-screen-tile-highlight` = 2px
  `-c-info` border (highscore card screen). Name + "Empty" / "N files" bottom left, one line below 64px tile height;
  content is clipped to the tile. Plain tiles show a round `am-screen-check` badge, media tiles (sidebar) the
  preview above a "Name · N files" line and the hover actions (`am-tile-action`, opaque `sm-button-icon`s) on top.
- Asset view (`dialog-table-asset-manager-embedded.fxml`, Ctrl+M): a 620px column under the common details header
  with the same blocks stacked: search tile (caption + sources, then field + search button, results + preview),
  screens tile (map + preview, then an `am-section-top` "INSTALLED" section with the actions, then the list).
- Panel header = `sm-divider` row: `sm-caption` ("FIND WHEEL" / "INSTALLED" + `sm-title` screen name), then the actions:
  icon-only `sm-button-icon`s with tooltips (only Upload keeps its text), `folder-component` + reload right-aligned.
- Lists are `am-list`: flat rows with 1px `-c-table-divider` lines and the table selection look (amber tint, 3px
  `-c-warn` bar). Search rows = `am-format-badge` (MP4, PNG) + `sm-mono` name + `sm-small` "emulator · author";
  installed rows = `am-file-icon` type icon + `sm-mono` name + `sm-small` "PNG · 214 KB" + `sm-pill-ok` "Default".
- Preview column (`am-preview-column`, hairline left border): `am-preview` inset box with an `sm-hint` / `sm-small`
  empty state, then `sm-accent-button` Download (+ web preview) or the `sm-small` "PNG · 512 × 512 · 214 KB" meta line.
- Footer = `sm-footer`: `default-button`s left, `submit-button` "Done" right.

### Playlist manager (`playlistmanager/dialog-playlist-manager.fxml`, `playlist-table.fxml`)
- Split view of `sm-panel`s (gap 14, page padding `12 16 14 16`): 320px playlist tree panel and a growing column with
  the settings panel above the games panel.
- Every panel starts with an `sm-divider` header (`sm-caption`, upper-cased by the controller); the settings header adds
  the `sm-title` name of the selected playlist. Tree panel: reload button in the header, then a
  `floating-toolbar-transparent` with create / rename | asset manager | `sm-danger-button` delete, then the TreeView.
- Settings body (padding `14 18 18 18`, 22 between the two columns): "OPTIONS" check boxes (`sm-text`, 14px muted help
  icons) and 130px `sm-text` field rows; "SQL QUERY" header row with the SQL check box, template `MenuButton` and
  "Save query" right-aligned, an `sm-script-text` area and the `sm-alert` with the SQL error below.
- Games panel: `floating-toolbar-transparent` (search | emulator | rebuild, `sm-danger-button` remove, `sm-hint` count
  right-aligned) above the flat table. Tree rows use token colors (`-c-error` for SQL errors, `-c-disabled` for hidden
  playlists) and 16px muted type icons.
- Footer = `sm-footer` with the `sm-small` favourites hint (info icon) and the `submit-button` "Close".
- Small form dialogs (`dialog-new-playlist.fxml`, `dialog-playlist-templates.fxml`): content padding `12 16 14 16`, an
  optional `sm-inset` info row (`c-icon-info` icon + `sm-hint` lines), one `sm-tile, sm-tile-bordered` form grid
  (hgap / vgap 8, fixed `sm-text` label column, fields fill the width), `sm-footer` with `default-button` Cancel / Close
  (`cancelButton`) and the `submit-button` (`defaultButton`).

## 7. Checklist for a new view

1. Page background stays the default; content goes into `sm-tile` cards or `sm-panel` split views.
2. Only colors from section 1, only font sizes from section 2.
3. Status = pill/banner variant by meaning; at most one `sm-accent-button` per panel.
4. Errors = `sm-alert` box (or `sm-field-error` next to a field), never a filled red background.
5. No default-styled or borderless buttons; small inline actions use `sm-button-small`.
6. Long text wraps (`wrapText="true"`), all text from the resource bundles (see i18n rules), "…" instead of "...".
7. Never rebuild nodes from size listeners during layout — defer with `Platform.runLater`.
8. Collapsible content = `Accordion` / `TitledPane` with an icon + `sm-collapsible-title` graphic header (section 6), no custom styling.
