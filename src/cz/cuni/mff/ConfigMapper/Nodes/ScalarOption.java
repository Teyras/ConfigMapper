package cz.cuni.mff.ConfigMapper.Nodes;

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
	 * @param name the name of the option
	 * @param value the value of the option
	 */
	public ScalarOption(String name, String value) {
		super(name);
		this.value = value;
	}

	/**
	 * Get the value of the option
	 * @return the value of the option
	 */
	public String getValue() {
		return value;
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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		ScalarOption other = (ScalarOption) obj;

		return Objects.equals(this.name, other.name) && Objects.equals(this.value, other.value);
	}
}
