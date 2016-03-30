package cz.cuni.mff.ConfigMapper.Nodes;

public class SimpleValue extends Value {
	
	private String value;

	public String getValue() {
		return value;
	}

	public SimpleValue(String name, Type type) {
		super(name, type);
	}

	public void setValue(String value) {
		// TODO Auto-generated method stub
		this.value = value;
	}

}
