package cz.cuni.mff.ConfigMapper.Adapters;

import cz.cuni.mff.ConfigMapper.ConfigurationException;
import cz.cuni.mff.ConfigMapper.Nodes.ConfigRoot;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides reading and writing functionality for a configuration file format
 */
public interface ConfigAdapter {
	/**
	 * Parse an input into a {@link ConfigRoot} structure
	 *
	 * @param input The input stream
	 * @throws ConfigurationException When the input file is invalid
	 * @return The parsed tree
	 */
	ConfigRoot read(InputStream input) throws ConfigurationException;

	/**
	 * Write a {@link ConfigRoot} structure into an output stream
	 *
	 * @param config The configuration structure
	 * @param output The output stream
	 * @throws ConfigurationException When the configuration cannot be written in this adapters format
	 */
	void write(ConfigRoot config, OutputStream output) throws ConfigurationException, IOException;
}
