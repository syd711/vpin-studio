# VPin Studio UI Style Guide

Visual rules for the redesigned views (Achievements, System Manager). New or reworked screens follow these rules
so that colors, sizes, badges and buttons stay the same everywhere. **Reuse an existing token or class before
adding a new one**; a new color or font size needs a reason that none of the existing ones covers.

- CSS classes: `sm-*` at the end of `vpin-studio-ui/src/main/resources/de/mephisto/vpin/ui/stylesheet.css`
- Java builders and color constants: `vpin-studio-ui/.../ui/achievements/AchievementsUI.java`
- Prefer `styleClass="sm-…"` over inline `style="…"`. Inline styles are only fine for values computed at
  runtime (a category color, a progress fraction) and must still use the tokens below.

## 1. Colors

### Surfaces (dark → light)

| Token | Hex | Use |
|-------|-----|-----|
| inset | `#161616` | Inset boxes inside a tile (`sm-inset`), logs, text areas, progress track (`AchievementsUI.TRACK`), monitor map |
| footer | `#1C1C1C` | Pinned action footer of a panel (`sm-footer`), log group rows |
| panel | `#222222` | Panels of a split view (`sm-panel`), chips, selected detail boxes |
| tile | `#2A2A2A` | Cards / tiles on the page background (`sm-tile`, `AchievementsUI.CARD`) |
| control | `#333333` | Buttons, list-row hover, neutral pill background |
| control hover | `#3D3D3D` | Button hover |
| selected | `#3A3A3A` | Selected list row, borders of panels / dividers |
| border | `#444444` | Button, chip and text-field borders |

Table-like row lists in the Achievements view use `ROW_EVEN #283335` / `ROW_ODD #303733` / `ROW_SELECTED #333344`
with a 3px `#8C8CE0` left border for the selected row (`AchievementsUI.row()`). Do not mix this striped style with
the `sm-list-row` style inside one view.

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

There are exactly these button types. Don't use the default JavaFX button style inside the redesigned views.

| Class | Look | Use |
|-------|------|-----|
| `sm-button` | `#333333` fill, `#444444` border, white 14px, padding `5 12` | Default action, also every button in an `sm-actionbar` (styled automatically) |
| `sm-button, sm-button-icon` | same, square padding `5 7` | Icon-only actions (refresh, copy) next to inputs |
| `sm-button, sm-button-small` | same look, 12px, padding `3 9` | Secondary inline actions next to small text / pills, e.g. "Set as installed", "Reset version", "Ignore version" |
| `sm-accent-button` | orange fill `#DD9646`, dark text `#1A1305`, bold, padding `8 14` | The **one** primary action of a panel (install, save) — at most one per panel, usually in the `sm-footer` |
| `sm-danger-button` | `#3A1C1F` fill, `#6E2B30` border (`#FF6B70` on hover, fill `#4A2326`), `#FF6B70` bold 14px text and icons, padding `5 12` | Destructive actions (restart server, shutdown system, delete cabinet) and buttons flagging a problem (asset filter with excluded assets). Never a filled bright red. |
| `sm-alert-button` / `sm-alert-close` | transparent with red border | Only inside `sm-alert` |
| `sm-filter-pill` (ToggleButton) | round outline, 12px, selected = `#333333` fill | Filters / toggles in a panel header |
| `sm-chip` | round `#222222` with border, 12px | Small clickable tags (e.g. screen shortcuts) |
| `sm-link` (Hyperlink) | `#7FB2FF`, underline on hover | External links, with `mdi2o-open-in-new` 14px icon |
| `sm-section-header` | transparent, hand cursor | Collapsible section header (chevron + `sm-text` + `sm-small` state) |

Borderless text buttons are not used any more (the former `sm-ghost-button` was removed).

### Text inputs

Every `TextField`, `PasswordField` and `TextArea` is dark — styled globally via `.text-input` in `stylesheet.css`,
no class needed: `#161616` fill, `#444444` 1px border, radius 4, white 14px text, `#7A7A7A` prompt text,
`#7A7A7A` border when focused, `#333366` selection. Don't override it with inline white backgrounds.
`sm-text-field` is the same look and stays valid for explicitness. Full-width log / script areas use
`sm-log-text` / `sm-script-text`.

