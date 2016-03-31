package cz.cuni.mff.ConfigMapper.Nodes;

import java.util.List;

public class ListValue extends Value {

	private List<String> value;

	public ListValue(String name) {
		super(name);
	}

	public List<String> getValue() {
		return value;
	}

	public void setValue(List<String> value) {
		this.value = value;
	}

}
