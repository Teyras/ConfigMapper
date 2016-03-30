package cz.cuni.mff.ConfigMapper.Nodes;

import java.util.List;

public class Root extends ConfigNode {

	// Zvolit jinou datovou strukturu, nez list?
	private List<Section> sections;
	
	public Root(String name) {
		super(name);
	}
	
	public boolean AddSection(Section newSection) {
		return sections.add(newSection);
	}
	
	public void setSetions(List<Section> sections) {
		this.sections = sections;
	}

	public List<Section> getSections() {
		return sections;
	}

}
