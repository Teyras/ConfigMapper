package cz.cuni.mff.ConfigMapper.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a field that maps to a section in a configuration file
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ConfigSection {
	/**
	 * Textual description of the kind of options the section contains
	 */
	String description() default "";

	/**
	 * The name of the section in the configuration file.
	 * If no name is given, the name of the annotated section variable is used.
	 */
	String name() default "";

	/**
	 * Specifies if the section is optional - if so, it doesn't have to be present in the configuration file.
	 * An optional section can contain non-optional options, which is fine - the section will be null.
	 */
	boolean optional() default false;
}
