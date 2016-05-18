package cz.cuni.mff.ConfigMapper;

import cz.cuni.mff.ConfigMapper.Annotations.*;
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

		// Construct all sections of the mapped class
		constructSections(instance, false);

		// Create a new mapping context
		Context context = new Context();
		context.mode = mode;

		// Map available configuration option names to reflections of corresponding fields
		extractOptions(instance, context, new Path());

		if (mode == LoadingMode.RELAXED && context.undeclaredOptions == null) {
			throw new MappingException(String.format(
				"Class %s has no field with @UndeclaredOptions",
				cls.getName()
			));
		}

		// Check if all declared sections are present in the configuration file.
		for (Map.Entry<Path, Destination> entry : context.sections.entrySet()) {
			Path path = entry.getKey();
			Destination destination = entry.getValue();

			if (!isSectionPresent(config, path)) {
				if (destination.isOptional) {
					// Remove optional sections from the mapped class
					// and their options from the context
					try {
						destination.set(null);
					} catch (IllegalAccessException e) {
						assert false;
					}

					List<Path> toRemove = context.options.keySet().stream()
						.filter((Path optionPath) -> optionPath.prefix().equals(path))
						.collect(Collectors.toList());

					for (Path removedPath : toRemove) {
						context.options.remove(removedPath);
					}
				} else {
					// Throw an exception if a required section is missing
					throw new MappingException(String.format(
						"Section %s is missing in the configuration",
						path.toString()
					));
				}
			}
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

		// Check if all required fields have been set
		for (Map.Entry<Path, Destination> entry : context.options.entrySet()) {
			Destination destination = entry.getValue();
			Path path = entry.getKey();

			if (!destination.isOptional && !destination.isSet) {
				throw new MappingException(String.format(
					"Required option %s is missing",
					path.toString()
				));
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

	private void constructSections(Object instance, boolean requiredOnly) throws MappingException {
		Class<?> cls = instance.getClass();

		for (Field field : cls.getDeclaredFields()) {
			ConfigSection sectionAnnotation = field.getAnnotation(ConfigSection.class);

			if (sectionAnnotation != null) {
				field.setAccessible(true);

				try {
					boolean constructIfNotPresent = !(requiredOnly && sectionAnnotation.optional());

					if (field.get(instance) == null && constructIfNotPresent) {
						field.set(instance, constructObject(field.getType()));
					}
				} catch (IllegalAccessException e) {
					assert false;
				}
			}
		}
	}

	/**
	 * Extract fields annotated as options from given class and store them in the mapping context.
	 * @param instance an instance of the class to be linked in the options field of the context
	 * @param context the mapping context where extracted options will be stored
	 * @param path path where we currently are in the configuration tree
	 *             (important for recursive calls on section fields)
	 * @throws MappingException
	 */
	private void extractOptions(Object instance, Context context, Path path) throws MappingException {
		Class<?> cls = instance.getClass();

		for (Field field : cls.getDeclaredFields()) {
			ConfigOption fieldAnnotation = field.getAnnotation(ConfigOption.class);

			if (fieldAnnotation != null) {
				String name = !fieldAnnotation.name().isEmpty()
					? fieldAnnotation.name()
					: field.getName();

				Path optionPath;

				if (!fieldAnnotation.section().isEmpty()) {
					optionPath = path.add(fieldAnnotation.section()).add(name);
					context.paths.add(path.add(fieldAnnotation.section()));
				} else {
					optionPath = path.add(name);
				}

				context.paths.add(optionPath);

				context.options.put(
					optionPath,
					new Destination(instance, field, fieldAnnotation.optional())
				);
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
				String name = !sectionAnnotation.name().isEmpty()
					? sectionAnnotation.name()
					: field.getName();

				Path sectionPath = path.add(name);
				context.paths.add(sectionPath);

				context.sections.put(
					sectionPath,
					new Destination(instance, field, sectionAnnotation.optional())
				);

				field.setAccessible(true);

				try {
					Object sectionInstance = field.get(instance);

					if (sectionInstance != null) {
						extractOptions(sectionInstance, context, sectionPath);
					}
				} catch (IllegalAccessException e) {
					assert false;
				}
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
		// Load metadata from the class
		Context context = new Context();
		extractOptions(object, context, new Path());

		// Check if all non-optional sections are present
		for (Path path : context.sections.keySet()) {
			Destination destination = context.sections.get(path);
			Object value = null;

			try {
				value = destination.field.get(destination.instance);
			} catch (IllegalAccessException e) {
				assert false;
			}

			if (!destination.isOptional && value == null) {
				throw new MappingException(String.format("Section %s is null", path));
			}
		}

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

			int pos1 = context.paths.indexOf(i1.path);
			int pos2 = context.paths.indexOf(i2.path);

			assert pos1 != -1;
			assert pos2 != -1;

			return pos1 - pos2;
		});

		// Populate the item set with option nodes
		for (Path path : context.options.keySet()) {
			Destination destination = context.options.get(path);

			Option node;
			destination.field.setAccessible(true);

			try {
				Object value = destination.field.get(destination.instance);

				if (value == null) {
					if (!destination.isOptional) {
						throw new MappingException(String.format("Missing option %s", path));
					}

					continue;
				}

				if (value instanceof List) {
					node = new ListOption(
						path.lastComponent(),
						(List<String>) value);
				} else if (destination.field.getType().isEnum()) {
					String stringValue = value.toString();

					for (ConstantAlias alias : destination.field.getAnnotationsByType(ConstantAlias.class)) {
						if (alias.constant().equals(stringValue)) {
							stringValue = alias.alias();
							break;
						}
					}

					node = new ScalarOption(
						path.lastComponent(),
						stringValue
					);
				} else {
					String stringValue = value.toString();

					checkIntegralConstraint(destination.field, stringValue);
					checkDecimalConstraint(destination.field, stringValue);

					node = new ScalarOption(
						path.lastComponent(),
						stringValue
					);
				}

				ConfigOption optionAnnotation = destination.field.getAnnotation(ConfigOption.class);
				if (!optionAnnotation.description().isEmpty()) {
					node.setDescription(optionAnnotation.description());
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
				Path path = new Path(entry.getKey().split(Path.COMPONENT_SEPARATOR));

				ConfigItem item = new ConfigItem(
					path,
					new ScalarOption(path.lastComponent(), entry.getValue())
				);

				// Insert all prefixes of the option path into the path list,
				// so that undeclared options can be sorted in order of appearance
				while (path.size() > 0) {
					context.paths.add(path);
					path = path.prefix();
				}

				items.add(item);
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

			// Find all items that belong in the same section and remove them from the set
			List<ConfigItem> pickedItems = items.stream()
				.filter(otherItem -> otherItem.path.prefix().equals(item.path.prefix()))
				.collect(Collectors.toList());

			items.removeAll(pickedItems);

			// Create a new section node that contains the removed items
			Path sectionPath = item.path.prefix();
			Section section = new Section(sectionPath.lastComponent(), pickedItems.stream()
				.map((ConfigItem val) -> val.node)
				.collect(Collectors.toList())
			);

			// Set the section description (if possible)
			Destination sectionDestination = context.sections.get(sectionPath);
			if (sectionDestination != null) {
				ConfigSection sectionAnnotation = sectionDestination.field.getAnnotation(ConfigSection.class);
				section.setDescription(sectionAnnotation.description());
			}

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
	 * Save the default values for given class into a new ConfigNode structure
	 * @param cls the class to save
	 * @return a newly created configuration structure
	 * @throws MappingException when the mapping of the class isn't correctly defined
	 */
	public Root saveDefaults(Class<?> cls) throws MappingException {
		Object object = constructObject(cls);
		constructSections(object, true);
		return save(object);
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
				ListOption listOption = (ListOption) option;
				context.undeclaredOptions.put(
					path.toString(),
					String.join(listOption.getSeparator(), listOption.getValue())
				);
			}

			return;
		}

		Field field = destination.field;

		boolean fieldAccessible = field.isAccessible();
		field.setAccessible(true);

		try {
			if (option instanceof ListOption) {
				destination.set(new ArrayList<>(((ListOption) option).getValue()));
			} else if (option instanceof ScalarOption) {
				String value = ((ScalarOption) option).getValue();

				checkIntegralConstraint(field, value);
				checkDecimalConstraint(field, value);

				if (field.getType() == String.class) {
					destination.set(value);
				}

				if (field.getType() == int.class || field.getType() == Integer.class) {
					destination.set(Integer.parseInt(value));
				}

				if (field.getType() == float.class || field.getType() == Float.class) {
					destination.set(Float.parseFloat(value));
				}

				if (field.getType() == double.class || field.getType() == Double.class) {
					destination.set(Double.parseDouble(value));
				}

				if (field.getType() == boolean.class || field.getType() == Boolean.class) {
					ParsedBoolean booleanValue = ((ScalarOption) option).getBooleanValue();
					if (booleanValue == ParsedBoolean.NOT_BOOLEAN) {
						throw new MappingException(String.format(
							"Option %s requires a boolean value",
							path
						));
					}

					destination.set(booleanValue == ParsedBoolean.TRUE);
				}

				if (field.getType().isEnum()) {
					for (ConstantAlias alias : field.getAnnotationsByType(ConstantAlias.class)) {
						if (alias.alias().equals(value)) {
							value = alias.constant();
							break;
						}
					}

					for (Object constant : field.getType().getEnumConstants()) {
						if (constant.toString().equals(value)) {
							destination.set(constant);
							break;
						}
					}

					if (!destination.isSet) {
						throw new MappingException(String.format(
							"Undefined constant %s",
							value
						));
					}
				}
			}
		} catch (IllegalArgumentException e) {
			throw new MappingException("Incompatible types");
		} catch (IllegalAccessException e) {
			assert false;
		}

		field.setAccessible(fieldAccessible);
	}

	/**
	 * If a {@link IntegralConstraint} annotation is present, check whether the option value satisfies the constraint
	 * @param field The field to check
	 * @param valueString A string representation of the option value
	 * @throws MappingException When the constraint is not satisfied or when the annotation is on a wrong type of field
	 */
	private void checkIntegralConstraint(Field field, String valueString) throws MappingException {
		IntegralConstraint constraint = field.getAnnotation(IntegralConstraint.class);

		if (constraint == null) {
			return;
		}

		if (!isNumericField(field)) {
			throw new MappingException(String.format(
				"@IntegralConstraint is not supported on field %s with type %s",
				field.getName(),
				field.getType().getName()
			));
		}

		long value = Long.parseLong(valueString);

		if (value > constraint.max()) {
			throw new MappingException(String.format(
				"Value %d is higher than the maximum allowed value (%d) in field %s",
				value,
				constraint.max(),
				field.getName()
			));
		}

		if (value < constraint.min()) {
			throw new MappingException(String.format(
				"Value %d is lower than the minimum allowed value (%d) in field %s",
				value,
				constraint.min(),
				field.getName()
			));
		}

		if (constraint.unsigned() && value < 0) {
			throw new MappingException(String.format(
				"Value of field %s is negative, but the field is unsigned",
				field.getName()
			));
		}
	}

	private void checkDecimalConstraint(Field field, String valueString) throws MappingException {
		DecimalConstraint constraint = field.getAnnotation(DecimalConstraint.class);

		if (constraint == null) {
			return;
		}

		if (!isNumericField(field)) {
			throw new MappingException(String.format(
				"@DecimalConstraint is not supported on field %s with type %s",
				field.getName(),
				field.getType().getName()
			));
		}

		double value = Double.parseDouble(valueString);

		if (value > constraint.max()) {
			throw new MappingException(String.format(
				"Value %f is higher than the maximum allowed value (%f) in field %s",
				value,
				constraint.max(),
				field.getName()
			));
		}

		if (value < constraint.min()) {
			throw new MappingException(String.format(
				"Value %f is lower than the minimum allowed value (%f) in field %s",
				value,
				constraint.min(),
				field.getName()
			));
		}

		if (constraint.unsigned() && value < 0) {
			throw new MappingException(String.format(
				"Value of field %s is negative, but the field is unsigned",
				field.getName()
			));
		}
	}

	/**
	 * Check if a field's type is numeric
	 * @param field the field to be checked
	 * @return true if the field is numeric, false otherwise
	 */
	private boolean isNumericField(Field field) {
		return field.getType() == Integer.class || field.getType() == int.class
			|| field.getType() == Long.class || field.getType() == long.class
			|| field.getType() == Float.class || field.getType() == float.class
			|| field.getType() == Double.class || field.getType() == double.class;
	}

	/**
	 * Check if a section (given by path) is present in a configuration
	 * @param config the configuration to check
	 * @param path path to the section
	 * @return true if there is a section with given path, false otherwise
	 */
	private boolean isSectionPresent(Root config, Path path) {
		Section cursor = config;

		// Traverse all path components
		for (int i = 0; i < path.size(); i++) {
			String componentName = path.get(i);

			// Find a child node with given name
			Optional<ConfigNode> node = cursor.getChildren().stream()
				.filter((ConfigNode child) -> child.getName().equals(componentName))
				.findFirst();

			if (!node.isPresent()) {
				// Given node was not found in the configuration
				return false;
			}

			if (!(node.get() instanceof Section)) {
				// The node is not a section
				return false;
			}

			cursor = (Section) node.get();
		}

		// All components of the path were found and they are sections
		return true;
	}
}

/**
 * A helper class that contains parts of a fully qualified name of an option.
 */
class Path {
	/**
	 * The string used to separate path components in text representations of the path
	 */
	static final String COMPONENT_SEPARATOR = "#";

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
		return String.join(COMPONENT_SEPARATOR, components);
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

	String get(int index) {
		return components.get(index);
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

	/**
	 * Has the field been set yet?
	 */
	boolean isSet;

	/**
	 * Is the field (or the corresponding option) optional?
	 */
	boolean isOptional;

	Destination(Object instance, Field field, boolean isOptional) {
		this.instance = instance;
		this.field = field;
		this.isOptional = isOptional;
	}

	/**
	 * Set the field's value
	 * @param value value to set
	 * @throws IllegalAccessException
	 */
	public void set(Object value) throws IllegalAccessException {
		field.set(instance, value);
		isSet = true;
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
	 * Records the order of field paths in the mapped class
	 */
	final List<Path> paths = new ArrayList<>();

	/**
	 * Maps section names to fields that contain their data in the mapped object
	 */
	final Map<Path, Destination> sections = new HashMap<>();

	/**
	 * A map where undeclared options should be stored
	 */
	Map<String, String> undeclaredOptions;

	/**
	 * The loading mode of the current mapping operation
	 */
	LoadingMode mode;
}