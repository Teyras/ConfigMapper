package cz.cuni.mff.ConfigMapper;

/**
 * Thrown when there is something wrong with mapping a configuration onto an object
 */
public class MappingException extends Exception {
	public MappingException(String message) {
		super(message);
	}

	public MappingException(String message, Throwable cause) {
		super(message, cause);
	}
}
