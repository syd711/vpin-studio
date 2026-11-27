package de.mephisto.vpin.restclient;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field of a {@link JsonSettings} class (or of an object nested in one) that holds a credential or another
 * private value: passwords, API keys, tokens, webhook URLs.
 * <p>
 * The MCP endpoint masks these fields when an AI assistant reads the settings and rejects every change to them, so
 * they can only be changed in VPin Studio itself.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Secret {
}
