package cz.cuni.mff.ConfigMapper;

import cz.cuni.mff.ConfigMapper.Nodes.ConfigNode;
import cz.cuni.mff.ConfigMapper.Nodes.Root;

/**
 * Maps {@link ConfigNode} structures to objects
 */
public class ConfigMapper {

	/**
	 * Map config to a newly created instance of a class
	 *
	 * @param config The configuration tree to be mapped
	 * @param cls The class the configuration will be mapped to
	 * @param mode The mapping mode
	 * @throws MappingException When the loaded configuration cannot be mapped onto an object of given class
	 * @return A new instance of given class with options from config
	 */
	public Object load(Root config, Class<?> cls, LoadingMode mode) throws MappingException {
		try {
			return cls.newInstance();
		} catch (InstantiationException e) {
			throw new MappingException(String.format(
				"Could not instantiate mapped class %s - does it have a default constructor?",
				cls.getName()
			));
		} catch (IllegalAccessException e) {
			throw new MappingException(String.format(
				"Could not access the default constructor of mapped class %s",
				cls.getName()
			));
		}
	}

	/**
	 * Store mapped options from an object to a new configuration structure.
	 * The mapping information is automatically extracted from the objects class.
	 *
	 * @param object The source instance
	 * @return The new configuration structure
	 */
	public Root save(Object object) {
		return null;
	}
}
