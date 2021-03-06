package cz.cuni.mff.ConfigMapper.Nodes;

import java.util.List;

/**
 * The root node of a configuration
 */
public final class ConfigRoot extends Section {
	/**
	 * @param name the name of the section
	 * @param children a list of children of the section
	 */
	public ConfigRoot(String name, List<ConfigNode> children) {
		super(name, children);
	}
}
