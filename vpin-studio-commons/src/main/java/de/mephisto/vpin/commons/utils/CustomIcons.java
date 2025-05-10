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
    NFOZZY_ICON('\ue90d');
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