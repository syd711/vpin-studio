package de.mephisto.vpin.server.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Server-side i18n helper.
 *
 * <p>Resolves messages from the Spring {@link MessageSource} using the locale
 * extracted from the HTTP {@code Accept-Language} request header.
 * Falls back to English when no locale is provided or a key is missing.
 *
 * <p>Usage in a REST resource:
 * <pre>
 *   &#64;GetMapping("/something")
 *   public ResponseEntity<?> doSomething(
 *       &#64;RequestHeader(value = HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "en") String acceptLanguage) {
 *     Locale locale = ServerMessages.parseLocale(acceptLanguage);
 *     String msg = ServerMessages.get(messageSource, "backup.job.done", locale, tableName);
 *     ...
 *   }
 * </pre>
 */
public class ServerMessages {
  private static final Logger LOG = LoggerFactory.getLogger(ServerMessages.class);

  private static final String BUNDLE_BASE = "messages/messages";

  private ServerMessages() {
  }

  /**
   * Parses the HTTP {@code Accept-Language} header value into a {@link Locale}.
   * Supports simple tags like "en", "de", "en-US", "de-DE".
   * Falls back to {@link Locale#ENGLISH} for unknown or blank values.
   */
  public static Locale parseLocale(String acceptLanguage) {
    if (StringUtils.isBlank(acceptLanguage)) {
      return Locale.ENGLISH;
    }
    // Accept-Language may contain quality values, e.g. "de,en-US;q=0.9"
    // Take only the primary tag
    String primary = acceptLanguage.split(",")[0].trim().split(";")[0].trim();
    try {
      Locale locale = Locale.forLanguageTag(primary);
      // Locale.forLanguageTag returns und (undetermined) for bad tags
      if ("und".equals(locale.toLanguageTag())) {
        return Locale.ENGLISH;
      }
      return locale;
    }
    catch (Exception e) {
      return Locale.ENGLISH;
    }
  }

  /**
   * Resolves a message using the Spring {@link MessageSource}.
   *
   * @param messageSource injected Spring MessageSource
   * @param key           the message key
   * @param locale        the target locale
   * @param args          optional {@link MessageFormat} arguments
   * @return resolved message, or the key itself if not found
   */
  public static String get(MessageSource messageSource, String key, Locale locale, Object... args) {
    try {
      return messageSource.getMessage(key, args, locale);
    }
    catch (NoSuchMessageException e) {
      LOG.warn("Missing server i18n key '{}' for locale {}", key, locale);
      // Try English fallback
      try {
        return messageSource.getMessage(key, args, Locale.ENGLISH);
      }
      catch (NoSuchMessageException ex) {
        return key;
      }
    }
  }

  /**
   * Convenience overload that uses the classpath {@link ResourceBundle} directly
   * (useful in Spring beans that do not have MessageSource injected, or in tests).
   */
  public static String get(String key, Locale locale, Object... args) {
    try {
      ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE_BASE, locale != null ? locale : Locale.ENGLISH);
      String value = bundle.getString(key);
      if (args != null && args.length > 0) {
        return MessageFormat.format(value, args);
      }
      return value;
    }
    catch (MissingResourceException e) {
      LOG.warn("Missing server i18n key '{}' for locale {}", key, locale);
      return key;
    }
  }
}
