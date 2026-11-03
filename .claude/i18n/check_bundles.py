"""usage: python -I check_bundles.py  -> keys missing per language (vs. en) and duplicate keys per bundle"""
import os, re, sys, io, collections
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')
MSG = r"C:\workspace\vpin-studio\vpin-studio-commons\src\main\resources\de\mephisto\vpin\ui\messages"
key_re = re.compile(r'^([^#!\s=:][^=:]*?)\s*[=:]', re.M)
def keys(fn):
    return key_re.findall(open(os.path.join(MSG, fn), encoding='utf-8').read())
en = keys('messages.properties')
ok = True
for lang in ['en', 'de', 'es', 'fr', 'it', 'pt']:
    k = keys('messages.properties' if lang == 'en' else f'messages_{lang}.properties')
    dupes = [x for x, c in collections.Counter(k).items() if c > 1]
    missing = sorted(set(en) - set(k))
    if dupes or missing:
        ok = False
    print(f"{lang}: missing={len(missing)} dupes={len(dupes)}")
    for m in missing[:50]:
        print("   missing:", m)
    for d in dupes[:50]:
        print("   dupe:", d)
sys.exit(0 if ok else 1)
