package cz.cuni.mff.ConfigMapper.Nodes;

import java.util.List;

public class ListValue extends Value {

	private List<String> value;

	public ListValue(String name, List<String> value) {
		super(name);
		this.value = value;
	}

	public List<String> getValue() {
		return value;
	}
}
