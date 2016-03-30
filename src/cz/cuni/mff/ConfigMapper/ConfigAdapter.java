package cz.cuni.mff.ConfigMapper;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides reading and writing functionality for a configuration file format
 */
public interface ConfigAdapter {
    /**
     * Parse an input into a {@link ConfigNode} structure
     * @param input The input stream
     * @return The parsed tree
     */
    ConfigNode read(InputStream input);

    /**
     * Write a {@link ConfigNode} structure into an output stream
     * @param config The configuration structure
     * @param output The output stream
     */
    void write(ConfigNode config, OutputStream output);
}
