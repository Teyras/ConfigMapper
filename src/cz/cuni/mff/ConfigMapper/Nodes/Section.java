package cz.cuni.mff.ConfigMapper.Nodes;

import java.util.List;

public class Section extends ConfigNode {

	private List<ConfigNode> children;

	public Section(String name, List<ConfigNode> children) {
		super(name);
		this.children = children;
	}

	/**
	 * @return the children
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
			if (!(node instanceof Value)) {
				return false;
			}
		}

		return true;
	}
}
