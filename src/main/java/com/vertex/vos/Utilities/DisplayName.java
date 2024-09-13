package com.vertex.vos.Utilities;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Define the DisplayName annotation
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD) // Target fields only
public @interface DisplayName {
    String value(); // The display name for the field

    boolean exclude() default false; // Whether to exclude this field from being displayed

}
