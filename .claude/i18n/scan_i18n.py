import os, re, sys, io
# usage: python -I scan_i18n.py <package> [--summary] [--commons]
#   <package> = sub package of de.mephisto.vpin.ui (e.g. "tables"), "_root" = the ui root package (non-recursive)
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
ROOT = r"C:\workspace\vpin-studio"
PKG = sys.argv[1] if len(sys.argv) > 1 else "tables"
SUMMARY = "--summary" in sys.argv
UI_JAVA = os.path.join(ROOT, r"vpin-studio-ui\src\main\java\de\mephisto\vpin\ui")
UI_RES = os.path.join(ROOT, r"vpin-studio-ui\src\main\resources\de\mephisto\vpin\ui")
if "--commons" in sys.argv:  # scan vpin-studio-commons (de.mephisto.vpin.commons.<pkg>) instead of the ui module
    UI_JAVA = os.path.join(ROOT, r"vpin-studio-commons\src\main\java\de\mephisto\vpin\commons")
    UI_RES = os.path.join(ROOT, r"vpin-studio-commons\src\main\resources\de\mephisto\vpin\commons")
JAVA = UI_JAVA if PKG == "_root" else os.path.join(UI_JAVA, PKG)
FXML = UI_RES if PKG == "_root" else os.path.join(UI_RES, PKG)
MSG = os.path.join(ROOT, r"vpin-studio-commons\src\main\resources\de\mephisto\vpin\ui\messages")

def walk(d):
    if not os.path.isdir(d):
        return
    if PKG != "_root":
        yield from os.walk(d)
    else:
        yield d, [], [f for f in os.listdir(d) if os.path.isfile(os.path.join(d, f))]

def load(p):
    keys = {}
    with open(p, encoding='utf-8') as f:
        cont = None
        for line in f:
            line = line.rstrip('\n')
            if cont:
                cont = line.endswith('\\')
                continue
            s = line.strip()
            if not s or s.startswith('#') or s.startswith('!'):
                continue
            m = re.match(r'([^=:\s]+)\s*[=:]', s)
            if m:
                keys[m.group(1)] = s
            cont = line.endswith('\\')
    return keys

bundles = {}
for fn in os.listdir(MSG):
    m = re.match(r'messages(?:_(\w+))?\.properties$', fn)
    if m:
        bundles[m.group(1) or 'en'] = load(os.path.join(MSG, fn))
en = bundles['en']

used = {}
def use(k, loc):
    used.setdefault(k, []).append(loc)

# Java scan
SKIP_CALL = re.compile(r'(LOG\.|log\.|getLogger|Messages\.get\(|\.getStyleClass|setStyle\(|setId\(|getResource|FXMLLoader|equals|endsWith|startsWith|contains\(|indexOf|replace|split|matches|Pattern|lookup\(|\.get\(\"|put\(|getString\(|getProperty|setProperty|case \"|@\w+|throw new|Exception\(|System\.|\.css|\.fxml|\.png|\.jpg|PreferenceNames|addListener|substring|format\(\"%|DateTimeFormatter|SimpleDateFormat)')
UI_CALL = re.compile(r'(setText|setPromptText|setTitle|setHeaderText|setContentText|new Label|new Button|new Tooltip|new MenuItem|new CheckBox|new Tab|new Hyperlink|new RadioButton|new TableColumn|new CheckMenuItem|new Menu\(|showAlert|showConfirmation|showInformation|showOutOfDateConfirmation|showAlertOption|showConfirmationWithCheckbox|showAlertOptionWithCheckbox|showConfirmationWithOption|showErrorAlert|setTooltip|createUpdateIcon|createCheckAndUpdateIcon|createCheckAndIgnoredIcon|createCheckboxIcon|createAlertIcon|createIcon|setPlaceholder|ProgressDialog|createProgressDialog|ProgressResultModel|setStatus|appendText|TooltipBuilder|setAccessibleText|Dialogs\.|new ProgressModel|super\(\"|createSuccessIcon|createExclamationIcon)')
STR = re.compile(r'"((?:[^"\\]|\\.)*)"')
def is_text(s):
    if len(s) < 2: return False
    if not re.search(r'[A-Za-z]{2,}', s): return False
    if re.fullmatch(r'[a-z0-9_.\-/]+', s) and ' ' not in s:  # keys, ids, paths
        return False
    if re.fullmatch(r'[\w\-]+\.(\w{2,5})', s): return False  # filenames
    if s.startswith('-fx-') or s.startswith('#') or s.startswith('http') or s.startswith('/'): return False
    if re.fullmatch(r'[A-Z_0-9]+', s): return False  # constants
    if re.fullmatch(r'[a-z][a-zA-Z0-9]*', s): return False  # camelCase ids
    return True

