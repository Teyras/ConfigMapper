package cz.cuni.mff.ConfigMapper.Annotations;

/**
 * Enables setting constraints on numeric options
 */
public @interface NumericValue {
	/**
	 * Maximum value of the option
	 */
	long max() default Long.MAX_VALUE;

	/**
	 * Minimum value of the option
	 */
	long min() default Long.MIN_VALUE;

	/**
	 * Should the option value be unsigned?
	 */
	boolean unsigned() default false;
}
