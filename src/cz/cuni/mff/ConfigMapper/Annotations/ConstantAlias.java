package cz.cuni.mff.ConfigMapper.Annotations;

/**
 * Used to set an alias for a value of an enumerable type option
 * (e.g. EnumerableType.CONSTANT maps to "constant" in the configuration file).
 * This is meant to annotate the enumerable type field.
 */
public @interface ConstantAlias {
	/**
	 * The name of the constant we want to alias
	 */
	String constant();

	/**
	 * The alias of the constant
	 */
	String alias();
}
