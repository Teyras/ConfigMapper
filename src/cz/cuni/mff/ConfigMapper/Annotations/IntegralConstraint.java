package cz.cuni.mff.ConfigMapper.Annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Enables setting constraints on integral number options
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegralConstraint {
	/**
	 * Maximum value of the option
	 */
	long max() default Long.MAX_VALUE;

	/**
	 * Minimum value of the option
	 */
	long min() default Long.MIN_VALUE;

	/**
	 * Should the option only allow unsigned numbers?
	 */
	boolean unsigned() default false;
}
