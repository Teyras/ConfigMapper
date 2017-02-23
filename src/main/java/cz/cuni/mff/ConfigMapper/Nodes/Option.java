package cz.cuni.mff.ConfigMapper.Nodes;

/**
 * A common ancestor for all options that can't contain other nodes
 */
public abstract class Option extends ConfigNode {
	/**
	 * @param name the name of the option
	 */
	Option(String name) {
		super(name);
	}
}
