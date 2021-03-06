package cz.cuni.mff.ConfigMapper.Annotations;

import java.lang.annotation.*;

/**
 * Used to set an alias for a value of an enumerable type option
 * (e.g. EnumerableType.CONSTANT maps to "constant" in the configuration file).
 * This is meant to annotate the enumerable type field.
 */
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ConstantAliases.class)
@Target(ElementType.FIELD)
public @interface ConstantAlias {
	/**
	 * The name of the constant we want to alias (unqualified - "CONST", not "Enumerable.CONST")
	 */
	String constant() default "";

	/**
	 * The alias of the constant
	 */
	String alias() default "";
}
