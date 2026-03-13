package de.mephisto.vpin.server.games;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import de.mephisto.vpin.server.mame.MameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

  @Autowired
  private MameService mameService;

  @Bean
  public Module gameSerializerModule() {
    SimpleModule module = new SimpleModule("GameModule");
    module.setSerializerModifier(new BeanSerializerModifier() {
      @SuppressWarnings("unchecked")
      @Override
      public JsonSerializer<?> modifySerializer(SerializationConfig config, BeanDescription beanDesc, JsonSerializer<?> serializer) {
        if (beanDesc.getBeanClass() == Game.class) {
          return new GameSerializer(mameService, (JsonSerializer<Object>) serializer);
        }
        return serializer;
      }
    });
    return module;
  }
}
