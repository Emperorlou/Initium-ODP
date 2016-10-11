package com.universeprojects.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is placed on a page-controller class, to indicate that this is an active controller.
 * Conversely, removing the annotation renders the controller inactive, as it won't be picked up by the registry.
 * <p>
 * (NOTE: A page-controller class is a class that extends @{link PageController})
 * <p>
 * It's also handy to configure the IDE to not label classes annotated with this annotation "unused".
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
}
