package cz.cuni.mff.ConfigMapper.Nodes;

import java.util.List;

public class Section extends ConfigNode {

	private List<ConfigNode> children;

	public Section(String name) {
		super(name);
	}

	/**
	 * @return the children
	 */
	public List<ConfigNode> getChildren() {
		return children;
	}

	/**
	 * @param children the children to set
	 */
	public void setChildren(List<ConfigNode> children) {
		this.children = children;
	}

	/**
	 * Does the section only contain values?
	 * @return true if the section only contains values (i.e. there are no subsections), false otherwise
	 */
	public boolean isFlat() {
		for (ConfigNode node : children) {
			if (!(node instanceof Value)) {
				return false;
			}
		}

		return true;
	}
}