java_hits = []
for dp, dn, fns in walk(JAVA):
    for fn in fns:
        if not fn.endswith('.java'): continue
        p = os.path.join(dp, fn)
        rel = os.path.relpath(p, ROOT)
        with open(p, encoding='utf-8') as f:
            lines = f.readlines()
        for i, line in enumerate(lines, 1):
            for m in re.finditer(r'Messages\.get\(\s*"([^"]+)"', line):
                use(m.group(1), f"{rel}:{i}")
            code = line.strip()
            if code.startswith('//') or code.startswith('*') or code.startswith('/*'):
                continue
            strs = [s for s in STR.findall(line) if is_text(s)]
            if not strs: continue
            if re.search(r'\b(LOG|log)\.(info|warn|error|debug|trace)', line): continue
            if re.search(r'throw new|Exception\(', line): continue
            # remove Messages.get("key") strings
            strs = [s for s in strs if not re.search(r'Messages\.get\(\s*"' + re.escape(s) + '"', line)]
            if not strs: continue
            ui = bool(UI_CALL.search(line))
            # suspicious: contains a space and capital letter word (sentence-like)
            sentence = any(re.search(r'[A-Za-z]+ [A-Za-z]+', s) or re.fullmatch(r'[A-Z][a-z]+:?', s) for s in strs)
            if ui or sentence:
                if re.search(r'\.(equals|endsWith|startsWith|contains|indexOf|matches|replace|replaceAll|split)\(', line) and not ui:
                    continue
                java_hits.append((rel, i, 'UI' if ui else '??', code[:220]))

# FXML scan
fxml_hits = []
ATTR = re.compile(r'\b(text|promptText|title|headerText|contentText|accessibleText)="([^"]*)"')
for dp, dn, fns in walk(FXML):
    for fn in fns:
        if not fn.endswith('.fxml'): continue
        p = os.path.join(dp, fn)
        rel = os.path.relpath(p, ROOT)
        with open(p, encoding='utf-8') as f:
            lines = f.readlines()
        for i, line in enumerate(lines, 1):
            for m in re.finditer(r'"%([A-Za-z0-9_.\-]+)"', line):
                use(m.group(1), f"{rel}:{i}")
            for m in ATTR.finditer(line):
                v = m.group(2)
                if v.startswith('%') or v.startswith('$'): continue
                if not re.search(r'[A-Za-z]{2,}', v): continue
                fxml_hits.append((rel, i, m.group(1), v))
            # Tooltip text spanning <Tooltip text="..."> covered above

if SUMMARY:
    miss = {l: len([k for k in used if k in en and k not in b]) for l, b in sorted(bundles.items()) if l != 'en'}
    print(f"{PKG}\tjava_ui={sum(1 for h in java_hits if h[2]=='UI')}\tjava_other={sum(1 for h in java_hits if h[2]!='UI')}\tfxml={len(fxml_hits)}\tundefined={len([k for k in used if k not in en])}\tuntranslated={miss}")
    sys.exit(0)
print("=== JAVA hardcoded candidates:", len(java_hits))
for h in java_hits: print(f"{h[0]}:{h[1]} [{h[2]}] {h[3]}")
print()
print("=== FXML hardcoded:", len(fxml_hits))
for h in fxml_hits: print(f"{h[0]}:{h[1]} {h[2]}=\"{h[3]}\"")
print()
missing_en = {k: v for k, v in used.items() if k not in en}
print("=== Keys used but missing in messages.properties:", len(missing_en))
for k, v in sorted(missing_en.items()): print(k, '->', v[0])
print()
for lang, b in sorted(bundles.items()):
    if lang == 'en': continue
    miss = sorted(k for k in used if k in en and k not in b)
    print(f"=== Keys used by tables, present in en but missing in {lang}: {len(miss)}")
    for k in miss: print('  ', k, '->', used[k][0])
