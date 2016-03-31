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

	public <MappedObject> MappedObject load(InputStream input, LoadingMode mode) {
		return mapper.load(adapter.read(input), mode);
	}

	public <MappedObject> MappedObject load(InputStream input) {
		return load(input, LoadingMode.STRICT);
	}

	public <MappedObject> MappedObject load(File file, LoadingMode mode) throws FileNotFoundException {
		return load(new FileInputStream(file), mode);
	}

	public <MappedObject> MappedObject load(File file) throws FileNotFoundException {
		return load(new FileInputStream(file), LoadingMode.STRICT);
	}

	public void save(MappedObject object, OutputStream output) {

	}

	public void save(MappedObject object, File file) throws FileNotFoundException {
		save(object, new FileOutputStream(file));
	}

	public void saveDefaults(OutputStream output) {

	}

	public void saveDefaults(File file) throws FileNotFoundException {
		saveDefaults(new FileOutputStream(file));
	}
}
