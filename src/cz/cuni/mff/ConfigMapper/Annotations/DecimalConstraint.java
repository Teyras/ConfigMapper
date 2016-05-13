package cz.cuni.mff.ConfigMapper.Annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Enables setting constraints on decimal number options (floats/doubles)
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface DecimalConstraint {
	/**
	 * Maximum value of the option
	 */
	double max() default Double.MAX_VALUE;

	/**
	 * Minimum value of the option
	 */
	double min() default Double.MIN_VALUE;

	/**
	 * Should the option only allow unsigned numbers?
	 */
	boolean unsigned() default false;
}
