package de.mephisto.vpin.commons.utils;

import org.kordamp.ikonli.Ikon;

public enum CustomIcons implements Ikon {
    //If the font (VpinStudio.ttf) is updated, these values may need to change.
    //Font was created at https://icomoon.io/app/
    VPX_ICON('\ue904'),
    FX_ICON('\ue901'),
    FX3_ICON('\ue900'),
    FUTUREPINBALL_ICON('\ue903'),
    PINBALLM_ICON('\ue902'),
    RECENTLYPLAYED_ICON('\ue906'),
    VPW_ICON('\ue905'),
    TOP_10_ICON('\ue907'),
    PUP_ICON('\ue908'),
    ADULT_ICON('\ue909'),
    SOCCER_ICON('\ue90a'),
    STAR_WARS_ICON('\ue90b'),
    MUSIC_ICON('\ue911'),
    MOVIE_ICON('\ue90c'),
    NFOZZY_ICON('\ue90d'),
    SUPERHERO_ICON('\ue90e'),
    MAME_ICON('\ue90f'),
    BALLY_ICON('\ue914'),
    ATARI_ICON('\ue910'),
    SEGA_ICON('\ue912'),
    ZACCARIA_ICON('\ue913'),
    DATAEAST_ICON('\ue915'),
    MIDWAY_ICON('\ue916'),
    GOTTLIEB_ICON('\ue917'),
    WILLIAMS_ICON('\ue918'),
    CHICAGO_ICON('\ue91b'),
    STERN_ICON('\ue919'),
    FIFTIES_ICON('\ue921'),
    SIXTIES_ICON('\ue920'),
    SEVENTIES_ICON('\ue91f'),
    EIGHTIES_ICON('\ue91e'),
    NINETIES_ICON('\ue91d'),
    AUGHTS_ICON('\ue91a'),
    VR_ICON('\ue922'),
    CAPCOM_ICON('\ue923'),
    BW_ICON('\ue926'),
    KIDS_ICON('\ue927'),
    SS_ICON('\ue929'),
    EM_ICON('\ue928'),
    MOD_ICON('\ue925')
   ;

    private final char code;

    CustomIcons(char code) {
        this.code = code;
    }

    @Override
    public String getDescription() {
        return "customicon-" + name().toLowerCase(); // returns "CUSTOM_ICON1", etc.
    }

    @Override
    public int getCode() {
        return code;
    }
}