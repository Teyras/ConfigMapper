package cz.cuni.mff.ConfigMapper.Nodes;

import java.util.Objects;

/**
 * A representation of an element in the configuration file.
 * Its purpose is to hide the specifics of the configuration file format (e.g. INI)
 * from {@link cz.cuni.mff.ConfigMapper.ConfigMapper}.
 */
public abstract class ConfigNode {
	/**
	 * The name of the node
	 */
	protected String name;

	/**
	 * A description of the node (a comment)
	 */
	private String description = "";

	/**
	 * @param name the name of the node
	 */
	public ConfigNode(String name) {
		this.name = name;
	}

	/**
	 * Get the name of the node
	 * @return the name of the node
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the description of the node
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Check if the nodes are equal
	 * @param other the node to compare this one to
	 * @return true if the nodes are equal, false otherwise
	 */
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}

		return other != null
			&& getClass() == other.getClass()
			&& Objects.equals(this.description, ((ConfigNode) other).description)
			&& Objects.equals(this.name, ((ConfigNode) other).name);
	}
}
