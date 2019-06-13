package com.instructure.espresso;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// This annotation will be used as a marker for end-to-end tests
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface E2E {
}
