package cz.cuni.mff.ConfigMapper;

/**
 * Thrown when there is an error in a configuration file
 */
public class ConfigurationException extends Exception {
	public ConfigurationException(String message) {
		super(message);
	}

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
