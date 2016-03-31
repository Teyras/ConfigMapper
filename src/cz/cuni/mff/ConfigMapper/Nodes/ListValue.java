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
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		ListValue other = (ListValue) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
	
	
}
