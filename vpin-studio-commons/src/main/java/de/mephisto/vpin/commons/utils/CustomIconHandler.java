package de.mephisto.vpin.commons.utils;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.IkonHandler;
import de.mephisto.vpin.commons.fx.*;
import java.io.InputStream;
import java.net.URL;

public class CustomIconHandler implements IkonHandler{
    private static final String FONT_RESOURCE = "VpinStudio.ttf";
    private static final String FONT_FAMILY = "VPinStudio"; // your font name

        @Override
        public boolean supports (String description){
            return description != null && description.startsWith("customicon-");
        }

        @Override
        public Ikon resolve (String description){
            String name = description.substring("customicon-".length()); // removes "myikon-"
            return CustomIcons.valueOf(name.toUpperCase());
        }

    @Override
    public URL getFontResource() {
        URL url = ServerFX.class.getResource(FONT_RESOURCE);
        if (url == null) {
            throw new IllegalStateException("Font resource not found!");
        }
        return url;
    }


    @Override
    public InputStream getFontResourceAsStream() {
        InputStream is = ServerFX.class.getResourceAsStream(FONT_RESOURCE);
        if (is == null) {
            throw new IllegalStateException("Font resource stream not found!");
        }
        return is;
    }

    @Override
    public String getFontFamily() {
        return FONT_FAMILY; // <- Must match font internal name!
    }

        private Object font;

        @Override
        public Object getFont () {
            return font;
        }

        @Override
        public void setFont (Object font){
            this.font = font;
        }
}
