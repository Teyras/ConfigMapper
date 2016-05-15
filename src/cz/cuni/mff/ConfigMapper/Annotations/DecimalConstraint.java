package cz.cuni.mff.ConfigMapper.Annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enables setting constraints on decimal number options (floats/doubles)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
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
