package cz.cuni.mff.ConfigMapper.Nodes;

/**
 * Created by teyras on 29.3.16.
 */
public abstract class ConfigNode {
	private String name;

	private String description;

	public ConfigNode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	/**
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

}
