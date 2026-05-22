package de.mephisto.vpin.server.games;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.ser.PropertyWriter;
import tools.jackson.databind.ser.bean.BeanSerializerBase;
import de.mephisto.vpin.server.vpinmame.VPinMameService;

import java.util.Iterator;

public class GameSerializer extends ValueSerializer<Game> {

    private final VPinMameService vPinMameService;
    private final ValueSerializer<Object> defaultSerializer;

    public GameSerializer(VPinMameService vPinMameService, ValueSerializer<Object> defaultSerializer) {
        this.vPinMameService = vPinMameService;
        this.defaultSerializer = defaultSerializer;
    }

    @Override
    public void resolve(SerializationContext context) {
        defaultSerializer.resolve(context);
    }

    @Override
    public void serialize(Game game, JsonGenerator gen, SerializationContext context) throws JacksonException {
        gen.writeStartObject();

        if (defaultSerializer instanceof BeanSerializerBase) {
            BeanSerializerBase beanSerializer = (BeanSerializerBase) defaultSerializer;
            Iterator<PropertyWriter> it = beanSerializer.properties();
            while (it.hasNext()) {
                PropertyWriter writer = it.next();
                try {
                    writer.serializeAsProperty(game, gen, context);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        gen.writeBooleanProperty("romExists", vPinMameService.isRomExists(game));

        gen.writeEndObject();
    }
}