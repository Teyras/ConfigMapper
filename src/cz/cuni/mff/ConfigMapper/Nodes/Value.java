package cz.cuni.mff.ConfigMapper.Nodes;

/**
 * A common ancestor for all options that can't contain other nodes
 */
public abstract class Value extends ConfigNode {
	/**
	 * @param name the name of the option
	 */
	public Value(String name) {
		super(name);
	}
}
