package cz.cuni.mff.ConfigMapper.Annotations;

/**
 * Marks a field that maps to an option in the configuration file
 */
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
	 * Optional maximum value of the option (only applies to numeric types)
	 */
	int max() default Integer.MAX_VALUE;

	/**
	 * Optional minimum value of the option (only applies to numeric types)
	 */
	int min() default Integer.MIN_VALUE;
}
