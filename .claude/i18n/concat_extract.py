"""
Finds string concatenations built from Messages.get(...) fragments in the UI sources and proposes
one placeholder message per concatenation.

usage: python -I concat_extract.py <out.json> [package ...]
  packages are sub packages of de/mephisto/vpin/ui ("_root" = root package only); default: all

A chain is a run of operands joined by '+' at the same nesting level. It is converted when it contains at
least one Messages.get("key"[, args]) and at least one other expression, or at least two Messages.get calls.
Leading/trailing whitespace-only literals stay outside the new call.
Each chain gets: pieces, the assembled template per language ({n} for expressions), a proposed key and
the replacement code. Review/correct the templates, then run concat_apply.py.
"""
import os, re, sys, io, json

ROOT = r"C:\workspace\vpin-studio"
UI = os.path.join(ROOT, r"vpin-studio-ui\src\main\java\de\mephisto\vpin\ui")
MSG = os.path.join(ROOT, r"vpin-studio-commons\src\main\resources\de\mephisto\vpin\ui\messages")
LANGS = ["en", "de", "es", "fr", "it", "pt"]

# ---------------------------------------------------------------- properties
def unescape_prop(v):
    out, i = [], 0
    while i < len(v):
        c = v[i]
        if c == '\\' and i + 1 < len(v):
            n = v[i + 1]
            if n == 'n': out.append('\n')
            elif n == 't': out.append('\t')
            elif n == 'r': out.append('\r')
            elif n == 'u' and i + 5 < len(v) + 0:
                out.append(chr(int(v[i + 2:i + 6], 16))); i += 6; continue
            else: out.append(n)
            i += 2
            continue
        out.append(c); i += 1
    return ''.join(out)

def load_bundle(lang):
    fn = 'messages.properties' if lang == 'en' else f'messages_{lang}.properties'
    vals = {}
    lines = open(os.path.join(MSG, fn), encoding='utf-8').read().split('\n')
    i = 0
    while i < len(lines):
        line = lines[i].rstrip('\r'); i += 1
        s = line.lstrip()
        if not s or s[0] in '#!':
            continue
        while line.endswith('\\') and not line.endswith('\\\\') and i < len(lines):
            line = line[:-1] + lines[i].rstrip('\r').lstrip(); i += 1
        m = re.match(r'\s*([^=:\s]+)\s*[=:]', line)
        if not m:
            continue
        raw = line[m.end():]
        # the bundles use "key=value"; leading whitespace in a fragment is intentional (it is lost by the loader -> bug we fix here)
        vals[m.group(1)] = unescape_prop(raw)
    return vals

# ---------------------------------------------------------------- java tokenizer
TOK = re.compile(r'''
   (?P<ws>\s+)
 | (?P<lc>//[^\n]*)
 | (?P<bc>/\*.*?\*/)
 | (?P<tb>"""(?:.|\n)*?""")
 | (?P<str>"(?:[^"\\\n]|\\.)*")
 | (?P<chr>'(?:[^'\\\n]|\\.)+')
 | (?P<id>[A-Za-z_$][\w$]*)
 | (?P<num>\d[\w.]*)
 | (?P<op>->|::|\+\+|--|&&|\|\||[=!<>]=|<<|>>>?|[-+*/%&|^!~?:;,.=<>(){}\[\]@])
''', re.X | re.S)

def tokenize(src):
    toks = []
    for m in TOK.finditer(src):
        k = m.lastgroup
        if k in ('ws', 'lc', 'bc'):
            continue
        toks.append((k, m.group(), m.start(), m.end()))
    return toks

def java_unescape(lit):
    body = lit[1:-1]
    return unescape_prop(body)

OPEN, CLOSE = '([{', ')]}'
STOP_LEFT = {'(', '[', '{', ',', ';', '=', '?', ':', 'return', '->', '&&', '||', '==', '!=', '+=', 'throw', 'case', 'else'}
STOP_RIGHT = {')', ']', '}', ',', ';', '?', ':', '&&', '||', '==', '!=', '->'}

