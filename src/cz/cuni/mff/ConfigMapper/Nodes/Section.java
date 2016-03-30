package cz.cuni.mff.ConfigMapper.Nodes;

import java.util.List;

public class Section extends ConfigNode {

	private List<Value> values;
	
	public Section(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return the values
	 */
	public List<Value> getValues() {
		return values;
	}

	/**
	 * @param values the values to set
	 */
	public void setValues(List<Value> values) {
		this.values = values;
	}

}
