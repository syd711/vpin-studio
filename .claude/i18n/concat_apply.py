"""
Applies the concatenation -> placeholder conversion.

usage: python -I concat_apply.py <chains.json> <overrides.py> [<overrides.py> ...] [--dry]

- chains.json: output of concat_extract.py (must be fresh: spans are verified against the sources)
- overrides files define O = {key: (en, de, es, fr, it, pt)} with natural apostrophes, optional
  CUSTOM = {key: "replacement java code"} and EXTRA_KEYS = [(key, en, de, es, fr, it, pt)]
UI chains -> Messages.get("msg.x", args); LOG chains -> plain English concatenation (logs stay English).
Afterwards fragment keys that are no longer referenced in ui/commons sources (Java + FXML) are removed
from all bundles.
"""
import os, re, sys, io, json

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
ROOT = r"C:\workspace\vpin-studio"
UI = os.path.join(ROOT, r"vpin-studio-ui\src\main\java\de\mephisto\vpin\ui")
SRC_DIRS = [os.path.join(ROOT, r"vpin-studio-ui\src\main"), os.path.join(ROOT, r"vpin-studio-commons\src\main")]
MSG = os.path.join(ROOT, r"vpin-studio-commons\src\main\resources\de\mephisto\vpin\ui\messages")
LANGS = ["en", "de", "es", "fr", "it", "pt"]
DYNAMIC_PREFIXES = ("backglass.res_bulk.description_", "bulk_importer.match_mode.", "bulk_importer.matched_by_")

args = [a for a in sys.argv[1:] if not a.startswith('--')]
DRY = '--dry' in sys.argv
chains = json.load(open(args[0], encoding='utf-8'))
O, CUSTOM, EXTRA = {}, {}, []
for f in args[1:]:
    ns = {}
    exec(open(f, encoding='utf-8').read(), ns)
    O.update(ns.get('O', {})); CUSTOM.update(ns.get('CUSTOM', {})); EXTRA += ns.get('EXTRA_KEYS', [])
POST = [  # (file, old, new) hand edits that belong to the conversion
  ("tables/TableStatusToggleProgressModel.java", 'String action = game.isDisabled() ? "enabled" : "disabled";',
   'String action = game.isDisabled() ? Messages.get("dialog.table_action_enabled") : Messages.get("dialog.table_action_disabled");'),
]

def esc_prop(v):
    v = v.replace('\\', '\\\\').replace('\n', '\\n').replace('\t', '\\t').replace('\r', '\\r')
    if v.startswith(' '):
        v = '\\' + v
    return v

def java_lit(s):
    return '"' + s.replace('\\', '\\\\').replace('"', '\\"').replace('\n', '\\n').replace('\t', '\\t') + '"'

def load_raw(lang):
    fn = 'messages.properties' if lang == 'en' else f'messages_{lang}.properties'
    return os.path.join(MSG, fn)

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from concat_extract import load_bundle  # noqa
bundles = {l: load_bundle(l) for l in LANGS}

errors = []
needed = {}
for c in chains:
    if c['log']:
        continue
    needed.setdefault(c['key'], len(c['args']))
for k, n in needed.items():
    if k in CUSTOM:
        continue
    if k not in O:
        errors.append(f"no reviewed translation for {k}")
        continue
    if len(O[k]) != 6:
        errors.append(f"{k}: needs 6 values")
        continue
    for l, v in zip(LANGS, O[k]):
        ph = sorted(set(int(x) for x in re.findall(r'\{(\d+)\}', v)))
        if ph != list(range(n)):
            errors.append(f"{k} [{l}]: placeholders {ph} != {n} args: {v}")
for k in O:
    if k not in needed:
        errors.append(f"override for unknown key {k}")
for row in EXTRA:
    if row[0] in bundles['en']:
        errors.append(f"extra key exists: {row[0]}")

# ------------------------------------------------------------ rewrite sources
byfile = {}
for c in chains:
    byfile.setdefault(c['file'], []).append(c)
