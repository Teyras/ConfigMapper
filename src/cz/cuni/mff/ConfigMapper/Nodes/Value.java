package cz.cuni.mff.ConfigMapper.Nodes;

public abstract class Value extends ConfigNode {

	public enum Type {
		Boolean,
		Signed,
		Unsigned,
		Float,
		String,
		Enum 
	}
	
	private Type type;
	
	public Value(String name, Type type) {
		super(name);
		this.type = type;
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

}
