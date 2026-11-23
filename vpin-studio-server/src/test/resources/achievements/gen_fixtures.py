"""Generate golden fixtures from the Python Achievement Watcher for the Java port.

Usage: python gen_fixtures.py <aw_repo> <studio_resources> <out_dir> rom1 rom2 ...
Writes per ROM: <rom>.audits.json (decoded audits from AW), <rom>.ach.json (generated rules),
<rom>.fields.json (parsed map fields) and levels.json / badges.json.
"""
import json
import os
import shutil
import sys
import tempfile

aw, res, out = sys.argv[1], sys.argv[2], sys.argv[3]
roms = sys.argv[4:]
sys.path.insert(0, aw)

from core.config import AppConfig  # noqa: E402
from core import watcher_core  # noqa: E402
from core.badges import compute_player_level, BADGE_DEFINITIONS  # noqa: E402

base = tempfile.mkdtemp(prefix="awfix_")
maps_dst = os.path.join(base, "tools", "NVRAM_Maps")
os.makedirs(maps_dst, exist_ok=True)
shutil.copytree(os.path.join(res, "maps", "maps"), os.path.join(maps_dst, "maps"))
shutil.copy(os.path.join(res, "maps", "index.json"), os.path.join(maps_dst, "index.json"))
shutil.copy(os.path.join(res, "maps", "romnames.json"), os.path.join(maps_dst, "romnames.json"))

cfg = AppConfig()
cfg.BASE = base
cfg.NVRAM_DIR = os.path.join(res, "nvrams")


class _Sig:
    def emit(self, *a, **k):
        pass


class Bridge:
    def __getattr__(self, name):
        return _Sig()


w = watcher_core.Watcher(cfg, Bridge())
w.INDEX = json.load(open(os.path.join(maps_dst, "index.json"), encoding="utf-8"))
w.ROMNAMES = json.load(open(os.path.join(maps_dst, "romnames.json"), encoding="utf-8"))
os.makedirs(os.path.join(base, "Achievements", "rom_specific_achievements"), exist_ok=True)
os.makedirs(out, exist_ok=True)

for rom in roms:
    fields, src = w.load_map_for_rom(rom)
    audits, notes, _ = w.read_nvram_audits_with_autofix(rom)
    rule_path = os.path.join(base, "Achievements", "rom_specific_achievements", f"{rom}.ach.json")
    rules = json.load(open(rule_path, encoding="utf-8")) if os.path.exists(rule_path) else {"rules": []}
    with open(os.path.join(out, f"{rom}.fields.json"), "w", encoding="utf-8") as f:
        json.dump([{"label": x.get("label"), "section": x.get("section", "")} for x in (fields or [])], f, indent=1, ensure_ascii=False)
    with open(os.path.join(out, f"{rom}.audits.json"), "w", encoding="utf-8") as f:
        json.dump(audits, f, indent=1, ensure_ascii=False)
    with open(os.path.join(out, f"{rom}.ach.json"), "w", encoding="utf-8") as f:
        json.dump(rules, f, indent=1, ensure_ascii=False)
    print(rom, "map:", src and os.path.relpath(src, maps_dst), "fields:", len(fields or []), "audits:", len(audits), "rules:", len(rules["rules"]))

levels = []
for total in [0, 1, 9, 10, 24, 25, 199, 200, 306, 399, 400, 1999, 2000, 2001, 2400, 4000, 10000, 12500]:
    state = {"global": {"__global__": [{"title": f"t{i}"} for i in range(total)]}, "session": {}}
    lv = compute_player_level(state)
    levels.append({"total": total, **lv})
json.dump(levels, open(os.path.join(out, "levels.json"), "w", encoding="utf-8"), indent=1, ensure_ascii=False)
json.dump([{"id": b[0], "name": b[2], "description": b[3]} for b in BADGE_DEFINITIONS],
          open(os.path.join(out, "badges.json"), "w", encoding="utf-8"), indent=1, ensure_ascii=False)
shutil.rmtree(base, ignore_errors=True)
