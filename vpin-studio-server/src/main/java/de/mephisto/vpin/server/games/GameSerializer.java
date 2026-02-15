package de.mephisto.vpin.server.games;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.ser.std.BeanSerializerBase;
import de.mephisto.vpin.server.vpx.FolderLookupService;

import java.io.IOException;
import java.util.Iterator;

/**
 * Custom serializer for Game that delegates to Jackson's default bean serialization
 * (which respects @JsonIgnore) and allows adding custom fields or transformations.
 */
public class GameSerializer extends JsonSerializer<Game> implements ResolvableSerializer {

  private final FolderLookupService folderLookupService;
  private final JsonSerializer<Object> defaultSerializer;

  public GameSerializer(FolderLookupService folderLookupService, JsonSerializer<Object> defaultSerializer) {
    this.folderLookupService = folderLookupService;
    this.defaultSerializer = defaultSerializer;
  }

  @Override
  public void resolve(SerializerProvider provider) throws JsonMappingException {
    if (defaultSerializer instanceof ResolvableSerializer) {
      ((ResolvableSerializer) defaultSerializer).resolve(provider);
    }
  }

  @Override
  public void serialize(Game game, JsonGenerator gen, SerializerProvider provider) throws IOException {
    gen.writeStartObject();

    // Write all default (non-@JsonIgnore) fields automatically
    if (defaultSerializer instanceof BeanSerializerBase) {
      BeanSerializerBase beanSerializer = (BeanSerializerBase) defaultSerializer;
      Iterator<PropertyWriter> it = beanSerializer.properties();
      while (it.hasNext()) {
        PropertyWriter writer = it.next();
        try {
          writer.serializeAsField(game, gen, provider);
        }
        catch (Exception e) {
          throw new IOException("Failed to serialize field: " + writer.getName(), e);
        }
      }
    }

    // Add custom fields here
    gen.writeBooleanField("romExists", folderLookupService.isRomExists(game));

    gen.writeEndObject();
  }
}
