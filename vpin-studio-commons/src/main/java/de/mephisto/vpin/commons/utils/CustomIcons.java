package de.mephisto.vpin.commons.utils;

import org.kordamp.ikonli.Ikon;

public enum CustomIcons implements Ikon {
    VPX_ICON('\ue904'),
    FX_ICON('\ue901'),
    FX3_ICON('\ue900'),
    FUTUREPINBALL_ICON('\ue903'),
    PINBALLM_ICON('\ue902');

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