package de.mephisto.vpin.commons.utils.i18n;

import de.mephisto.vpin.commons.utils.localsettings.LocalUISettings;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Client-side i18n utility.
 * <p>
 * Resource bundles are loaded from the classpath:
 *   de/mephisto/vpin/ui/messages/messages.properties        (English fallback)
 *   de/mephisto/vpin/ui/messages/messages_de.properties     (German)
 * <p>
 * The language preference lives server-side in {@code UISettings}, but that is not known
 * until the client has connected and fetched it. To render the splash screen and the very
 * first REST calls in the right language, the last-known language tag is cached locally
 * (see {@link LocalUISettings#LANGUAGE}) and used until {@link #setLanguage(String)} is
 * called with the (possibly updated) server-provided value, which refreshes the cache.
 */
public class Messages {
  private static final Logger LOG = LoggerFactory.getLogger(Messages.class);

  private static final String BUNDLE_BASE = "de/mephisto/vpin/ui/messages/messages";

  private static ResourceBundle bundle;
  private static Locale currentLocale;
  private static String currentLanguageTag = LocalUISettings.getString(LocalUISettings.LANGUAGE);

  static {
    reload();
  }

  /**
   * Sets the active language tag (e.g. "en", "de"), caches it locally so the next
   * startup already knows it before connecting to the server, and reloads the bundle.
   */
  public static void setLanguage(String languageTag) {
    currentLanguageTag = languageTag;
    LocalUISettings.saveProperty(LocalUISettings.LANGUAGE, languageTag);
    reload();
  }

  /**
   * (Re-)loads the resource bundle for the currently active language.
   * Falls back to English if no language is set or the bundle is not found.
   */
  public static void reload() {
    currentLocale = resolveLocale(currentLanguageTag);
    Locale.setDefault(currentLocale);
    try {
      bundle = ResourceBundle.getBundle(BUNDLE_BASE, currentLocale);
      LOG.info("Loaded i18n messages bundle for locale: {}", currentLocale);
    }
    catch (MissingResourceException e) {
      LOG.warn("Could not load messages bundle for locale {}; falling back to English: {}", currentLocale, e.getMessage());
      bundle = ResourceBundle.getBundle(BUNDLE_BASE, Locale.ENGLISH);
    }
  }

  /**
   * Returns the current active locale.
   */
  public static Locale getLocale() {
    return currentLocale;
  }

  /**
   * Returns the current language tag (e.g. "en", "de").
   */
  public static String getLanguageTag() {
    return currentLocale.getLanguage();
  }

  /**
   * Looks up a message by key, optionally formatting it with arguments.
   * Returns the key itself if the key is not found.
   */
  public static String get(String key, Object... args) {
    try {
      String value = bundle.getString(key);
      if (args.length > 0) {
        return MessageFormat.format(value, args);
      }
      return value;
    }
    catch (MissingResourceException e) {
      LOG.warn("Missing i18n key: {}", key);
      return key;
    }
  }

  /**
   * Returns the ResourceBundle for use with FXMLLoader.
   */
  public static ResourceBundle getBundle() {
    return bundle;
  }

  // -------------------------------------------------------

  public static Locale resolveLocale(String lang) {
    if (StringUtils.isBlank(lang)) {
      return Locale.ENGLISH;
    }
    switch (lang.toLowerCase()) {
      case "de":
        return Locale.GERMAN;
      case "fr":
        return Locale.FRENCH;
      case "it":
        return Locale.ITALIAN;
      case "es":
        return new Locale("es");
      case "pt":
        return new Locale("pt");
      default:
        return Locale.ENGLISH;
    }
  }
}
