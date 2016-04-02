package cz.cuni.mff.ConfigMapper.Nodes;

import java.util.List;

/**
 * Represents a section of the configuration file - an entity that groups similar options
 */
public class Section extends ConfigNode {

	/**
	 * A list of nodes contained in this section
	 */
	private List<ConfigNode> children;

	/**
	 * @param name the name of the section
	 * @param children a list of children of the section
	 */
	public Section(String name, List<ConfigNode> children) {
		super(name);
		this.children = children;
	}

	/**
	 * Get the nodes contained in this section
	 * @return the child nodes
	 */
	public List<ConfigNode> getChildren() {
		return children;
	}

	/**
	 * Does the section only contain values?
	 * @return true if the section only contains values (i.e. there are no subsections), false otherwise
	 */
	public boolean isFlat() {
		for (ConfigNode node : children) {
			if (!(node instanceof Option)) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Check if both objects are sections and if their children are equal
	 * @param obj the object to compare this section to
	 * @return true if both objects are equal sections that have equal children, false otherwise
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
		
		Section other = (Section) obj;
		
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		
		if (children == null) {
			if (other.children != null) {
				return false;
			}
		} else if (!children.equals(other.children)) {
			return false;
		}
		return true;
	}

	
}