newsrc = {}
for f, cs in byfile.items():
    p = os.path.join(UI, f.replace('/', os.sep))
    src = open(p, encoding='utf-8', newline='').read()
    for c in sorted(cs, key=lambda c: -c['start']):
        if src[c['start']:c['end']] != c['orig']:
            errors.append(f"{f}:{c['line']}: source changed, re-run concat_extract.py")
            continue
        if c['log']:
            parts = []
            for x in c['pieces']:
                if x['t'] == 'key':
                    if x['args']:
                        parts.append(x['code'])  # rare; keep
                    else:
                        parts.append(java_lit(bundles['en'][x['k']]))
                else:
                    parts.append(x['code'])
            # merge adjacent literals
            merged = []
            for s in parts:
                if merged and merged[-1].startswith('"') and merged[-1].endswith('"') and s.startswith('"') and s.endswith('"') and len(s) > 1:
                    merged[-1] = merged[-1][:-1] + s[1:]
                else:
                    merged.append(s)
            call = ' + '.join(merged)
        elif c['key'] in CUSTOM:
            call = CUSTOM[c['key']]
        else:
            call = f'Messages.get("{c["key"]}"' + ''.join(', ' + a for a in c['args']) + ')'
        rep = ' + '.join(c['lead'] + [call] + c['trail'])
        src = src[:c['start']] + rep + src[c['end']:]
    newsrc[p] = src
for f, old, new in POST:
    p = os.path.join(UI, f.replace('/', os.sep))
    s = newsrc.get(p) or open(p, encoding='utf-8', newline='').read()
    if s.count(old) != 1:
        errors.append(f"POST edit not found in {f}")
    newsrc[p] = s.replace(old, new)

if errors:
    print("\n".join(errors)); sys.exit(1)

# ------------------------------------------------------------ new keys
rows = []
for k, n in needed.items():
    if k in CUSTOM:
        continue
    vals = O[k]
    rows.append((k,) + tuple(v.replace("'", "''") if n > 0 else v for v in vals))
for row in EXTRA:
    n = len(set(re.findall(r'\{(\d+)\}', row[1])))
    rows.append((row[0],) + tuple(v.replace("'", "''") if n > 0 else v for v in row[1:]))

# ------------------------------------------------------------ orphaned fragments
def referenced_keys(overrides):
    refs = set()
    for d in SRC_DIRS:
        for dp, dn, fns in os.walk(d):
            for fn in fns:
                if not fn.endswith(('.java', '.fxml')):
                    continue
                p = os.path.join(dp, fn)
                s = overrides.get(p) or open(p, encoding='utf-8', errors='replace').read()
                refs |= set(re.findall(r'"%?([A-Za-z][\w.\-]*\.[\w.\-]+)"', s))
    return refs

refs = referenced_keys(newsrc)
fragments = sorted({k for c in chains for k in c['keys']})
orphans = [k for k in fragments if k not in refs and not k.startswith(DYNAMIC_PREFIXES)]

print(f"{len(chains)} chains ({sum(1 for c in chains if c['log'])} log), {len(rows)} new keys, {len(orphans)} orphaned fragment keys to remove, {len(newsrc)} files")
if DRY:
    sys.exit(0)

for p, s in newsrc.items():
    with open(p, 'w', encoding='utf-8', newline='') as fh:
        fh.write(s)

orph = set(orphans)
for i, lang in enumerate(LANGS):
    path = load_raw(lang)
    raw = open(path, encoding='utf-8', newline='').read()
    nl = '\r\n' if '\r\n' in raw else '\n'
    lines = raw.split(nl)
    kept, removed = [], 0
    for line in lines:
        m = re.match(r'\s*([^#!=:\s][^=:\s]*)\s*[=:]', line)
        if m and m.group(1) in orph:
            removed += 1
            continue
        kept.append(line)
    text = nl.join(kept)
    if not text.endswith(nl):
        text += nl
    text += f"# placeholder messages (former concatenations){nl}" + nl.join(f"{r[0]}={esc_prop(r[1 + i])}" for r in rows) + nl
    with open(path, 'w', encoding='utf-8', newline='') as fh:
        fh.write(text)
    print(f"{os.path.basename(path)}: -{removed} +{len(rows)}")
open(os.path.join(os.path.dirname(os.path.abspath(__file__)), 'batches', 'concat_removed_keys.txt'), 'w', encoding='utf-8').write('\n'.join(orphans) + '\n')
