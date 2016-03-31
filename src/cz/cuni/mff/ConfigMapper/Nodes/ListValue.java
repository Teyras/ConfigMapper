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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ListValue other = (ListValue) obj;
		
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		
		if (value == null) {
			if (other.value != null) {
				return false;
			}
		} else if (!value.equals(other.value)) {
			return false;
		}
		return true;
	}
}
