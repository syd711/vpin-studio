import json, shutil, os, sys
src, dst = sys.argv[1], sys.argv[2]
roms = "afm_113b mm_109c tz_94h taf_l7 cc_13 tom_14h apollo13 lw3_208 baywatch twd_156 godzilla mb_106b ss_15 xenon centaur sttng_l7 bop_l7 rs_l6 gladiatr bk2k_l4".split()
idx = json.load(open(os.path.join(src, "index.json")))
out = {"_note": "Subset of resources/maps/index.json for the achievement golden tests (see README.md)"}
for r in roms:
    out[r] = idx[r]
    p = idx[r]
    os.makedirs(os.path.dirname(os.path.join(dst, p)), exist_ok=True)
    shutil.copy(os.path.join(src, p), os.path.join(dst, p))
json.dump(out, open(os.path.join(dst, "index.json"), "w"), indent=2)
if os.path.exists(os.path.join(dst, "platforms")):
    shutil.rmtree(os.path.join(dst, "platforms"))
shutil.copytree(os.path.join(src, "platforms"), os.path.join(dst, "platforms"))
for f in ["LICENSE-DbCL", "LICENSE-ODbL.md"]:
    shutil.copy(os.path.join(src, f), os.path.join(dst, f))
