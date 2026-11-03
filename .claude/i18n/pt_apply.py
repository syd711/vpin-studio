"""
usage: python -I pt_apply.py batches/pt_european.py [--check-only]
Replaces pt values (PT = {key: value}) in messages_pt.properties in place and lists values of the
sweep keys (earlier batches + msg.*) that still contain Brazilian Portuguese markers.
"""
import re, sys, io, os

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from concat_extract import load_bundle  # noqa

PATH = r"C:\workspace\vpin-studio\vpin-studio-commons\src\main\resources\de\mephisto\vpin\ui\messages\messages_pt.properties"


def esc(v):
    if re.search(r'\{\d', v):
        v = v.replace("'", "''")
    return v.replace('\\', '\\\\').replace('\n', '\\n')


if '--check-only' not in sys.argv:
    ns = {}
    exec(open(sys.argv[1], encoding='utf-8').read(), ns)
    PT = ns['PT']
    raw = open(PATH, encoding='utf-8', newline='').read()
    nl = '\r\n' if '\r\n' in raw else '\n'
    lines = raw.split(nl)
    done = set()
    for i, line in enumerate(lines):
        m = re.match(r'([^#!=:\s][^=:\s]*)=', line)
        if m and m.group(1) in PT:
            lines[i] = f"{m.group(1)}={esc(PT[m.group(1)])}"
            done.add(m.group(1))
    print('replaced', len(done), 'not found', sorted(set(PT) - done))
    open(PATH, 'w', encoding='utf-8', newline='').write(nl.join(lines))

pt = load_bundle('pt')
keys = set()
for f in ['tables', 'small_packages', 'cards_playlists_vpxz', 'preferences_util_vps', 'preferences_fixup', 'missing_ternary_keys']:
    n = {}
    exec(open(os.path.join(HERE, 'batches', f + '.py'), encoding='utf-8').read(), n)
    keys |= {r[0] for r in n.get('NEW_KEYS', [])} | set(n.get('MISSING', {}))
keys |= {k for k in pt if k.startswith('msg.')}
BR = re.compile(r'\b(arquivos?|salvar|excluir|exclu\w+|tela|telas|mídias?|baixar|baixad\w+|usuári\w+|você|gerenciador|compartilh\w+|'
                r'assinaturas?|registro|padrão|digite|solte|pressione|em um|em uma|\w+ando|\w+endo|\w+indo|ele|ela|seus|suas)\b', re.I)
IGNORE = {'quando', 'grande', 'fundo', 'segundo', 'mundo', 'comando', 'comandos'}
for k in sorted(keys):
    v = pt.get(k, '')
    hits = [h for h in BR.findall(v) if h.lower() not in IGNORE]
    if hits:
        print(k, '|', hits, '|', v[:130].replace('\n', '\\n'))
