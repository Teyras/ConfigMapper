package cz.cuni.mff.ConfigMapper;

import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.Annotations.ConfigSection;
import cz.cuni.mff.ConfigMapper.Annotations.UndeclaredOptions;
import cz.cuni.mff.ConfigMapper.Nodes.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

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
		extractOptions(cls, instance, context, new Path(), true);

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

	/**
	 * Create an instance of given class using its default constructor.
	 * @param cls class to be instantiated
	 * @param <MappedObject> type of the class
	 * @return a new instance of given class
	 * @throws MappingException when the instantiation fails
	 */
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

	/**
	 * Extract fields annotated as options from given class and store them in the mapping context.
	 * @param cls class to be extracted
	 * @param instance an instance of the class to be linked in the options field of the context
	 * @param context the mapping context where extracted options will be stored
	 * @param path path where we currently are in the configuration tree
	 *             (important for recursive calls on section fields)
	 * @param instantiateSections if set to true, section fields that are null
	 *                            will be instantiated automatically
	 * @throws MappingException
	 */
	private void extractOptions(Class<?> cls, Object instance, Context context, Path path, boolean instantiateSections) throws MappingException {
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
						if (!instantiateSections) {
							return;
						}

						sectionInstance = constructObject(sectionCls);
						field.set(instance, sectionInstance);
					}
				} catch (IllegalAccessException e) {
					assert false;
					return;
				}

				extractOptions(sectionCls, sectionInstance, context, path.add(name), true);
			}
		}
	}

	/**
	 * Store mapped options from an object to a new configuration structure.
	 * The mapping information is automatically extracted from the object's class.
	 *
	 * @param object The source instance
	 * @throws MappingException When the mapped object is invalid
	 * @return The new configuration structure
	 */
	public Root save(Object object) throws MappingException {
		// Find out the class of the instance
		Class<?> cls = object.getClass();

		// Load metadata from the class
		Context context = new Context();
		extractOptions(cls, object, context, new Path(), false);

		/**
		 * A simple holder for a config node and its path
		 */
		class ConfigItem {
			private Path path;
			private ConfigNode node;

			private ConfigItem(Path path, ConfigNode node) {
				this.path = path;
				this.node = node;
			}
		}

		// Make a data structure that will contain configuration nodes along with their paths,
		// sorted by the length of their paths (longest path first)
		SortedSet<ConfigItem> items = new TreeSet<>((ConfigItem i1, ConfigItem i2) -> {
			// Reverse the order by negating the comparison
			int diff = i2.path.size() - i1.path.size();

			if (diff != 0) {
				return diff;
			}

			return i1.equals(i2) ? 0 : 1; // TODO
		});

		// Populate the item set with option nodes
		for (Path path : context.options.keySet()) {
			Destination destination = context.options.get(path);

			Option node;
			destination.field.setAccessible(true);

			try {
				if (destination.field.get(destination.instance) instanceof List) {
					node = new ListOption(
						path.lastComponent(),
						(List<String>) destination.field.get(destination.instance)
					);
				} else {
					node = new ScalarOption(
						path.lastComponent(),
						destination.field.get(destination.instance).toString()
					);
				}
			} catch (IllegalAccessException e) {
				throw new MappingException(String.format(
					"Field %s of class %s is not accessible",
					destination.field.getName(),
					destination.field.getDeclaringClass().getName()
				));
			}

			items.add(new ConfigItem(path, node));
		}

		// Insert undeclared options into the item list
		if (context.undeclaredOptions != null) {
			for (Map.Entry<String, String> entry : context.undeclaredOptions.entrySet()) {
				Path path = new Path(entry.getKey().split("#"));

				items.add(new ConfigItem(
					path,
					new ScalarOption(path.lastComponent(), entry.getValue())
				));
			}
		}

		// If there are no options, return an empty configuration tree
		if (items.size() == 0) {
			return new Root("", Collections.emptyList());
		}

		// Until all items are at the top level (i.e. their paths only have one component)
		while (!items.stream().allMatch((ConfigItem item) -> item.path.size() == 1)) {
			// Pick item with the longest path
			ConfigItem item = items.first();
			Set<ConfigItem> pickedItems = new HashSet<>(Collections.singleton(item));

			// Find all items that belong in the same section and remove them from the set
			for (ConfigItem otherItem : items) {
				if (otherItem.path.prefix().equals(item.path.prefix())) {
					pickedItems.add(otherItem);
				}
			}

			items.removeAll(pickedItems);

			// Create a new section node that contains the removed items
			Path sectionPath = item.path.prefix();
			Section section = new Section(sectionPath.lastComponent(), pickedItems.stream()
				.map((ConfigItem val) -> val.node)
				.collect(Collectors.toList())
			);

			// Insert the new node back into the item set
			items.add(new ConfigItem(sectionPath, section));
		}

		// Group the top-level nodes under a root node and return it
		return new Root("", items.stream()
			.map((ConfigItem item) -> item.node)
			.collect(Collectors.toList())
		);
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

	int size() {
		return components.size();
	}

	String lastComponent() {
		return components.get(components.size() - 1);
	}

	Path prefix() {
		if (components.size() <= 1) {
			return new Path();
		}

		return new Path(components.subList(0, components.size() - 1));
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
	final Map<Path, Destination> options = new LinkedHashMap<>();

	/**
	 * A map where undeclared options should be stored
	 */
	Map<String, String> undeclaredOptions;

	/**
	 * The loading mode of the current mapping operation
	 */
	LoadingMode mode;
}