package cz.cuni.mff.ConfigMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by teyras on 29.3.16.
 */
public class ConfigNode {
    public enum Type {
        LIST,
        SECTION,
        SIMPLE_VALUE,
        ROOT_NODE
    }

    public Type getType() {
        return Type.SIMPLE_VALUE;
    }

    public String getName() {
        return "";
    }

    public List<String> getListValue() {
        return new ArrayList<>();
    }
}
