package cz.cuni.mff.ConfigMapper.Adapters;

import cz.cuni.mff.ConfigMapper.ConfigurationException;
import cz.cuni.mff.ConfigMapper.Nodes.Root;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A configuration adapter for INI files
 */
public class IniAdapter implements ConfigAdapter {
	/**
	 * Parse config from an INI file
	 *
	 * @param input The input stream
	 * @throws ConfigurationException When the input file is invalid
	 * @return The configuration structure
	 */
	@Override
	public Root read(InputStream input) throws ConfigurationException {
		return null;
	}

	/**
	 * Write config into an INI file
	 *
	 * @param config The configuration structure
	 * @param output The output stream
	 */
	@Override
	public void write(Root config, OutputStream output) {

	}
}
