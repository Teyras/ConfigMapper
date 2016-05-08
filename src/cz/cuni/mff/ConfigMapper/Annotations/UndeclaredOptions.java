package cz.cuni.mff.ConfigMapper.Annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a field that should contain all undeclared options
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface UndeclaredOptions {
}
