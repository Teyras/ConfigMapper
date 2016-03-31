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
		
		SimpleValue other = (SimpleValue) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
