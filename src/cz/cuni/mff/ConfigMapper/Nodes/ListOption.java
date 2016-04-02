package cz.cuni.mff.ConfigMapper.Nodes;

import java.util.List;

/**
 * Represents a list of values in the configuration file
 */
public class ListOption extends Option {

	/**
	 * The list of values
	 */
	private List<String> value;

	/**
	 * @param name the name of the option
	 * @param value the value of the option
	 */
	public ListOption(String name, List<String> value) {
		super(name);
		this.value = value;
	}

	/**
	 * Get the values
	 * @return a list of values
	 */
	public List<String> getValue() {
		return value;
	}

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
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ListOption other = (ListOption) obj;
		
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}
}
