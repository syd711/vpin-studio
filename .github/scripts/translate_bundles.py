#!/usr/bin/env python3
"""
VPin Studio i18n — Translate all client bundle strings
Usage: ANTHROPIC_API_KEY=sk-ant-... python3 translate_bundles.py

Translates messages.properties -> messages_{fr,es,pt,it}.properties
using Claude claude-sonnet-4-6 in batches of 60 strings.
Results are committed directly to GitHub via the API.
"""

import os, json, time, sys, base64, urllib.request, urllib.error

# ── Config ──────────────────────────────────────────────────────────────────
ANTHROPIC_KEY = os.environ.get("ANTHROPIC_API_KEY", "")
GITHUB_TOKEN  = os.environ.get("GITHUB_TOKEN", "")
REPO          = "syd711/vpin-studio"
BRANCH        = "main"
BUNDLE_DIR    = "vpin-studio-commons/src/main/resources/de/mephisto/vpin/ui/messages"
_ALL_LANGS    = {"fr": "French", "es": "Spanish", "pt": "Portuguese", "it": "Italian"}
_lang_env     = os.environ.get("LANGUAGES", "fr,es,pt,it").split(",")
LANGS         = {k: v for k, v in _ALL_LANGS.items() if k.strip() in _lang_env}
BATCH_SIZE    = int(os.environ.get("BATCH_SIZE", "60"))
MODEL         = "claude-sonnet-4-6"

if not ANTHROPIC_KEY:
    sys.exit("ERROR: Set ANTHROPIC_API_KEY environment variable.")

# ── Helpers ──────────────────────────────────────────────────────────────────
def gh_get(path):
    url = f"https://api.github.com/repos/{REPO}/contents/{path}"
    req = urllib.request.Request(url, headers={
        "Authorization": f"token {GITHUB_TOKEN}",
        "Accept": "application/vnd.github.v3+json"
    })
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read())

def gh_raw(path):
    d = gh_get(path)
    return base64.b64decode(d["content"].replace("\n","")), d["sha"]

def gh_put(path, content_bytes, sha, message):
    url = f"https://api.github.com/repos/{REPO}/contents/{path}"
    payload = json.dumps({
        "message": message,
        "content": base64.b64encode(content_bytes).decode(),
        "sha": sha,
        "branch": BRANCH
    }).encode()
    req = urllib.request.Request(url, data=payload, headers={
        "Authorization": f"token {GITHUB_TOKEN}",
        "Accept": "application/vnd.github.v3+json",
        "Content-Type": "application/json"
    }, method="PUT")
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read())

def parse_props(text):
    d, order = {}, []
    for line in text.splitlines():
        line = line.rstrip("\n")
        order.append(line)
        if line and not line.startswith("#") and "=" in line:
            k, v = line.split("=", 1)
            d[k.strip()] = v
    return d, order

def call_claude(system_prompt, user_content, retries=3):
    for attempt in range(retries):
        try:
            payload = json.dumps({
                "model": MODEL,
                "max_tokens": 4000,
                "system": system_prompt,
                "messages": [{"role": "user", "content": user_content}]
            }).encode()
            req = urllib.request.Request(
                "https://api.anthropic.com/v1/messages",
                data=payload,
                headers={
                    "x-api-key": ANTHROPIC_KEY,
                    "anthropic-version": "2023-06-01",
                    "Content-Type": "application/json"
                },
                method="POST"
            )
            with urllib.request.urlopen(req, timeout=120) as r:
                data = json.loads(r.read())
            return data["content"][0]["text"]
        except Exception as e:
            if attempt < retries - 1:
                wait = 5 * (attempt + 1)
                print(f"  ⚠ Attempt {attempt+1} failed ({e}), retrying in {wait}s…")
                time.sleep(wait)
            else:
                raise

def translate_batch(values, lang_name):
    system = f"""You are a professional software localizer translating Java .properties values into {lang_name}.
Rules:
1. Return ONLY a JSON array of translated strings, same order and count as the input array.
2. Preserve ALL placeholders exactly: {{0}} {{1}} {{2}} \\n \\\\ etc.
3. Keep these technical terms in English: VPX, DMD, ROM, PUP Pack, DirectB2S, DOF, ALT Color, ALT Sound, VPin Studio, Discord, Backglass, Popper, Frontend, MAME, NVRAM, iScored, VPin Mania, VPXZ, NVOffset, FlexDMD, UltraDMD, Freezy, Serum, PinVol, DOFLinx, B2S, PinUP, PINemHi.
4. Keep URLs, file paths, and keyboard shortcuts (Ctrl+X, F2, etc.) unchanged.
5. Use natural, idiomatic {lang_name} phrasing for a desktop application UI.
6. Strings starting with - or • are list items; preserve the prefix.
7. Output ONLY the raw JSON array — no markdown fences, no explanation."""
    raw = call_claude(system, json.dumps(values))
    clean = raw.strip().lstrip("```json").lstrip("```").rstrip("```").strip()
    result = json.loads(clean)
    if not isinstance(result, list) or len(result) != len(values):
        raise ValueError(f"Length mismatch: expected {len(values)}, got {len(result) if isinstance(result,list) else type(result)}")
    return result

