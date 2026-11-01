# UI i18n Progress

Goal: no hardcoded user-visible English in `vpin-studio-ui/src/main/java/de/mephisto/vpin/ui/**` (Java + FXML),
and every key used by the UI translated in all 6 bundles (en, de, es, fr, it, pt).

Bundles: `vpin-studio-commons/src/main/resources/de/mephisto/vpin/ui/messages/messages*.properties`
Lookup: `Messages.get("key", args…)` (`de.mephisto.vpin.commons.utils.i18n.Messages`), FXML: `text="%key"`.

## Tooling (`.claude/i18n/`)

| File | Purpose |
|------|---------|
| `scan_i18n.py <pkg> [--summary]` | Lists hardcoded string candidates (`[UI]` = in a UI call, `[??]` = sentence-like), hardcoded FXML texts, undefined keys and untranslated keys for one package. `_root` = ui root package only. |
| `apply_i18n.py batches/<pkg>.py [--dry]` | Applies a batch: exact replacements in Java, appends new keys to all 6 bundles, adds missing translations, adds the `Messages` import. Validates counts, key clashes, quotes in MessageFormat values. Always run `--dry` first. |
| `concat_extract.py <out.json> [pkg…]` | Finds `+` chains built from `Messages.get` fragments and proposes one `msg.*` placeholder message each (templates assembled from the existing fragment translations, for review). |
| `concat_apply.py <chains.json> <overrides.py…> [--dry]` | Applies reviewed `msg.*` translations, rewrites the chains (LOG chains → plain English), deletes fragment keys nobody references anymore (`batches/concat_removed_keys.txt`). |
| `check_bundles.py` | Verifies every bundle has all en keys and no duplicate keys (exit code 1 on problems). |
| `batches/` | One batch file per run, kept as a record of what was applied. **Never re-run an applied batch** (keys would be appended twice; `apply_i18n.py` refuses existing keys, so it fails safely). |

Workflow per package:
1. `python -I .claude/i18n/scan_i18n.py <pkg>` and review hits (read context where unclear).
2. Write `batches/<pkg>.py` (keys + replacements). Reuse existing keys (`common.*`, etc.) where the value matches.
3. `apply_i18n.py --dry`, then apply.
4. `mvn -q -o compile -pl vpin-studio-ui -am` must be clean.
5. Re-scan, update the table below.

## Conventions

- New keys: `dialog.*` (matches the existing code-side keys), no clash with existing keys (apply script checks).
- Values with `{0}` go through `MessageFormat` → double single quotes (`l''audio`). Values without args are returned raw.
- Literal `" & < >` in bundles, never HTML entities. Use `…` (U+2026), not `...`.
- Portuguese: European Portuguese (ficheiro, guardar, eliminar, ecrã, multimédia, transferir, predefinição, utilizador,
  partilhar, subscrição, "a + infinitivo" instead of gerúndio). `pt_apply.py --check-only` lists Brazilian leftovers.
- German: "Sie" form, "Tisch" for table. Product/technical names (ALT Sound, PUP Pack, DirectB2S, VPS, ROM, DMD) stay as they are.
- Leave alone: log messages, exception messages, identifiers used in `switch`/`equals`, file/folder names, date patterns,
  CSS, preference keys, thread names. If a localized value is compared later, compare against `Messages.get(...)` too.
- Static lists (combo models) are built at class-load time; that is acceptable since a language change needs a restart.

## Status

Initial scan (2026-11-01, after `tables` was done). Counts are scanner candidates, not confirmed findings.

