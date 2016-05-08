package cz.cuni.mff.ConfigMapper.Annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a field that maps to an option in the configuration file
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigOption {
	/**
	 * Textual description of the option
	 */
	String description() default "";

	/**
	 * Can be used to specify the section the option is found in
	 */
	String section() default "";

	/**
	 * The name of the configuration option. If no name is specified, the name of the annotated variable is used
	 */
	String name() default "";
}