# ── Main ─────────────────────────────────────────────────────────────────────
def main():
    print("═" * 60)
    print("VPin Studio i18n — Translation Script")
    print("═" * 60)

    # Load EN bundle
    print("\n▸ Loading EN bundle…")
    en_bytes, _ = gh_raw(f"{BUNDLE_DIR}/messages.properties")
    en_map, en_order = parse_props(en_bytes.decode("utf-8"))
    print(f"  {len(en_map)} keys")

    # Load DE bundle (to know keep-EN keys)
    print("▸ Loading DE bundle…")
    de_bytes, _ = gh_raw(f"{BUNDLE_DIR}/messages_de.properties")
    de_map, de_order = parse_props(de_bytes.decode("utf-8"))
    keep_en = set(k for k in de_map if de_map[k] == en_map.get(k, ""))
    print(f"  {len(de_map)} keys, {len(keep_en)} intentionally kept in English")

    # Load existing locale files
    existing = {}
    shas = {}
    for lang in LANGS:
        print(f"▸ Loading {lang} bundle…")
        b, sha = gh_raw(f"{BUNDLE_DIR}/messages_{lang}.properties")
        existing[lang] = parse_props(b.decode("utf-8"))[0]
        shas[lang] = sha
        already = sum(1 for k,v in existing[lang].items() if v != en_map.get(k,"") and k not in keep_en)
        print(f"  {already} keys already translated")

    # Keys that need translating
    splash_keys = set(k for k in de_map if k.startswith("studio.splash.fact."))
    needs = [k for k in de_map if k not in keep_en and k not in splash_keys]
    print(f"\n▸ Keys to translate: {len(needs)}")

    # Unique EN values (many keys share same value)
    # For each language, only translate values not already translated
    # Build per-lang unique-value sets
    val_trans = {lang: {} for lang in LANGS}

    # Pre-populate with existing translations
    for lang in LANGS:
        for k in needs:
            en_v = en_map.get(k, "")
            if k in existing[lang] and existing[lang][k] != en_v:
                val_trans[lang][en_v] = existing[lang][k]

    # Find values still needing translation (at least one lang missing)
    all_unique_vals = list(dict.fromkeys(en_map[k] for k in needs if k in en_map))
    still_needed = [v for v in all_unique_vals if any(v not in val_trans[lang] for lang in LANGS)]
    print(f"▸ Unique values still needing translation: {len(still_needed)}")

    # Batch translate
    batches = [still_needed[i:i+BATCH_SIZE] for i in range(0, len(still_needed), BATCH_SIZE)]
    print(f"▸ {len(batches)} batches of up to {BATCH_SIZE}\n")

    for i, batch in enumerate(batches):
        pct = int((i+1)*100/len(batches))
        bar = "█" * (pct // 5) + "░" * (20 - pct // 5)
        print(f"  Batch {i+1:3d}/{len(batches)} [{bar}] {pct}%", end="", flush=True)

        for lang, lang_name in LANGS.items():
            to_do = [v for v in batch if v not in val_trans[lang]]
            if not to_do:
                continue
            results = translate_batch(to_do, lang_name)
            for v, t in zip(to_do, results):
                if t and t != v:
                    val_trans[lang][v] = t

        print(f"  ✓")
        time.sleep(0.5)  # be polite to the API

    # Apply translations and build output files
    print("\n▸ Building output files…")
    for lang, lang_name in LANGS.items():
        out_lines = [f"# VPin Studio Client - {lang_name}"]

        for raw_line in de_order:
            if not raw_line or raw_line.startswith("#"):
                out_lines.append(raw_line)
                continue
            if "=" not in raw_line:
                out_lines.append(raw_line)
                continue

            k, de_v = raw_line.split("=", 1)
            k = k.strip()
            en_v = en_map.get(k, de_v)

            if k in keep_en:
                out_lines.append(f"{k}={en_v}")
            elif k in splash_keys and k in existing[lang] and existing[lang][k] != en_v:
                # Keep existing splash fact translation
                out_lines.append(f"{k}={existing[lang][k]}")
            elif en_v in val_trans[lang]:
                out_lines.append(f"{k}={val_trans[lang][en_v]}")
            else:
                out_lines.append(f"{k}={en_v}")

        content = "\n".join(out_lines).encode("utf-8")
        translated_count = sum(1 for k,v in de_map.items()
                               if k not in keep_en and k in en_map
                               and val_trans[lang].get(en_map[k], en_map[k]) != en_map[k])
        total = len([k for k in de_map if k not in keep_en])
        print(f"  {lang}: {translated_count}/{total} translated ({translated_count*100//total}%)")

        # Push to GitHub
        print(f"  Pushing messages_{lang}.properties ({len(content)//1024}KB)…", end="", flush=True)
        result = gh_put(
            f"{BUNDLE_DIR}/messages_{lang}.properties",
            content,
            shas[lang],
            f"i18n: translate all UI strings to {lang_name} via Claude\n\n"
            f"Machine-translated {translated_count} unique string values using claude-sonnet-4-6.\n"
            f"Technical terms, placeholders, and URLs preserved unchanged.\n"
            f"Splash facts use existing human translations."
        )
        shas[lang] = result["content"]["sha"]
        print(" ✓")

    print("\n✅ Done! All 4 locale files pushed to GitHub.")
    print(f"   View: https://github.com/{REPO}/commits/{BRANCH}")

if __name__ == "__main__":
    main()
