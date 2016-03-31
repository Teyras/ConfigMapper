package cz.cuni.mff.ConfigMapper.Nodes;

import java.util.List;

/**
 * The root node of a configuration
 */
public class Root extends Section {
	public Root(String name, List<ConfigNode> children) {
		super(name, children);
	}
}
