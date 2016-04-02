package cz.cuni.mff.ConfigMapper;

import cz.cuni.mff.ConfigMapper.Adapters.ConfigAdapter;

import java.io.*;

/**
 * Provides a simple API for the configuration mapping functionality
 */
public class ConfigFacade<MappedObject> {
	private final ConfigAdapter adapter;
	private final ConfigMapper<MappedObject> mapper;

	public ConfigFacade(Class<MappedObject> cls, ConfigAdapter adapter) {
		this.adapter = adapter;
		this.mapper = new ConfigMapper<MappedObject>(cls);
	}

	public <MappedObject> MappedObject load(InputStream input, LoadingMode mode) throws MappingException, ConfigurationException {
		return mapper.load(adapter.read(input), mode);
	}

	public <MappedObject> MappedObject load(InputStream input) throws MappingException, ConfigurationException {
		return load(input, LoadingMode.STRICT);
	}

	public <MappedObject> MappedObject load(File file, LoadingMode mode) throws FileNotFoundException, MappingException, ConfigurationException {
		return load(new FileInputStream(file), mode);
	}

	public <MappedObject> MappedObject load(File file) throws FileNotFoundException, MappingException, ConfigurationException {
		return load(new FileInputStream(file), LoadingMode.STRICT);
	}

	public void save(MappedObject object, OutputStream output) throws ConfigurationException {
		adapter.write(mapper.save(object), output);
	}

	public void save(MappedObject object, File file) throws FileNotFoundException, ConfigurationException {
		save(object, new FileOutputStream(file));
	}

	public void saveDefaults(OutputStream output) throws  ConfigurationException {

	}

	public void saveDefaults(File file) throws FileNotFoundException, ConfigurationException {
		saveDefaults(new FileOutputStream(file));
	}
}
