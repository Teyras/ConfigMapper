package cz.cuni.mff.ConfigMapper.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field that maps to an option in the configuration file.
 * The initial value of the field (after the default constructor is executed) is considered the default value.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
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

	/**
	 * True if the option doesn't have to be present in the configuration file.
	 * If so, the default value of the corresponding field's type is used (e.g. null or 0)
	 */
	boolean optional() default false;
}
