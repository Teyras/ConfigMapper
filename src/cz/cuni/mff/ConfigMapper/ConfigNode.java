package cz.cuni.mff.ConfigMapper;

import java.util.List;

/**
 * Created by teyras on 29.3.16.
 */
public final class ConfigNode {
    public enum Type {
        LIST,
        SECTION,
        SIMPLE_VALUE,
        ROOT_NODE
    }

    private Type type;

    private String name;

    private String simpleValue;

    private List<String> listValue;

    private List<ConfigNode> sections;

    public ConfigNode(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getSimpleValue() {
        return simpleValue;
    }

    public void setSimpleValue(String simpleValue) {
        this.simpleValue = simpleValue;
    }

    public List<String> getListValue() {
        return listValue;
    }

    public void setListValue(List<String> listValue) {
        this.listValue = listValue;
    }

    public List<ConfigNode> getSections() {
        return sections;
    }

    public void setSections(List<ConfigNode> sections) {
        this.sections = sections;
    }
}
