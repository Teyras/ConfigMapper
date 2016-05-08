package cz.cuni.mff.ConfigMapper;

import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.Nodes.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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
			// Map available configuration option names to reflections of corresponding fields
			Map<Path, Field> options = new HashMap<>();

			for (Field field : cls.getDeclaredFields()) {
				ConfigOption annotation = field.getAnnotation(ConfigOption.class);

				if (annotation != null) {
					String name = !annotation.name().equals("")
						? annotation.name()
						: field.getName();

					if (!annotation.section().equals("")) {
						options.put(new Path(annotation.section(), name), field);
					} else {
						options.put(new Path(name), field);
					}
				}
			}

			// Create a new instance of the mapped class
			Constructor<?> constructor = cls.getDeclaredConstructor();
			boolean constructorAccessible = constructor.isAccessible();
			constructor.setAccessible(true);
			Object instance = constructor.newInstance();
			constructor.setAccessible(constructorAccessible);

			// Traverse the configuration tree and map it onto the newly created instance
			for (ConfigNode node : config.getChildren()) {
				if (node instanceof Section) {
					mapSection((Section) node, new Path(node.getName()), instance, options);
				} else if (node instanceof Option) {
					mapOption((Option) node, new Path(node.getName()), instance, options);
				} else {
					throw new MappingException("Unsupported structure of the configuration tree");
				}
			}

			return instance;
		} catch (NoSuchMethodException e) {
			throw new MappingException(String.format(
				"Mapped class %s has no default constructor",
				cls.getName()
			));
		} catch (InstantiationException e) {
			throw new MappingException(String.format(
				"Could not instantiate mapped class %s",
				cls.getName()
			), e);
		} catch (IllegalAccessException e) {
			throw new MappingException(String.format(
				"Could not access the default constructor of mapped class %s",
				cls.getName()
			), e);
		} catch (InvocationTargetException e) {
			throw new MappingException(String.format(
				"The constructor of class %s threw an exception",
				cls.getName()
			), e);
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

	/**
	 * Map a section of the configuration onto an instance of the mapped class.
	 * @param section The section to be mapped
	 * @param path The path to the section
	 * @param instance An instance of the mapped class
	 * @param options A map from option paths to their destinations
	 * @throws MappingException When the section is ill-formed
	 */
	private void mapSection(Section section, Path path, Object instance, Map<Path, Field> options) throws MappingException {
		for (ConfigNode node : section.getChildren()) {
			if (node instanceof Option) {
				mapOption((Option) node, path.add(node.getName()), instance, options);
			} else {
				throw new MappingException(""); // TODO
			}
		}
	}

	/**
	 * Map an option onto an instance of the mapped class.
	 * @param option The option to be mapped
	 * @param path The path to the option
	 * @param instance An instance of the mapped class
	 * @param options A map from option paths to their destinations
	 * @throws MappingException When the option value is not compatible with its corresponding field
	 *                          or when the option field is undeclared and strict {@link LoadingMode} is used
	 */
	private void mapOption(Option option, Path path, Object instance, Map<Path, Field> options) throws MappingException {
		Field field = options.get(path);

		if (field == null) {
			throw new MappingException(""); // TODO
		}

		boolean fieldAccessible = field.isAccessible();
		field.setAccessible(true);

		try {
			if (option instanceof ListOption) {
				field.set(instance, ((ListOption) option).getValue());
			} else if (option instanceof ScalarOption) {
				String value = ((ScalarOption) option).getValue();

				if (field.getType() == String.class) {
					field.set(instance, value);
				}

				if (field.getType() == int.class || field.getType() == Integer.class) {
					field.set(instance, Integer.parseInt(value));
				}

				if (field.getType() == float.class || field.getType() == Float.class) {
					field.set(instance, Float.parseFloat(value));
				}

				if (field.getType() == double.class || field.getType() == Double.class) {
					field.set(instance, Double.parseDouble(value));
				}

				if (field.getType() == boolean.class || field.getType() == Boolean.class) {
					boolean isTrue = value.equals("on")
						|| value.equals("yes")
						|| value.equals("y")
						|| value.equals("true");

					field.set(instance, isTrue);
				}
			}
		} catch (IllegalArgumentException e) {
			throw new MappingException("Incompatible types");
		} catch (IllegalAccessException e) {
			assert false;
		}

		field.setAccessible(fieldAccessible);
	}
}

/**
 * A helper class that contains parts of a fully qualified name of an option.
 */
class Path {
	/**
	 * The components of the option name.
	 */
	private final List<String> components;

	/**
	 * Constructs a new path with given components.
	 * @param components Components of the new path
	 */
	Path(String... components) {
		this.components = new ArrayList<>(Arrays.asList(components));
	}

	private Path(List<String> components) {
		this.components = components;
	}

	/**
	 * Construct a new path with given component appended to the path of current instance.
	 * @param component The path component to be added
	 * @return A new path with
	 */
	Path add(String component) {
		List<String> components = new ArrayList<>(this.components);
		components.add(component);

		return new Path(components);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Path && components.equals(((Path) other).components);
	}

	@Override
	public int hashCode() {
		return components.hashCode();
	}

	public String toString() {
		return String.join("#", components);
	}
}