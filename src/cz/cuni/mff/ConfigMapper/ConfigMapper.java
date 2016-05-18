package cz.cuni.mff.ConfigMapper;

import cz.cuni.mff.ConfigMapper.Annotations.*;
import cz.cuni.mff.ConfigMapper.Nodes.*;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Maps {@link ConfigNode} structures to objects and back
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
	public <MappedObject> MappedObject load(ConfigRoot config, Class<MappedObject> cls, LoadingMode mode) throws MappingException {
		// Create a new instance of the mapped class
		MappedObject instance = constructObject(cls);

		// Construct all sections of the mapped class
		constructSections(instance, false);

		// Create a new mapping context
		Context context = new Context();
		context.mode = mode;

		// Map available configuration option names to reflections of corresponding fields
		extractMappingData(instance, context, new Path());

		// If we're using the relaxed loading mode,
		// make sure that there is a container for undeclared options
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

			// Handle the case when a section is not present in the configuration
			if (!isSectionPresent(config, path)) {
				if (destination.isOptional) {
					// Remove optional sections from the mapped class
					// and their options from the context
					destination.set(null);

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
				loadSection((Section) node, new Path(node.getName()), context);
			} else if (node instanceof Option) {
				loadOption((Option) node, new Path(node.getName()), context);
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
			makeAccessible(constructor);
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
		} catch (InvocationTargetException e) {
			throw new MappingException(String.format(
				"The constructor of class %s threw an exception",
				cls.getName()
			), e);
		} catch (IllegalAccessException e) {
			// This shouldn't happen if makeAccessible() succeeded
			assert false;
			throw new MappingException(""); // just to make the compiler happy
		}
	}

	/**
	 * Traverse the fields of an object and, if necessary,
	 * initialize those annotated with {@link ConfigSection} using the default constructor.
	 * @param instance an object whose sections we need to construct
	 * @param requiredOnly if set to true, optional sections will not be initialized
	 * @throws MappingException When the construction of an object fails
	 */
	private void constructSections(Object instance, boolean requiredOnly) throws MappingException {
		Class<?> cls = instance.getClass();

		// Traverse the fields of the object
		for (Field field : cls.getDeclaredFields()) {
			ConfigSection sectionAnnotation = field.getAnnotation(ConfigSection.class);

			// If the field is an annotated section, check its value
			if (sectionAnnotation != null) {
				try {
					makeAccessible(field);
					boolean constructIfNotPresent = !(requiredOnly && sectionAnnotation.optional());

					// If necessary, construct the section object
					if (field.get(instance) == null && constructIfNotPresent) {
						field.set(instance, constructObject(field.getType()));
					}

					// Also construct the subsections of the section
					if (field.get(instance) != null) {
						constructSections(field.get(instance), requiredOnly);
					}
				} catch (IllegalAccessException e) {
					// If makeAccessible() succeeded, this shouldn't happen
					assert false;
				}
			}
		}
	}

	/**
	 * Make the value of an object accessible via reflection API.
	 * If the object is already accessible, there will be no reflection call.
	 * @param object object to be made accessible
	 */
	private void makeAccessible(AccessibleObject object) {
		if (!object.isAccessible()) {
			object.setAccessible(true);
		}
	}

	/**
	 * Extract information about annotated options, sections, etc. from given object and store them in the mapping context.
	 * For section fields, the function is called recursively.
	 * @param instance an instance of the class to extract mapping data from
	 * @param context output parameter - the mapping context where extracted options should be stored
	 * @param path path where we currently are in the configuration tree
	 *             (important for recursive calls on section fields)
	 * @throws MappingException
	 */
	private void extractMappingData(Object instance, Context context, Path path) throws MappingException {
		Class<?> cls = instance.getClass();

		// Traverse declared fields
		for (Field field : cls.getDeclaredFields()) {
			processOptionAnnotation(field, instance, context, path);
			processSectionAnnotation(field, instance, context, path);
			processUndeclaredOptionsAnnotation(field, instance, context);
		}
	}

	/**
	 * If given field has the {@link ConfigOption} annotation, save information about the option into the context
	 * @param field the field to check
	 * @param instance instance to check
	 * @param context the context where the resulting information should be stored
	 * @param path path of the section containing the field
	 */
	private void processOptionAnnotation(Field field, Object instance, Context context, Path path) {
		ConfigOption optionAnnotation = field.getAnnotation(ConfigOption.class);

		if (optionAnnotation == null) {
			return;
		}

		String name = !optionAnnotation.name().isEmpty()
			? optionAnnotation.name()
			: field.getName();

		Path optionPath;

		if (!optionAnnotation.section().isEmpty()) {
			optionPath = path.add(optionAnnotation.section()).add(name);
			context.paths.add(path.add(optionAnnotation.section()));
		} else {
			optionPath = path.add(name);
		}

		context.paths.add(optionPath);

		context.options.put(
			optionPath,
			new Destination(instance, field, optionAnnotation.optional())
		);
	}

	/**
	 * If given field has the {@link ConfigSection} annotation, save information about the section into the context
	 * @param field the field to check
	 * @param instance instance to check
	 * @param context the context where the resulting information should be stored
	 * @param path path of the section containing the field
	 * @throws MappingException
	 */
	private void processSectionAnnotation(Field field, Object instance, Context context, Path path) throws MappingException {
		ConfigSection sectionAnnotation = field.getAnnotation(ConfigSection.class);

		if (sectionAnnotation == null) {
			return;
		}

		String name = !sectionAnnotation.name().isEmpty()
			? sectionAnnotation.name()
			: field.getName();

		Path sectionPath = path.add(name);
		context.paths.add(sectionPath);

		context.sections.put(
			sectionPath,
			new Destination(instance, field, sectionAnnotation.optional())
		);

		try {
			makeAccessible(field);
			Object sectionInstance = field.get(instance);

			if (sectionInstance != null) {
				extractMappingData(sectionInstance, context, sectionPath);
			}
		} catch (IllegalAccessException e) {
			// If makeAccessible() succeeded, this shouldn't happen
			assert false;
		}
	}

	/**
	 * If given field has the {@link UndeclaredOptions} annotation, save information about the undeclared option container into the context
	 * @param field the field to check
	 * @param instance instance to check
	 * @param context the context where the resulting information should be stored
	 * @throws MappingException
	 */
	private void processUndeclaredOptionsAnnotation(Field field, Object instance, Context context) throws MappingException {
		UndeclaredOptions undeclaredAnnotation = field.getAnnotation(UndeclaredOptions.class);

		if (undeclaredAnnotation == null) {
			return;
		}

		Class<?> cls = instance.getClass();

		if (context.undeclaredOptions != null) {
			throw new MappingException(String.format(
				"Class %s contains more than one field with @UndeclaredOptions",
				cls.getName()
			));
		}

		try {
			makeAccessible(field);

			if (!(field.get(instance) instanceof Map<?, ?>)) {
				throw new MappingException(String.format(
					"Field %s of class %s is not of type Map<String, String>",
					field.getName(),
					cls.getName()
				));
			}

			context.undeclaredOptions = (Map<String, String>) field.get(instance);
		} catch (IllegalAccessException e) {
			// If makeAccessible() succeeded, this shouldn't happen
			assert false;
		}
	}

	/**
	 * Store mapped options from an object to a new configuration structure.
	 * The mapping information is automatically extracted from the object's class.
	 *
	 * @param object The source instance
	 * @param originalConfig The configuration used to load the object now being saved
	 * @param keepDefaults Should the result contain default values?
	 * @throws MappingException When the mapped object is invalid
	 * @return The new configuration structure
	 */
	public ConfigRoot save(Object object, ConfigRoot originalConfig, boolean keepDefaults) throws MappingException {
		// Load metadata from the class
		Context context = new Context();
		extractMappingData(object, context, new Path());

		// Make another context from a default object of the mapped class
		Context defaultContext = new Context();

		if (!keepDefaults) {
			Object defaultObject = constructObject(object.getClass());
			constructSections(defaultObject, true);
			extractMappingData(defaultObject, defaultContext, new Path());
		}

		// Check if all non-optional sections are present
		for (Path path : context.sections.keySet()) {
			Destination destination = context.sections.get(path);
			Object value = destination.get();

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
		// sorted by the length of their paths (longest path first),
		// and then by their order of appearance in the mapped class
		SortedSet<ConfigItem> items = new TreeSet<>((ConfigItem i1, ConfigItem i2) -> {
			// Items with longer paths go first
			int diff = i2.path.size() - i1.path.size();

			if (diff != 0) {
				return diff;
			}

			// When paths have equal lengths, compare order of appearance
			int pos1 = context.paths.indexOf(i1.path);
			int pos2 = context.paths.indexOf(i2.path);

			// Both of the paths should be present in the context
			assert pos1 != -1;
			assert pos2 != -1;

			return pos1 - pos2;
		});

		// Populate the item set with option nodes
		for (Path path : context.options.keySet()) {
			Destination destination = context.options.get(path);
			Object value = destination.get();

			// If a required option is missing, throw an exception
			if (value == null) {
				if (!destination.isOptional) {
					throw new MappingException(String.format("Missing option %s", path));
				}

				continue;
			}

			Option originalOption = null;
			if (originalConfig != null) {
				ConfigNode node = getNode(originalConfig, path);
				if (node instanceof Option) {
					originalOption = (Option) node;
				}
			}

			Option node = storeOptionValue(path.lastComponent(), destination.field, value);

			if (originalOption != null && !originalOption.getDescription().isEmpty()) {
				node.setDescription(originalOption.getDescription());
			}

			// If an option is optional and equal to its default value, skip it
			// (unless keepDefaults is set or it was in the original config)
			if (!keepDefaults && defaultContext.options.containsKey(path)) {
				Destination defaultDestination = defaultContext.options.get(path);
				Object defaultValue = defaultDestination.get();

				boolean defaultEqual = Objects.equals(value, defaultValue);
				boolean originalEqual = Objects.equals(node, originalOption);

				if (!originalEqual && defaultDestination.isOptional && defaultEqual) {
					continue;
				}
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
			return new ConfigRoot("", Collections.emptyList());
		}

		// Until all items are at the top level (i.e. their paths only have one component)
		while (!items.stream().allMatch((item) -> item.path.size() == 1)) {
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
		return new ConfigRoot("", items.stream()
			.map((ConfigItem item) -> item.node)
			.collect(Collectors.toList())
		);
	}

	/**
	 * Create a new option node with given value
	 * @param name name of the option
	 * @param field the field where the option was stored
	 * @param value the value of the option
	 * @return a new option node
	 * @throws MappingException when a constraint fails
	 */
	private Option storeOptionValue(String name, Field field, Object value) throws MappingException {
		Option node;

		if (value instanceof List) {
			node = new ListOption(name, (List<String>) value);
		} else if (field.getType().isEnum()) {
			String stringValue = value.toString();

			for (ConstantAlias alias : field.getAnnotationsByType(ConstantAlias.class)) {
				if (alias.constant().equals(stringValue)) {
					stringValue = alias.alias();
					break;
				}
			}

			node = new ScalarOption(name, stringValue);
		} else {
			String stringValue = value.toString();

			checkIntegralConstraint(field, stringValue);
			checkDecimalConstraint(field, stringValue);

			node = new ScalarOption(name, stringValue);
		}

		ConfigOption optionAnnotation = field.getAnnotation(ConfigOption.class);
		if (!optionAnnotation.description().isEmpty()) {
			node.setDescription(optionAnnotation.description());
		}

		return node;
	}

	/**
	 * Save the default values for given class into a new ConfigNode structure
	 * @param cls the class to save
	 * @return a newly created configuration structure
	 * @throws MappingException when the mapping of the class isn't correctly defined
	 */
	public ConfigRoot saveDefaults(Class<?> cls) throws MappingException {
		Object object = constructObject(cls);
		constructSections(object, true);
		return save(object, null, true);
	}

	/**
	 * Map a section of the configuration onto an instance of the mapped class.
	 * @param section The section to be mapped
	 * @param path The path to the section
	 * @param context The mapping context
	 * @throws MappingException When the section is ill-formed
	 */
	private void loadSection(Section section, Path path, Context context) throws MappingException {
		for (ConfigNode node : section.getChildren()) {
			if (node instanceof Option) {
				loadOption((Option) node, path.add(node.getName()), context);
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
	private void loadOption(Option option, Path path, Context context) throws MappingException {
		Destination destination = context.options.get(path);

		// Handle the case of an undeclared option
		if (destination == null) {
			if (context.mode != LoadingMode.RELAXED) {
				throw new MappingException(String.format(
					"Undeclared option %s",
					path
				));
			}

			loadUndeclaredOption(option, path, context);
			return;
		}

		Field field = destination.field;

		try {
			loadOptionValue(option, destination);
		} catch (IllegalArgumentException e) {
			throw new MappingException(String.format(
				"Invalid value supplied for field %s of type %s",
				field.getName(),
				field.getType().getName()
			));
		}
	}

	/**
	 * Map an option to the corresponding destination
	 * @param option the option to be mapped
	 * @param destination where the option value shall be stored
	 * @throws IllegalAccessException
	 * @throws MappingException
	 */
	private void loadOptionValue(Option option, Destination destination) throws MappingException {
		if (option instanceof ListOption) {
			destination.set(new ArrayList<>(((ListOption) option).getValue()));
			return;
		}

		if (option instanceof ScalarOption) {
			Field field = destination.field;
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
						"Field %s requires a boolean value",
						field.getName()
					));
				}

				destination.set(booleanValue == ParsedBoolean.TRUE);
			}

			if (field.getType().isEnum()) {
				value = resolveEnumConstantAlias(field, value);
				Object enumConstant = getEnumConstantByName(field.getType(), value);

				if (enumConstant == null) {
					throw new MappingException(String.format(
						"Undefined constant %s",
						value
					));
				}

				destination.set(enumConstant);
			}
		}
	}

	/**
	 * Store an option in the undeclared option container
	 * @param option the option to store
	 * @param path path to the option
	 * @param context mapping context
	 */
	private void loadUndeclaredOption(Option option, Path path, Context context) {
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
	}

	/**
	 * For given enum class and string, find an enum constant with name equal to the string
	 * @param cls an enum class
	 * @param value the string to search for
	 * @return enum constant with name equal to given string or null
	 */
	private Object getEnumConstantByName(Class<?> cls, String value) {
		for (Object constant : cls.getEnumConstants()) {
			if (constant.toString().equals(value)) {
				return constant;
			}
		}

		return null;
	}

	/**
	 * If given string is an alias of another enum constant, return the string representation of that constant
	 * @param field the field that contains given string
	 * @param value the string value
	 * @return the resolved constant name
	 */
	private String resolveEnumConstantAlias(Field field, String value) {
		for (ConstantAlias alias : field.getAnnotationsByType(ConstantAlias.class)) {
			if (alias.alias().equals(value)) {
				return alias.constant();
			}
		}

		return value;
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

	/**
	 * If a {@link DecimalConstraint} annotation is present, check whether the option value satisfies the constraint
	 * @param field The field to check
	 * @param valueString A string representation of the option value
	 * @throws MappingException When the constraint is not satisfied or when the annotation is on a wrong type of field
	 */
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
	 * Find a node with given path in a configuration structure
	 * @param config configuration structure to search in
	 * @param path the path of required node
	 * @return the node or null
	 */
	private ConfigNode getNode(ConfigRoot config, Path path) {
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
				return null;
			}

			if (i == path.size() - 1) {
				// Last iteration - return the node
				return node.get();
			}

			// Go deeper into the configuration structure
			cursor = (Section) node.get();
		}

		return null;
	}

	/**
	 * Check if a section (given by path) is present in a configuration
	 * @param config the configuration to check
	 * @param path path to the section
	 * @return true if there is a section with given path, false otherwise
	 */
	private boolean isSectionPresent(ConfigRoot config, Path path) {
		return getNode(config, path) instanceof Section;
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

		if (!field.isAccessible()) {
			field.setAccessible(true);
		}
	}

	/**
	 * Set the field's value
	 * @param value value to set
	 */
	public void set(Object value) {
		try {
			field.set(instance, value);
			isSet = true;
		} catch (IllegalAccessException e) {
			// This shouldn't happen if setAccessible() succeeded
			assert false;
		}
	}

	/**
	 * Get the field's value
	 * @return the field'S value
	 */
	public Object get() {
		try {
			return field.get(instance);
		} catch (IllegalAccessException e) {
			// This shouldn't happen if setAccessible() succeeded
			assert false;
		}

		return null;
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