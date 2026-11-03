"""usage: python -I crosscheck_bundles.py [--verbose]
Cross-checks resource bundle keys against their usages in Java and FXML.

Bundles:
  client: vpin-studio-commons/.../de/mephisto/vpin/ui/messages/messages*.properties
          used by Messages.get(...) in ui + commons Java and %key in ui + commons FXML
  server: vpin-studio-server/.../messages/messages*.properties
          used by ServerMessages.msg/get(...) and messageSource in server Java

Reports per bundle:
  MISSING  - keys referenced in code/FXML that do not exist in the English bundle
  UNUSED   - keys in the English bundle that are not referenced anywhere
             (a key counts as referenced if it appears as any string literal in the module's Java
              code, or matches a dynamic prefix like Messages.get("foo." + x))
  LANG     - keys missing/extra/duplicated per language vs. English
"""
import os, re, sys, io, collections

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
ROOT = r"C:\workspace\vpin-studio"
VERBOSE = "--verbose" in sys.argv

BUNDLES = {
    "client": {
        "dir": os.path.join(ROOT, r"vpin-studio-commons\src\main\resources\de\mephisto\vpin\ui\messages"),
        "java": [os.path.join(ROOT, m, r"src\main\java") for m in ("vpin-studio-ui", "vpin-studio-commons", "vpin-studio-app")],
        "fxml": [os.path.join(ROOT, m, r"src\main\resources") for m in ("vpin-studio-ui", "vpin-studio-commons", "vpin-studio-app")],
        "calls": [r"\bMessages\.get\(", r"\bresources\.getString\(", r"\bMessages\.getBundle\(\)\.getString\("],
    },
    "server": {
        "dir": os.path.join(ROOT, r"vpin-studio-server\src\main\resources\messages"),
        "java": [os.path.join(ROOT, r"vpin-studio-server\src\main\java")],
        "fxml": [],
        "calls": [r"\bServerMessages\.msg\(", r"\bServerMessages\.get\((?:messageSource\s*,\s*)?", r"\bmessageSource\.getMessage\("],
    },
}


def load_props(path):
    """returns list of keys in order (keeps duplicates)"""
    keys = []
    with open(path, encoding='utf-8') as f:
        cont = False
        for line in f:
            line = line.rstrip('\r\n')
            if cont:
                cont = line.endswith('\\') and not line.endswith('\\\\')
                continue
            s = line.lstrip()
            if not s or s[0] in '#!':
                continue
            m = re.match(r'((?:[^=:\s\\]|\\.)+)\s*[=:\s]', s + ' ')
            if m:
                keys.append(m.group(1).replace('\\', ''))
            cont = line.endswith('\\') and not line.endswith('\\\\')
    return keys


def files(dirs, ext):
    for d in dirs:
        for root, _, fs in os.walk(d):
            if os.sep + 'target' + os.sep in root + os.sep:
                continue
            for f in fs:
                if f.endswith(ext):
                    yield os.path.join(root, f)


STR = re.compile(r'"((?:[^"\\\n]|\\.)*)"')
COMMENT = re.compile(r'//[^\n]*|/\*.*?\*/', re.S)


def strip_comments(src):
    # remove comments but keep string literals (naive: protect strings first)
    out, i, n = [], 0, len(src)
    while i < n:
        c = src[i]
        if c == '"':
            if src.startswith('"""', i):
                j = src.find('"""', i + 3)
                j = n if j < 0 else j + 3
            else:
                j = i + 1
                while j < n and src[j] != '"' and src[j] != '\n':
                    j += 2 if src[j] == '\\' else 1
                j += 1
            out.append(src[i:j]); i = j
        elif c == "'":
            j = i + 1
            while j < n and src[j] != "'" and src[j] != '\n':
                j += 2 if src[j] == '\\' else 1
            out.append(src[i:j + 1]); i = j + 1
        elif src.startswith('//', i):
            j = src.find('\n', i); j = n if j < 0 else j
            out.append(' '); i = j
        elif src.startswith('/*', i):
            j = src.find('*/', i + 2); j = n if j < 0 else j + 2
            out.append('\n' * src.count('\n', i, j)); i = j
        else:
            out.append(c); i += 1
    return ''.join(out)


def first_arg(src, start):
    """src[start] is just after '(' -> returns text of the first top-level argument"""
    depth, i, n = 0, start, len(src)
    while i < n:
        c = src[i]
        if c == '"':
            m = STR.match(src, i)
            i = m.end() if m else i + 1
            continue
        if c in '([{':
            depth += 1
        elif c in ')]}':
            if depth == 0:
                return src[start:i]
            depth -= 1
        elif c == ',' and depth == 0:
            return src[start:i]
        i += 1
    return src[start:]


