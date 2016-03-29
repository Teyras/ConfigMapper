import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.ConfigNode;
import cz.cuni.mff.ConfigMapper.ConfigOption;
import cz.cuni.mff.ConfigMapper.LoadingMode;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by teyras on 29.3.16.
 */
public class ConfigMapperTest {
    class BasicMappedClass {
        @ConfigOption(section = "section1")
        public String optionString;

        @ConfigOption(section = "section1")
        public int optionInt;

        @ConfigOption(section = "section2")
        public boolean optionBool;
    }

    @Test
    public void loadBasic() throws Exception {
        // Set up testing config structure
        ConfigNode section1 = new ConfigNode(ConfigNode.Type.SECTION, "section1");
        ConfigNode optionString = new ConfigNode(ConfigNode.Type.SIMPLE_VALUE, "optionString");
        optionString.setSimpleValue("value");
        ConfigNode optionInt = new ConfigNode(ConfigNode.Type.SIMPLE_VALUE, "optionInt");
        optionInt.setSimpleValue("10");
        section1.setChildNodes(Arrays.asList(optionString, optionInt));

        ConfigNode section2 = new ConfigNode(ConfigNode.Type.SECTION, "section2");
        ConfigNode optionBool = new ConfigNode(ConfigNode.Type.SIMPLE_VALUE, "optionBool");
        optionBool.setSimpleValue("on");
        section2.setChildNodes(Arrays.asList(optionBool));

        ConfigNode config = new ConfigNode(ConfigNode.Type.ROOT_NODE, "config.ini");
        config.setChildNodes(Arrays.asList(section1, section2));

        // Instantiate the mapper
        ConfigMapper<BasicMappedClass> mapper = new ConfigMapper<>(BasicMappedClass.class);
        BasicMappedClass object = mapper.load(config, LoadingMode.STRICT);

        // Check the loaded values
        assertEquals("value", object.optionString);
        assertEquals(10, object.optionInt);
        assertEquals(true, object.optionBool);
    }
}