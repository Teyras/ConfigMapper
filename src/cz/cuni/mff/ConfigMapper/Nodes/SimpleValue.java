package cz.cuni.mff.ConfigMapper.Nodes;

public class SimpleValue extends Value {

	private String value;

	public SimpleValue(String name, String value) {
		super(name);
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
