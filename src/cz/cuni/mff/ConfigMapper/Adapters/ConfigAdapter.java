package cz.cuni.mff.ConfigMapper.Adapters;

import cz.cuni.mff.ConfigMapper.Nodes.Root;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides reading and writing functionality for a configuration file format
 */
public interface ConfigAdapter {
	/**
	 * Parse an input into a {@link Root} structure
	 *
	 * @param input The input stream
	 * @return The parsed tree
	 */
	Root read(InputStream input);

	/**
	 * Write a {@link Root} structure into an output stream
	 *
	 * @param config The configuration structure
	 * @param output The output stream
	 */
	void write(Root config, OutputStream output);
}