def operand_end(toks, i):
    """i = first token of operand; returns index after operand (stops at '+' or a terminator, depth 0)"""
    depth = 0
    j = i
    while j < len(toks):
        t = toks[j][1]
        if t in OPEN:
            depth += 1
        elif t in CLOSE:
            if depth == 0:
                return j
            depth -= 1
        elif depth == 0 and (t == '+' or t in STOP_RIGHT):
            return j
        j += 1
    return j

def operand_start(toks, j):
    """j = last token index of operand; walk left to its first token"""
    depth = 0
    i = j
    while i >= 0:
        t = toks[i][1]
        if t in CLOSE:
            depth += 1
        elif t in OPEN:
            if depth == 0:
                return i + 1
            depth -= 1
        elif depth == 0 and (t == '+' or t in STOP_LEFT or (toks[i][0] == 'id' and t in ('return', 'throw', 'case', 'new') and False)):
            return i + 1
        i -= 1
    return 0

def split_args(toks, a, b):
    """split tokens a..b (exclusive) by top-level commas"""
    parts, depth, start = [], 0, a
    for k in range(a, b):
        t = toks[k][1]
        if t in OPEN: depth += 1
        elif t in CLOSE: depth -= 1
        elif t == ',' and depth == 0:
            parts.append((start, k)); start = k + 1
    if start < b:
        parts.append((start, b))
    return parts

def classify(toks, a, b, src):
    """operand tokens a..b -> piece"""
    text = src[toks[a][2]:toks[b - 1][3]]
    if b - a == 1 and toks[a][0] == 'str':
        return {'t': 'lit', 'v': java_unescape(toks[a][1]), 'code': text}
    # Messages.get("key"[, args])
    if b - a >= 6 and toks[a][1] == 'Messages' and toks[a + 1][1] == '.' and toks[a + 2][1] == 'get' and toks[a + 3][1] == '(' \
            and toks[a + 4][0] == 'str' and toks[b - 1][1] == ')':
        # make sure the closing paren matches the call
        depth = 0
        for k in range(a + 3, b):
            if toks[k][1] in OPEN: depth += 1
            elif toks[k][1] in CLOSE:
                depth -= 1
                if depth == 0 and k != b - 1:
                    break
        else:
            if toks[a + 5][1] in (')', ','):
                args = []
                if toks[a + 5][1] == ',':
                    for s, e in split_args(toks, a + 6, b - 1):
                        args.append(src[toks[s][2]:toks[e - 1][3]])
                return {'t': 'key', 'k': java_unescape(toks[a + 4][1]), 'args': args, 'code': text}
    return {'t': 'expr', 'code': text}

STRING_GETTERS = re.compile(r'(getGameDisplayName|getName|getMessage|getLocalizedMessage|getAbsolutePath|getFileName|getFilename|getRom|getPath|getLabel|getDisplayName|'
                            r'getTitle|getText|toString|getUrl|getDescription|getTableName|getVersion|getExtVersion|getUuid|getId\w+Name|getValue|name)\(\)$')
SIMPLE_STRING_VARS = re.compile(r'^(name|msg|message|title|text|label|path|filename|fileName|url|rom|version|value|screenName|term|key|type|target|source|reason|error|action|status)$', re.I)

def wrap(code):
    c = code.strip()
    if c.startswith('"') or STRING_GETTERS.search(c) or SIMPLE_STRING_VARS.match(c) or c.startswith('String.') or c.startswith('Messages.get(') \
            or c.startswith('StringUtils.') or c.startswith('FilenameUtils.') or c.startswith('FrontendUtil.'):
        return c
    return f'String.valueOf({c})'

def slug(s, maxw=7):
    s = re.sub(r'\{\d+\}', ' ', s)
    words = re.findall(r'[a-z0-9]+', s.lower())
    return '_'.join(words[:maxw]) or 'message'

