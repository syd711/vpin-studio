Subset of the NVRAM maps (github.com/tomlogic/pinmame-nvram-maps, see the LICENSE files) used by the
achievement golden tests, so the tests do not depend on the gitignored `resources/maps` folder.

After a map update in `resources/maps`, run from `vpin-studio-server`:

    python src/test/resources/achievements/copy_test_maps.py ../resources/maps src/test/resources/achievements/maps
    python src/test/resources/achievements/gen_fixtures.py <vpx-achievement-watcher repo> ../resources src/test/resources/achievements/golden afm_113b mm_109c tz_94h taf_l7 cc_13 tom_14h apollo13 lw3_208 baywatch twd_160 godzilla mb_106b ss_15 xenon centaur sttng_l7 bop_l7 rs_l6 gladiatr bk2k_l4

and commit the changed maps and golden fixtures together.
