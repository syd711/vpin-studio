package de.mephisto.vpin.server.util;

/**
 * Utility for accessing the different config files.
 */
public class Config {
  private final static String GENERATOR_CONFIG_FILENAME = "overlay-generator.properties";
  private final static String CARD_CONFIG_FILENAME = "card-generator.properties";
  private final static String COMMAND_CONFIG_FILENAME = "commands.properties";
  private final static String VERSION_CONFIG_FILENAME = "../version.properties";

  private static PropertiesStore generatorConfig;
  private static PropertiesStore cardConfig;
  private static PropertiesStore commandConfig;

  public static PropertiesStore getCardGeneratorConfig() {
    if (cardConfig == null) {
      cardConfig = PropertiesStore.create(CARD_CONFIG_FILENAME);
    }
    return cardConfig;
  }

  public static PropertiesStore getOverlayGeneratorConfig() {
    if (generatorConfig == null) {
      generatorConfig = PropertiesStore.create(GENERATOR_CONFIG_FILENAME);
    }
    return generatorConfig;
  }

  public static PropertiesStore getCommandConfig() {
    if (commandConfig == null) {
      commandConfig = PropertiesStore.create(COMMAND_CONFIG_FILENAME);
    }
    return commandConfig;
  }

  public static PropertiesStore getVersionConfig() {
    if (commandConfig == null) {
      commandConfig = PropertiesStore.create(VERSION_CONFIG_FILENAME);
    }
    return commandConfig;
  }

  public static PropertiesStore getConfig(String name) {
    return PropertiesStore.create(name);
  }

  public static void reloadAll() {
    commandConfig = null;
    cardConfig = null;
    generatorConfig = null;
  }
}