def rel(p):
    return os.path.relpath(p, ROOT)


ok = True
for name, cfg in BUNDLES.items():
    print("=" * 100)
    print(f"BUNDLE: {name}  ({rel(cfg['dir'])})")
    print("=" * 100)
    langs = {}
    for fn in sorted(os.listdir(cfg['dir'])):
        m = re.match(r'messages(?:_(\w+))?\.properties$', fn)
        if m:
            langs[m.group(1) or 'en'] = load_props(os.path.join(cfg['dir'], fn))
    en = set(langs['en'])

    referenced = collections.defaultdict(list)   # key -> locations (explicit key usages)
    literals = set()                              # every string literal in the module's java
    prefixes = collections.defaultdict(list)      # dynamic prefix -> locations
    nonliteral = []                               # calls with a computed key
    call_re = re.compile('|'.join(cfg['calls']))

    for jf in files(cfg['java'], '.java'):
        src = strip_comments(open(jf, encoding='utf-8', errors='replace').read())
        for m in STR.finditer(src):
            literals.add(m.group(1))
        for m in call_re.finditer(src):
            arg = first_arg(src, m.end()).strip()
            line = src.count('\n', 0, m.start()) + 1
            loc = f"{rel(jf)}:{line}"
            lits = STR.findall(arg)
            if not lits:
                nonliteral.append((loc, arg))
                continue
            # "prefix." + x  -> dynamic prefix
            dyn = re.match(r'^"((?:[^"\\]|\\.)*)"\s*\+', arg)
            if dyn:
                prefixes[dyn.group(1)].append(loc)
                continue
            for lit in lits:
                # ternaries / expressions: every literal is a key; skip literals that are clearly not keys
                if re.fullmatch(r'[A-Za-z0-9_.\-]+', lit) and lit:
                    referenced[lit].append(loc)
                else:
                    nonliteral.append((loc, arg))

    for xf in files(cfg['fxml'], '.fxml'):
        src = open(xf, encoding='utf-8', errors='replace').read()
        for m in re.finditer(r'="%([^"]+)"', src):
            line = src.count('\n', 0, m.start()) + 1
            referenced[m.group(1)].append(f"{rel(xf)}:{line}")

    # ---- MISSING
    missing = {k: v for k, v in referenced.items() if k not in en}
    print(f"\n-- MISSING keys (referenced but not in messages.properties): {len(missing)}")
    for k in sorted(missing):
        print(f"   {k}")
        for loc in missing[k][:5]:
            print(f"        at {loc}")
    for p in sorted(prefixes):
        if not any(k.startswith(p) for k in en):
            ok = False
            print(f"   dynamic prefix with no matching keys: \"{p}\" + ...  at {prefixes[p][0]}")
    if missing:
        ok = False

    # ---- UNUSED
    def used(k):
        return k in referenced or k in literals or any(k.startswith(p) for p in prefixes)
    unused = sorted(k for k in en if not used(k))
    print(f"\n-- UNUSED keys (in messages.properties but not referenced): {len(unused)}")
    groups = collections.defaultdict(list)
    for k in unused:
        groups[k.split('.')[0]].append(k)
    for g in sorted(groups):
        for k in groups[g]:
            print(f"   {k}")

    # ---- computed keys (manual review)
    print(f"\n-- Calls with computed keys (manual check, keys may be built elsewhere): {len(nonliteral)}")
    for loc, arg in nonliteral:
        print(f"   {loc}: {arg[:100]}")
    print(f"\n-- Dynamic prefixes: " + ", ".join(f'"{p}"' for p in sorted(prefixes)))

    # ---- per language
    print(f"\n-- LANGUAGE consistency vs. en ({len(en)} keys)")
    for lang, keys in langs.items():
        ks = set(keys)
        dupes = [x for x, c in collections.Counter(keys).items() if c > 1]
        miss = sorted(en - ks)
        extra = sorted(ks - en)
        print(f"   {lang}: keys={len(ks)} missing={len(miss)} extra={len(extra)} dupes={len(dupes)}")
        for x in miss: print(f"        missing: {x}")
        for x in extra: print(f"        extra:   {x}")
        for x in dupes: print(f"        dupe:    {x}")
        if miss or extra or dupes:
            ok = False
    print()

sys.exit(0 if ok else 1)
