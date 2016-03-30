import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.ConfigOption;
import cz.cuni.mff.ConfigMapper.LoadingMode;
import cz.cuni.mff.ConfigMapper.Nodes.*;

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
        Section section1 = new Section("section1");
        SimpleValue optionString = new SimpleValue("optionString", Value.Type.String);
        optionString.setValue("value");
        SimpleValue optionInt = new SimpleValue("optionInt", Value.Type.Signed);
        optionInt.setValue("10");
        section1.setValues(Arrays.asList(optionString, optionInt));

        Section section2 = new Section("section2");
        SimpleValue optionBool = new SimpleValue("optionBool", Value.Type.Boolean);
        optionBool.setValue("on");
        section2.setValues(Arrays.asList(optionBool));

        Root config = new Root("config.ini");
        config.setSetions(Arrays.asList(section1, section2));

        // Instantiate the mapper
        ConfigMapper<BasicMappedClass> mapper = new ConfigMapper<>(BasicMappedClass.class);
        BasicMappedClass object = mapper.load(config, LoadingMode.STRICT);

        // Check the loaded values
        assertEquals("value", object.optionString);
        assertEquals(10, object.optionInt);
        assertEquals(true, object.optionBool);
    }
}