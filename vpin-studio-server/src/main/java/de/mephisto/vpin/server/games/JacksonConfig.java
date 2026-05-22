package de.mephisto.vpin.server.games;


import tools.jackson.databind.BeanDescription;
import tools.jackson.databind.JacksonModule;
import tools.jackson.databind.SerializationConfig;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.ValueSerializerModifier;
import de.mephisto.vpin.server.vpinmame.VPinMameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Autowired
    private VPinMameService vPinMameService;

    @Bean
    public JacksonModule gameSerializerModule() {
        SimpleModule module = new SimpleModule("GameModule");
        module.setSerializerModifier(new ValueSerializerModifier() {
            @SuppressWarnings("unchecked")
            public ValueSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, ValueSerializer<?> serializer) {
                if (beanDesc.getBeanClass() == Game.class) {
                    return new GameSerializer(vPinMameService, (ValueSerializer<Object>) serializer);
                }
                return serializer;
            }
        });
        return module;
        }
    }
