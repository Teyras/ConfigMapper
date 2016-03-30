import cz.cuni.mff.ConfigMapper.*;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;

class StringInputStream extends ByteArrayInputStream {
    public StringInputStream(String input) {
        super(input.getBytes());
    }
}

/**
 * Created by teyras on 30.3.16.
 */
public class ConfigFacadeTest {
    class BasicMappedClass {
        @ConfigOption(section = "section1")
        public String optionString;

        @ConfigOption(section = "section1")
        public int optionInt;

        @ConfigOption(section = "section2")
        public boolean optionBool;
    }

    @Test
    public void loadIniBasic() throws Exception {
        StringInputStream input = new StringInputStream(
           "[sectionA]\n" +
           "optionString = value\n" +
           "[sectionB]\n" +
           "optionInt = 234\n" +
           "optionBool = true\n"
        );

        ConfigFacade<BasicMappedClass> facade = new ConfigFacade<>(BasicMappedClass.class, new IniAdapter());
        BasicMappedClass object = facade.load(input, LoadingMode.STRICT);

        assertEquals(object.optionString, "value");
        assertEquals(object.optionInt, 234);
        assertEquals(object.optionBool, true);
    }

    class NestedSectionMappedClass {
        class FooSection {
            @ConfigOption
            public String option;
        }

        @ConfigSection
        FooSection sectionA;

        @ConfigSection
        FooSection sectionB;
    }

    @Test
    public void loadIniNested() throws Exception {
        StringInputStream input = new StringInputStream(
                "[sectionA]\n" +
                "option = value\n" +
                "[sectionB]\n" +
                "option = value2\n"
        );

        ConfigFacade<NestedSectionMappedClass> facade = new ConfigFacade<>(NestedSectionMappedClass.class, new IniAdapter());
        NestedSectionMappedClass object = facade.load(input, LoadingMode.STRICT);

        assertEquals(object.sectionA.option, "value");
        assertEquals(object.sectionB.option, "value2");
    }
}