| Package | java_ui | java_other | fxml | untranslated | Status | Notes |
|---------|--------:|-----------:|-----:|-------------:|--------|-------|
| tables | 2 | 40 | 4 | 0 | ✅ done | Remaining hits intentionally skipped (cache ids, AA modes, date patterns, "(ID: …)") |
| _root | 5 | 7 | 0 | 0 | ✅ done | Skipped: window titles "VPin Studio (…)", thread names |
| backglassmanager | 7 | 12 | 1 | 0 | ✅ done | Skipped: "B2SPro/DirectB2S Backglass" type names, FXML "px"; `backglass.res_bulk.description_` is a dynamic prefix |
| backups | 5 | 4 | 0 | 0 | ✅ done |  |
| cards | 6 | 37 | 0 | 0 | ✅ done | Skipped: Instructions card scanner/controller (dev-only tool, not referenced elsewhere), template property names ("Width", "Alignment") |
| competitions | 2 | 13 | 0 | 0 | ✅ done |  |
| components | 4 | 4 | 0 | 0 | ✅ done | Skipped: DOFTester table nouns ("DOF Tester") |
| dropins | 3 | 0 | 0 | 0 | ✅ done |  |
| events | 0 | 0 | 0 | 0 | ✅ done | nothing to do |
| jobs | 0 | 1 | 0 | 0 | ✅ done | only a thread name |
| launcher | 1 | 11 | 0 | 0 | ✅ done | Skipped: fonts, SQL |
| mania | 4 | 0 | 1 | 0 | ✅ done | Skipped: FXML "-id-" placeholder |
| monitor | 2 | 1 | 0 | 0 | ✅ done | Skipped: "Menu" equals check |
| players | 2 | 1 | 0 | 0 | ✅ done | Skipped: font name |
| playlistmanager | 6 | 22 | 0 | 0 | ✅ done | SQL template names localized (display only, map key used for menu text) |
| preferences | 26 | 15 | 1 | 1 (es/fr/it/pt) | ✅ done | Skipped: PINemHi voice names (native language names), "Webhooks" default set name, VR wiki URL |
| recorder | 2 | 1 | 0 | 0 | ✅ done | Skipped: "Menu" equals check |
| util | 17 | 40 | 0 | 0 | ✅ done | Skipped: PositionResizerDemo2/3 (dev demos), RichText VBScript keywords, Keys "Esc", font defaults, thread names |
| vps | 3 | 41 | 0 | 0 | ✅ done | Skipped: link host names (Mega, Dropbox, Ext), emulator product names; feature tooltips in VpsUtil localized |
| vpxz | 11 | 4 | 0 | 0 | ✅ done |  |

**All packages done (2026-11-01).** Final verification: every package rescanned with `--summary`, `check_bundles.py` clean,
`mvn -q -o compile -pl vpin-studio-ui -am` clean. Nothing committed yet; not run in the app.

## Known gaps / ideas for a follow-up session

- The scanner only sees string literals on a line with a UI call or a sentence-like literal. It misses:
  - keys chosen via ternary (`Messages.get(a ? "k1" : "k2")`) — the "undefined/untranslated" check does not see them
    (that's how `active_ini_desktop/vr` slipped through; `check_bundles.py` catches the translation side)
  - concatenations of single words, e.g. `size + " " + noun` (found `BaseTableController` by hand)
- `components/doftester/DOFTesterController` still passes English nouns to `BaseTableController.initialize`.
- ~~Older keys built by concatenation~~ → done 2026-11-01, see log.
- ~~Portuguese variant mismatch~~ → aligned to European Portuguese 2026-11-01 (`batches/pt_european.py`, `pt_apply.py`).
- Translations for es/fr/it/pt were written by Claude, should be reviewed by native speakers.
- Other modules (`vpin-studio-commons` FX components, server `messages/`) were not part of this run.

## Log

- 2026-11-01: 149 pt values from the first sweep converted to European Portuguese; `msg.*` keys were already written in pt-PT. Remaining scanner hits are false positives ("arquivo" = archive, "os seus").

- 2026-11-01: concatenation → placeholder conversion. 372 chains in 140 files (56 inside `LOG` calls, now plain English
  instead of localized). 245 new `msg.*` keys with reviewed translations for all 6 languages
  (`batches/concat_overrides_1..4.py`, chains in `batches/concat_chains.json`), 274 orphaned fragment keys removed.
  Fixed on the way: fragments with a leading space that the loader dropped ("10elements"), German word order
  ('" löschen{0}"?'), missing ¿ in Spanish, "tabla" → "mesa" (es), "tabella" → "tavolo" (it), "Re-validating {0} +  tables",
  "is used by for competition", move/clone error now has its own message per action, table enable/disable alert uses
  localized action words. Remaining `+` around `Messages.get` are whitespace padding / newlines only.

- 2026-11-01: batches `preferences_util_vps.py` (56 keys, 79 replacements, 1 missing translation), `preferences_fixup.py` (card size confirmation), `missing_ternary_keys.py` (2 es/fr/it/pt translations). Added `check_bundles.py`. All packages done.

- 2026-11-01: batch `cards_playlists_vpxz.py` (35 keys, 50 replacements).

- 2026-11-01: batch `small_packages.py` (66 keys, 82 replacements): _root, backglassmanager, backups, competitions, components, dropins, launcher, mania, monitor, players, recorder. Also: `BaseTableController` count label nouns ("N tables") now localized via callers' `super.initialize(Messages.get(noun), Messages.get(nouns), …)`; DOFTester still passes English.

- 2026-11-01: `tables` done (145 new keys, 27 missing translations, 55 files). Fixed swapped VPX/FP filter labels in `TableUploadController`, AltSound "looped" filter now compares localized Yes/No.
