package de.mephisto.vpin.server.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ServerMessagesTest {

  private static final List<String> LANGUAGES = List.of("de", "es", "fr", "it", "pt");
  private static final Pattern PLACEHOLDER = Pattern.compile("\\{(\\d+)");

  private final Locale defaultLocale = Locale.getDefault();

  @AfterEach
  void restoreDefaultLocale() {
    Locale.setDefault(defaultLocale);
  }

  @Test
  void englishDoesNotFallBackToJvmDefaultLocale() {
    // the JVM default locale is the local UI language on a cabinet, not the language the client asked for
    Locale.setDefault(Locale.GERMAN);
    assertThat(ServerMessages.get("mania.error.disabled", Locale.ENGLISH)).isEqualTo("VPin Mania is disabled.");
    assertThat(ServerMessages.get("mania.error.disabled", Locale.GERMAN)).isEqualTo("VPin Mania ist deaktiviert.");
  }

  @Test
  void regionalLocaleFallsBackToLanguage() {
    assertThat(ServerMessages.get("mania.error.disabled", Locale.forLanguageTag("pt-PT"))).isEqualTo("O VPin Mania está desativado.");
  }

  @Test
  void formatsArguments() {
    assertThat(ServerMessages.get("rom2altsound.error.no_result", Locale.FRENCH, "afm_113b"))
        .isEqualTo("rom2altsound n'a renvoyé aucun résultat pour \"afm_113b\".");
  }

  @Test
  void parseLocale() {
    assertThat(ServerMessages.parseLocale("de,en-US;q=0.9").getLanguage()).isEqualTo("de");
    assertThat(ServerMessages.parseLocale(null)).isEqualTo(Locale.ENGLISH);
    assertThat(ServerMessages.parseLocale("")).isEqualTo(Locale.ENGLISH);
  }

  @Test
  void bundlesHaveSameKeysAndPlaceholders() throws IOException {
    Properties en = load("messages/messages.properties");
    for (String lang : LANGUAGES) {
      Properties bundle = load("messages/messages_" + lang + ".properties");
      assertThat(bundle.stringPropertyNames()).as("keys of " + lang).isEqualTo(en.stringPropertyNames());
      for (String key : en.stringPropertyNames()) {
        assertThat(placeholders(bundle.getProperty(key))).as(lang + ": " + key).isEqualTo(placeholders(en.getProperty(key)));
      }
    }
  }

  @Test
  void valuesWithArgumentsAreValidMessageFormats() throws IOException {
    for (String file : List.of("messages", "messages_de", "messages_es", "messages_fr", "messages_it", "messages_pt")) {
      Properties bundle = load("messages/" + file + ".properties");
      for (String key : bundle.stringPropertyNames()) {
        String value = bundle.getProperty(key);
        if (PLACEHOLDER.matcher(value).find()) {
          assertThatCode(() -> new MessageFormat(value)).as(file + ": " + key).doesNotThrowAnyException();
          // an unescaped apostrophe swallows the placeholders behind it
          String formatted = MessageFormat.format(value, "A", "B", "C", "D", "E", "F");
          assertThat(formatted).as(file + ": " + key).doesNotContain("{0}").doesNotContain("{1}");
        }
      }
    }
  }

  private static Set<String> placeholders(String value) {
    Set<String> result = new TreeSet<>();
    Matcher m = PLACEHOLDER.matcher(value);
    while (m.find()) {
      result.add(m.group(1));
    }
    return result;
  }

  private static Properties load(String resource) throws IOException {
    Properties properties = new Properties();
    try (InputStream in = ServerMessagesTest.class.getClassLoader().getResourceAsStream(resource)) {
      assertThat(in).as(resource).isNotNull();
      properties.load(new InputStreamReader(in, StandardCharsets.UTF_8));
    }
    return properties;
  }
}
