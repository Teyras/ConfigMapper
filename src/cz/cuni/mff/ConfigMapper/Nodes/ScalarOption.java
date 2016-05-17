package cz.cuni.mff.ConfigMapper.Nodes;

import cz.cuni.mff.ConfigMapper.ParsedBoolean;

import java.util.Objects;

/**
 * Represents a scalar option value in the configuration file.
 * The value is kept in string form.
 */
public class ScalarOption extends Option {

	/**
	 * The value of the option
	 */
	private String value;

	/**
	 * The boolean value of the option, if applicable
	 */
	private ParsedBoolean booleanValue = ParsedBoolean.NOT_BOOLEAN;

	/**
	 * @param name the name of the option
	 * @param value the value of the option
	 */
	public ScalarOption(String name, String value) {
		super(name);
		this.value = value;
	}

	/**
	 * @param name name of the option
	 * @param value value of the option
	 * @param booleanValue boolean value of the option
	 */
	public ScalarOption(String name, String value, ParsedBoolean booleanValue) {
		this(name,value);
		this.booleanValue = booleanValue;
	}

	/**
	 * Get the value of the option
	 * @return the value of the option
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Get a boolean value of the option depending on the parsing context
	 * @return TRUE if true, FALSE if false, NOT_BOOLEAN if the option could not be
	 * mapped on a boolean value
	 */
	public ParsedBoolean getBooleanValue() {
		return booleanValue;
	}

	/**
	 * Set the boolean value of the option
	 * @param booleanValue the boolean value of the option
	 */
	public void setBooleanValue(ParsedBoolean booleanValue) {
		this.booleanValue = booleanValue;
	}

	/**
	 * Check if both objects contain the same options
	 * @param obj the other object
	 * @return true if the options are equal, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!super.equals(obj)) {
			return false;
		}
		
		ScalarOption other = (ScalarOption) obj;

		return Objects.equals(this.value, other.value);
	}
}
