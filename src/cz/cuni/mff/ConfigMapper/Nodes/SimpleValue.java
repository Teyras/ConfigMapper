package cz.cuni.mff.ConfigMapper.Nodes;

public class SimpleValue extends Value {

	private String value;

	public String getValue() {
		return value;
	}

	public SimpleValue(String name) {
		super(name);
	}

	public void setValue(String value) {
		this.value = value;
	}

}