def main():
    out = sys.argv[1]
    pkgs = sys.argv[2:]
    bundles = {l: load_bundle(l) for l in LANGS}
    files = []
    for dp, dn, fns in os.walk(UI):
        rel = os.path.relpath(dp, UI)
        top = '_root' if rel == '.' else rel.split(os.sep)[0]
        if pkgs and top not in pkgs:
            continue
        for fn in fns:
            if fn.endswith('.java'):
                files.append(os.path.join(dp, fn))
    chains = []
    for p in sorted(files):
        src = open(p, encoding='utf-8', newline='').read()
        toks = tokenize(src)
        seen = set()
        for idx, t in enumerate(toks):
            if not (t[1] == 'Messages' and idx + 2 < len(toks) and toks[idx + 1][1] == '.' and toks[idx + 2][1] == 'get'):
                continue
            # find enclosing chain
            e = operand_end(toks, idx)
            s = operand_start(toks, e - 1)
            # extend left over '+'
            ops = [(s, e)]
            while s - 1 >= 0 and toks[s - 1][1] == '+' and toks[s - 2][1] not in ('+',):
                ps = operand_start(toks, s - 2)
                ops.insert(0, (ps, s - 1))
                s = ps
            j = e
            while j < len(toks) and toks[j][1] == '+':
                ne = operand_end(toks, j + 1)
                ops.append((j + 1, ne))
                j = ne
            if len(ops) < 2:
                continue
            span = (toks[ops[0][0]][2], toks[ops[-1][1] - 1][3])
            if span in seen:
                continue
            seen.add(span)
            pieces = [classify(toks, a, b, src) for a, b in ops]
            # string concat only if a string literal or Messages call is among the first two operands (java left-to-right)
            # peel whitespace-only literals at both ends
            lead, trail = [], []
            while pieces and pieces[0]['t'] == 'lit' and not pieces[0]['v'].strip():
                lead.append(pieces.pop(0))
            while pieces and pieces[-1]['t'] == 'lit' and not pieces[-1]['v'].strip():
                trail.insert(0, pieces.pop())
            nkeys = sum(1 for x in pieces if x['t'] == 'key')
            nexpr = sum(1 for x in pieces if x['t'] == 'expr')
            if nkeys == 0 or (nexpr == 0 and nkeys < 2):
                continue
            if any(x['t'] == 'key' and x['k'] not in bundles['en'] for x in pieces):
                continue  # dynamic / unknown key
            line = src.count('\n', 0, span[0]) + 1
            linetxt = src.split('\n')[line - 1]
            is_log = bool(re.search(r'\bLOG\.(info|warn|error|debug|trace)\(', linetxt))
            # build templates
            args, tmpl = [], {l: '' for l in LANGS}
            for x in pieces:
                if x['t'] == 'lit':
                    for l in LANGS:
                        tmpl[l] += x['v'].replace("'", "''").replace('{', "'{'").replace('}', "'}'")
                elif x['t'] == 'expr':
                    n = len(args); args.append(wrap(x['code']))
                    for l in LANGS:
                        tmpl[l] += '{' + str(n) + '}'
                else:
                    base = len(args)
                    args += [a.strip() for a in x['args']]
                    for l in LANGS:
                        v = bundles[l].get(x['k'], bundles['en'][x['k']])
                        if x['args']:
                            v = re.sub(r'\{(\d+)', lambda m: '{' + str(int(m.group(1)) + base), v)
                        else:
                            v = v.replace("'", "''")
                        tmpl[l] += v
            chains.append({
                'file': os.path.relpath(p, UI).replace(os.sep, '/'), 'line': line, 'start': span[0], 'end': span[1],
                'orig': src[span[0]:span[1]], 'log': is_log,
                'lead': [x['code'] for x in lead], 'trail': [x['code'] for x in trail],
                'keys': [x['k'] for x in pieces if x['t'] == 'key'], 'args': args, 'tmpl': tmpl, 'pieces': pieces,
            })
    # assign keys: identical en+de template -> same key
    by_t, used = {}, set(bundles['en'])
    for c in chains:
        sig = (c['tmpl']['en'], c['tmpl']['de'], len(c['args']))
        if sig not in by_t:
            k = 'msg.' + slug(c['tmpl']['en'])
            base, n = k, 2
            while k in used:
                k = f'{base}_{n}'; n += 1
            used.add(k)
            by_t[sig] = k
        c['key'] = by_t[sig]
    json.dump(chains, open(out, 'w', encoding='utf-8'), ensure_ascii=False, indent=1)
    print(f"{len(chains)} chains, {len(by_t)} distinct messages, {len({c['file'] for c in chains})} files")

if __name__ == '__main__':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
    main()
