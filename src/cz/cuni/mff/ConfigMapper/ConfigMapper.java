package cz.cuni.mff.ConfigMapper;

import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.Annotations.ConfigSection;
import cz.cuni.mff.ConfigMapper.Annotations.UndeclaredOptions;
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
	public <MappedObject> MappedObject load(Root config, Class<MappedObject> cls, LoadingMode mode) throws MappingException {
		// Create a new instance of the mapped class
		MappedObject instance = constructObject(cls);

		// Create a new mapping context
		Context context = new Context();
		context.mode = mode;

		// Map available configuration option names to reflections of corresponding fields
		extractOptions(cls, instance, context, new Path());

		if (mode == LoadingMode.RELAXED && context.undeclaredOptions == null) {
			throw new MappingException(String.format(
				"Class %s has no field with @UndeclaredOptions",
				cls.getName()
			));
		}

		// Traverse the configuration tree and map it onto the newly created instance
		for (ConfigNode node : config.getChildren()) {
			if (node instanceof Section) {
				mapSection((Section) node, new Path(node.getName()), context);
			} else if (node instanceof Option) {
				mapOption((Option) node, new Path(node.getName()), context);
			} else {
				throw new MappingException("Unsupported structure of the configuration tree");
			}
		}

		return instance;
	}

	private <MappedObject> MappedObject constructObject(Class<MappedObject> cls) throws MappingException {
		try {
			Constructor<MappedObject> constructor = cls.getDeclaredConstructor();
			constructor.setAccessible(true);
			return constructor.newInstance();
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

	private void extractOptions(Class<?> cls, Object instance, Context context, Path path) throws MappingException {
		for (Field field : cls.getDeclaredFields()) {
			ConfigOption fieldAnnotation = field.getAnnotation(ConfigOption.class);

			if (fieldAnnotation != null) {
				String name = !fieldAnnotation.name().equals("")
					? fieldAnnotation.name()
					: field.getName();

				if (!fieldAnnotation.section().equals("")) {
					context.options.put(
						path.add(fieldAnnotation.section()).add(name),
						new Destination(instance, field)
					);
				} else {
					context.options.put(
						path.add(name),
						new Destination(instance, field)
					);
				}
			}

			UndeclaredOptions undeclaredAnnotation = field.getAnnotation(UndeclaredOptions.class);

			if (undeclaredAnnotation != null) {
				if (context.undeclaredOptions != null) {
					throw new MappingException(String.format(
						"Class %s contains more than one field with @UndeclaredOptions",
						cls.getName()
					));
				}

				field.setAccessible(true);

				try {
					if (!(field.get(instance) instanceof Map<?, ?>)) {
						throw new MappingException(String.format(
							"Field %s of class %s is not of type Map<String, String>",
							field.getName(),
							cls.getName()
						));
					}

					context.undeclaredOptions = (Map<String, String>) field.get(instance);
				} catch (IllegalAccessException e) {
					assert false;
				}
			}

			ConfigSection sectionAnnotation = field.getAnnotation(ConfigSection.class);

			if (sectionAnnotation != null) {
				Class<?> sectionCls = field.getType();

				String name = !sectionAnnotation.name().equals("")
					? sectionAnnotation.name()
					: field.getName();

				field.setAccessible(true);
				Object sectionInstance;

				try {
					sectionInstance = field.get(instance);

					if (sectionInstance == null) {
						sectionInstance = constructObject(sectionCls);
						field.set(instance, sectionInstance);
					}
				} catch (IllegalAccessException e) {
					assert false;
					return;
				}

				extractOptions(sectionCls, sectionInstance, context, path.add(name));
			}
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
	 * @param context The mapping context
	 * @throws MappingException When the section is ill-formed
	 */
	private void mapSection(Section section, Path path, Context context) throws MappingException {
		for (ConfigNode node : section.getChildren()) {
			if (node instanceof Option) {
				mapOption((Option) node, path.add(node.getName()), context);
			} else {
				throw new MappingException("Unsupported configuration structure");
			}
		}
	}

	/**
	 * Map an option onto an instance of the mapped class.
	 * @param option The option to be mapped
	 * @param path The path to the option
	 * @param context The mapping context
	 * @throws MappingException When the option value is not compatible with its corresponding field
	 *                          or when the option field is undeclared and strict {@link LoadingMode} is used
	 */
	private void mapOption(Option option, Path path, Context context) throws MappingException {
		Destination destination = context.options.get(path);

		if (destination == null) {
			if (context.mode != LoadingMode.RELAXED) {
				throw new MappingException(String.format(
					"Undeclared option %s",
					path
				));
			}

			if (option instanceof ScalarOption) {
				context.undeclaredOptions.put(
					path.toString(),
					((ScalarOption) option).getValue()
				);
			} else if (option instanceof ListOption) {
				context.undeclaredOptions.put(
					path.toString(),
					String.join(":", ((ListOption) option).getValue())
				);
			}

			return;
		}

		Field field = destination.field;
		Object instance = destination.instance;

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

/**
 * Contains a reflection of a field along with an instance of the object that contains it
 */
class Destination {
	/**
	 * An instance to which the destination field belongs
	 */
	final Object instance;

	/**
	 * Reflection of the destination field
	 */
	final Field field;

	Destination(Object instance, Field field) {
		this.instance = instance;
		this.field = field;
	}
}

/**
 * Contains information related to a single mapping operation
 */
class Context {
	/**
	 * Maps fully qualified option names to fields that should contain their values
	 */
	final Map<Path, Destination> options = new HashMap<>();

	/**
	 * A map where undeclared options should be stored
	 */
	Map<String, String> undeclaredOptions;

	/**
	 * The loading mode of the current mapping operation
	 */
	LoadingMode mode;
}