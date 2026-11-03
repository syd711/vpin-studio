"""
Applies one i18n batch file to the UI sources and the message bundles.

usage: python -I apply_i18n.py batches/<name>.py [--dry]

A batch file defines:
  NEW_KEYS = [(key, en, de, es, fr, it, pt), ...]        new keys, appended to all 6 bundles
  R        = [(file, old, new[, count]), ...]            exact replacements, file relative to de/mephisto/vpin/ui
                                                         count defaults to 1, -1 = any number (also 0)
  MISSING  = {key: {lang: value}}                       (optional) translations for existing en keys
  REGEX    = [(file, pattern, replacement, count)]      (optional) regex replacements for multi-line code
  BASE     = "vpin-studio-commons/src/main"             (optional) other source base dir (relative to repo root)

Rules for values: MessageFormat is applied when args are passed -> double single quotes ('') in values with {0}.
Write literal " & < > (no HTML entities), use "…" instead of "...", "\\n" for line breaks.
The Messages import is added automatically to every touched file that uses Messages.get.
"""
import os, re, sys, io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
ROOT = r"C:\workspace\vpin-studio"
UI = os.path.join(ROOT, r"vpin-studio-ui\src\main\java\de\mephisto\vpin\ui")
MSG = os.path.join(ROOT, r"vpin-studio-commons\src\main\resources\de\mephisto\vpin\ui\messages")
IMPORT = "import de.mephisto.vpin.commons.utils.i18n.Messages;"
LANGS = ["en", "de", "es", "fr", "it", "pt"]

batch = sys.argv[1]
DRY = '--dry' in sys.argv
ns = {}
exec(open(batch, encoding='utf-8').read(), ns)
NEW_KEYS = ns.get('NEW_KEYS', [])
R = ns.get('R', [])
MISSING = ns.get('MISSING', {})
REGEX = ns.get('REGEX', [])
TITLE = ns.get('TITLE', os.path.splitext(os.path.basename(batch))[0])
if 'BASE' in ns:  # optional: source base dir relative to ROOT (e.g. "vpin-studio-commons/src/main"), files in R relative to it
    UI = os.path.join(ROOT, ns['BASE'].replace('/', os.sep))
if 'BUNDLE' in ns:  # optional: other bundle dir relative to ROOT (server: "vpin-studio-server/src/main/resources/messages")
    MSG = os.path.join(ROOT, ns['BUNDLE'].replace('/', os.sep))
IMPORT = ns.get('IMPORT', IMPORT)          # optional: import line added to touched files
CALL = ns.get('CALL', 'Messages.get(')     # optional: call that triggers the import
ESCAPE = ns.get('ESCAPE_UNICODE', False)   # optional: write non-ASCII bundle chars as backslash-uXXXX (server bundles)


def esc(v):
    return ''.join(c if ord(c) < 128 else (chr(92) + 'u%04x' % ord(c)) for c in v) if ESCAPE else v

errors = []
contents = {}


def get(f):
    p = os.path.join(UI, f.replace('/', os.sep))
    if p not in contents:
        with open(p, encoding='utf-8', newline='') as fh:
            s = fh.read()
        contents[p] = [s, '\r\n' if '\r\n' in s else '\n']
    return p


for r in R:
    p = get(r[0])
    s, nl = contents[p]
    old, new = r[1].replace('\n', nl), r[2].replace('\n', nl)
    exp = r[3] if len(r) > 3 else 1
    c = s.count(old)
    if exp != -1 and c != exp:
        errors.append(f"{r[0]}: expected {exp}, found {c}: {old[:100]}")
        continue
    contents[p][0] = s.replace(old, new)

for f, pat, rep, exp in REGEX:
    p = get(f)
    s, n = re.subn(pat, rep, contents[p][0])
    if n != exp:
        errors.append(f"{f}: regex expected {exp}, found {n}: {pat[:100]}")
    contents[p][0] = s

key_re = re.compile(r'^([^#!\s=:][^=:]*?)\s*[=:]', re.M)
en_text = open(os.path.join(MSG, 'messages.properties'), encoding='utf-8').read()
en_keys = set(key_re.findall(en_text))
new_names = [r[0] for r in NEW_KEYS]
if len(set(new_names)) != len(new_names):
    errors.append("duplicate keys inside batch")
for row in NEW_KEYS:
    if len(row) != 7:
        errors.append(f"key row needs 7 entries: {row[0]}")
    if row[0] in en_keys:
        errors.append(f"key already exists: {row[0]}")
    for v in row[1:]:
        if re.search(r'\{\d', v) and re.search(r"(?<!')'(?!')", v):
            errors.append(f"single quote in MessageFormat value: {row[0]}: {v}")
        if re.search(r'&(quot|amp|lt|gt|#\d+);', v) or '...' in v:
            errors.append(f"entity or '...' in value: {row[0]}: {v}")
for k in MISSING:
    if k not in en_keys:
        errors.append(f"MISSING key not in en bundle: {k}")
used = set()
for s, _ in contents.values():
    used |= set(re.findall(r'Messages\.(?:get|msg)\(\s*(?:messageSource\s*,\s*)?"([^"]+)"', s))
for k in sorted(used - en_keys - set(new_names)):
    if not k.endswith(('.', '_')):  # dynamic prefixes
        errors.append(f"undefined key used: {k}")

if errors:
    print("\n".join(errors))
    sys.exit(1)
if DRY:
    print(f"dry run ok: {len(R)} replacements, {len(NEW_KEYS)} new keys, {len(MISSING)} missing translations")
    sys.exit(0)

for p, (s, nl) in contents.items():
    if CALL in s and IMPORT not in s and not p.endswith('ServerMessages.java'):
        m = re.search(r'^import ', s, re.M)
        s = s[:m.start()] + IMPORT + nl + s[m.start():]
    with open(p, 'w', encoding='utf-8', newline='') as fh:
        fh.write(s)
    print("updated", os.path.relpath(p, UI))

for i, lang in enumerate(LANGS):
    fn = 'messages.properties' if lang == 'en' else f'messages_{lang}.properties'
    path = os.path.join(MSG, fn)
    raw = open(path, encoding='utf-8', newline='').read()
    nl = '\r\n' if '\r\n' in raw else '\n'
    existing = set(key_re.findall(raw))
    lines = []
    if lang != 'en':
        miss = [(k, d[lang]) for k, d in MISSING.items() if lang in d and k not in existing]
        if miss:
            lines.append(f"# missing translations ({TITLE})")
            lines += [f"{k}={esc(v)}" for k, v in miss]
    if NEW_KEYS:
        lines.append(f"# {TITLE} i18n")
        lines += [f"{r[0]}={esc(r[1 + i])}" for r in NEW_KEYS]
    if lines:
        add = ('' if raw.endswith('\n') else nl) + nl.join(lines) + nl
        with open(path, 'a', encoding='utf-8', newline='') as fh:
            fh.write(add)
    print(fn, "+", len(lines))
