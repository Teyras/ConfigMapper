package cz.cuni.mff.ConfigMapper;

import cz.cuni.mff.ConfigMapper.Annotations.UndeclaredOptions;

/**
 * Represents the way of mapping configuration to objects
 */
public enum LoadingMode {
	/**
	 * Any undeclared fields result in an error
	 */
	STRICT,

	/**
	 * Undeclared options are stored in a field annotated with {@link UndeclaredOptions}
	 */
	RELAXED
}
