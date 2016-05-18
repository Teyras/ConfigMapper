package cz.cuni.mff.ConfigMapper;

import cz.cuni.mff.ConfigMapper.Adapters.ConfigAdapter;
import cz.cuni.mff.ConfigMapper.Nodes.Root;

import java.io.*;

/**
 * Provides a simple API for the configuration mapping functionality
 */
public class ConfigFacade {
	/**
	 * The configuration adapter used to work with configuration files
	 */
	private final ConfigAdapter adapter;

	/**
	 * The configuration mapper
	 */
	private final ConfigMapper mapper;

	/**
	 * @param adapter The adapter used to read and write configuration files
	 */
	public ConfigFacade(ConfigAdapter adapter) {
		this.adapter = adapter;
		this.mapper = new ConfigMapper();
	}

	/**
	 * Load an object from an InputStream
	 * @param input The input stream
	 * @param cls The class on which the configuration file should be mapped
	 * @param mode The loading mode
	 * @return A new instance of the mapped class
	 * @throws MappingException when the configuration file cannot be mapped onto this mappers class
	 * @throws ConfigurationException when the configuration file is malformed
	 */
	public <MappedObject> MappedObject load(InputStream input, Class<MappedObject> cls, LoadingMode mode) throws MappingException, ConfigurationException {
		return mapper.load(adapter.read(input), cls, mode);
	}

	/**
	 * Load an object from an InputStream, using the strict mode
	 * @param input The input stream
	 * @param cls The class on which the configuration file should be mapped
	 * @return A new instance of the mapped class
	 * @throws MappingException when the configuration file cannot be mapped onto this mappers class
	 * @throws ConfigurationException when the configuration file is malformed
	 */
	public <MappedObject> MappedObject load(InputStream input, Class<MappedObject> cls) throws MappingException, ConfigurationException {
		return load(input, cls, LoadingMode.STRICT);
	}

	/**
	 * Load an object from a file
	 * @param file The input file
	 * @param cls The class on which the configuration file should be mapped
	 * @param mode The loading mode
	 * @return A new instance of the mapped class
	 * @throws FileNotFoundException when the input file cannot be found
	 * @throws MappingException when the configuration file cannot be mapped onto this mappers class
	 * @throws ConfigurationException when the configuration file is malformed
	 */
	public <MappedObject> MappedObject load(File file, Class<MappedObject> cls, LoadingMode mode) throws FileNotFoundException, MappingException, ConfigurationException {
		return load(new FileInputStream(file), cls, mode);
	}

	/**
	 * Load an object from a file, using the strict mode
	 * @param file The input file
	 * @param cls The class on which the configuration file should be mapped
	 * @return A new instance of the mapped class
	 * @throws FileNotFoundException when the input file cannot be found
	 * @throws MappingException when the configuration file cannot be mapped onto this mappers class
	 * @throws ConfigurationException when the configuration file is malformed
	 */
	public <MappedObject> MappedObject load(File file, Class<MappedObject> cls) throws FileNotFoundException, MappingException, ConfigurationException {
		return load(new FileInputStream(file), cls, LoadingMode.STRICT);
	}

	/**
	 * Save an object into an OutputStream
	 * @param object The mapped object
	 * @param output The output stream
	 * @throws ConfigurationException When the file cannot be saved in the format supported by the adapter
	 */
	public <MappedObject> void save(MappedObject object, OutputStream output) throws MappingException, ConfigurationException, IOException {
		adapter.write(mapper.save(object, false), output);
	}

	/**
	 * Save an object into a file
	 * @param object The mapped object
	 * @param file The output file
	 * @throws IOException when there is a problem with the output file
	 * @throws ConfigurationException When the file cannot be saved in the format supported by the adapter
	 */
	public <MappedObject> void save(MappedObject object, File file) throws IOException, MappingException, ConfigurationException {
		save(object, new FileOutputStream(file));
	}

	/**
	 * Save the default values for the mapped class into an OutputStream
	 * @param output The output stream
	 * @throws IOException when there is a problem with the output file
	 * @throws ConfigurationException when the default values cannot be saved in the format supported by the adapter
	 */
	public <MappedObject> void saveDefaults(Class<MappedObject> cls, OutputStream output) throws IOException, MappingException, ConfigurationException {
		Root config = mapper.saveDefaults(cls);
		adapter.write(config, output);
	}

	/**
	 * Save the default values for the mapped class into a file
	 * @param file The output file
	 * @throws IOException when there is a problem with the output file
	 * @throws ConfigurationException when the default values cannot be saved in the format supported by the adapter
	 */
	public <MappedObject> void saveDefaults(Class<MappedObject> cls, File file) throws IOException, MappingException, ConfigurationException {
		saveDefaults(cls, new FileOutputStream(file));
	}
}
