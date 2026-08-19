package de.mephisto.vpin.server.i18n;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Spring configuration for server-side i18n.
 *
 * <p>The {@link LocaleResolver} reads the {@code Accept-Language} HTTP header
 * sent by the VPin Studio client with every request. Supported locales are
 * English (default) and German.
 *
 * <p>The {@link MessageSource} loads message bundles from
 * {@code messages/messages.properties} (English) and
 * {@code messages/messages_de.properties} (German) on the server classpath.
 * German umlauts are stored as escaped sequences in the properties file.
 */
@Configuration
public class I18nConfiguration {

  private static final List<Locale> SUPPORTED_LOCALES = Arrays.asList(
      Locale.ENGLISH,
      Locale.GERMAN
  );

  /**
   * Resolves the locale from the {@code Accept-Language} HTTP header.
   * Falls back to English when the header is absent or the locale is unsupported.
   */
  @Bean
  public LocaleResolver localeResolver() {
    AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
    resolver.setSupportedLocales(SUPPORTED_LOCALES);
    resolver.setDefaultLocale(Locale.ENGLISH);
    return resolver;
  }

  /**
   * MessageSource backed by classpath resource bundles.
   * Encoding is UTF-8 so German umlauts are read correctly from the
   * {@code messages_de.properties} file (stored as native-to-ascii escaped sequences).
   */
  @Bean
  public MessageSource messageSource() {
    ResourceBundleMessageSource source = new ResourceBundleMessageSource();
    source.setBasename("messages/messages");
    source.setDefaultEncoding("UTF-8");
    source.setUseCodeAsDefaultMessage(true); // return key when message not found
    source.setFallbackToSystemLocale(false);
    return source;
  }
}
