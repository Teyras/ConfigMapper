package cz.cuni.mff.ConfigMapper.Nodes;

import java.util.List;
import java.util.Objects;

/**
 * Represents a list of values in the configuration file.
 * The field must be initialized by the default constructor.
 */
public final class ListOption extends Option {

	/**
	 * The list of values
	 */
	private List<String> value;

	private String separator;

	/**
	 * Default constructor, which lets the class itself determine the separator
	 * @param name the name of the option
	 * @param value the value of the option
	 */
	public ListOption(String name, List<String> value) {
		this(name,value,",");
	}

	/**
	 * Constructor enabling the specification of the separator
	 * @param name the name of the option
	 * @param value the value of the option
	 * @param separator separator
	 */
	public ListOption(String name, List<String> value, String separator) {
		super(name);
		this.value = value;

		if (separator.isEmpty()) {
			// iterate through list counting the occurrences of the separators
			int commas = 0;
			int colons = 0;
			for (String val : value) {
				commas += val.length() - val.replaceAll(",", "").length();
				colons += val.length() - val.replaceAll(":", "").length();
			}

			/*
			 * pick the separator, which minimizes escaping special characters when
			 * printing the list
			 */
			if (commas < colons) {
				this.separator = ",";
			} else {
				this.separator = ":";
			}

		} else {
			this.separator = separator;
		}
	}

	/**
	 * Get the values
	 * @return a list of values
	 */
	public List<String> getValue() {
		return value;
	}

	public String getSeparator() {return separator;}

	/**
	 * Check if both nodes contain the same lists
	 * @param obj the other node
	 * @return true if both objects are list nodes and their lists are equal, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!super.equals(obj)) {
			return false;
		}

		ListOption other = (ListOption) obj;

		return Objects.equals(this.value, other.value)
			&& Objects.equals(this.separator, other.separator);
	}
}
