package de.mephisto.vpin.server.games;


import de.mephisto.vpin.server.backups.BackupService;
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
import org.springframework.context.annotation.Lazy;

@Configuration
public class JacksonConfig {

    @Lazy
    @Autowired
    private VPinMameService vPinMameService;

    @Lazy
    @Autowired
    private BackupService backupService;

    @Bean
    public JacksonModule gameSerializerModule() {
        SimpleModule module = new SimpleModule("GameModule");
        module.setSerializerModifier(new ValueSerializerModifier() {
            @SuppressWarnings("unchecked")
            @Override
            public ValueSerializer<?> modifySerializer(SerializationConfig config, BeanDescription.Supplier beanDesc, ValueSerializer<?> serializer) {
                if (beanDesc.getBeanClass() == Game.class) {
                    return new GameSerializer(vPinMameService, backupService, (ValueSerializer<Object>) serializer);
                }
                return serializer;
            }
        });
        return module;
        }
    }