`ComboBox`, `DatePicker` and `ColorPicker` (`.combo-box-base`) use the same dark box: `#161616` fill, `#444444` border,
radius 4, white 14px text, `#BDBDBD` arrow (white on hover), `#1C1C1C` on hover, `#7A7A7A` border when focused or open.
The popup list is `#161616` with white cells and `#333366` for hover / selection. Editable combos have no extra
inner box — the editor is transparent inside the combo's border.

`Spinner` uses the same box with a transparent editor; the arrow buttons are transparent with `#BDBDBD` arrows
(white arrow + `#333333` fill on hover). `CheckBox` boxes are `#161616` with a `#444444` border (radius 3,
`#7A7A7A` on hover/focus) and a white check mark; `RadioButton` circles look the same with a white dot and have
the same outer size as the check box (~21px at 14px font) — keep both in sync when changing paddings.
`ScrollBar`: 10px, no arrow buttons, transparent track, rounded `#444444` thumb (`#5A5A5A` hover, `#7A7A7A` drag).
`ToggleButton` has the `sm-button` look (`#333333`, `#444444` border, `#3D3D3D` hover); selected = `#333366` fill
with an `#8C8CE0` border. `sm-filter-pill` toggles keep their round outline look.
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
| Status pill | `sm-pill` + `sm-pill-ok` / `-warn` / `-error` / `-unknown` | 12px bold, round, colored text on the tinted background of section 1 |
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

### Achievements view
- **KPI tile** (`overview/tile-unlocked.fxml`, `tile-cloud-rank.fxml`): `#2A2A2A` card, 16px title, 26px monospace number,
  14px colored delta line below (green for positive progress).
- **Level tile** (`overview/tile-level.fxml`, `badges/tile-level.fxml`): `levelCircle()` with orange ring, 20px level name,
  14px orange prestige text, `progress()` bar to the next level.
- **List tile** (`tile-recent-unlocks`, `tile-latest-scores`, `tile-ranking`, `tables/tile-table-list`): headline + striped
  `AchievementsUI.row()` rows, rank medals via `rank()`, avatars via `initials()`.
- **Badge grid** (`badges/tile-badges.fxml`, `badge-group.fxml`): `medal()` per badge, selected badge shown in a `#222222`
  detail box with an `#8C8CE0` left border and a 16px bold name.
- **Progress row** (`tile-closest.fxml`, `leaderboards/tile-table.fxml`): label, 12px `#9A9A9A` caption, flat `progress()` bar
  on `#161616`.
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
  right-most panel grows.
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
- **Folder tabs** (default for every TabPane, no class needed): flat header strip one step lighter than the content,
  tabs with 5px top radius, 14px `#BDBDBD` labels + icons (white on hover/selected, selected bold); the selected tab
  takes the content color so it merges with the page. Strip/content colors come from `-sm-tab-strip` /
  `-sm-tab-content`: `#2A2A2A`/`#222222` on the page, `#333333`/`#2A2A2A` inside a `subpanel`. If the tab content
  is a `#222222` page inside a subpanel (System Manager), add `tabs-dark-content` to the TabPane.
- **Underlined tabs** (secondary level): add `sm-tabs` to a TabPane — 14px bold `#BDBDBD` labels, white when selected/hovered, 2px orange underline.
- **Action bar** (`sm-actionbar`): top bar of a component tab; any Button inside gets the `sm-button` look automatically.

## 7. Checklist for a new view

1. Page background stays the default; content goes into `sm-tile` cards or `sm-panel` split views.
2. Only colors from section 1, only font sizes from section 2.
3. Status = pill/banner variant by meaning; at most one `sm-accent-button` per panel.
4. Errors = `sm-alert` box (or `sm-field-error` next to a field), never a filled red background.
5. No default-styled or borderless buttons; small inline actions use `sm-button-small`.
6. Long text wraps (`wrapText="true"`), all text from the resource bundles (see i18n rules), "…" instead of "...".
7. Never rebuild nodes from size listeners during layout — defer with `Platform.runLater`.
