import cz.cuni.mff.ConfigMapper.ConfigNode;
import cz.cuni.mff.ConfigMapper.IniAdapter;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.*;

/**
 * Created by teyras on 29.3.16.
 */
public class IniAdapterTest {

    @Test
    public void readBasic() throws Exception {
        byte[] input = (
            "[sectionA]\n" +
            "option1=foo\n" +
            "option2=bar\n" +
            "[sectionB]\n" +
            "option3=baz\n"
        ).getBytes();

        IniAdapter adapter = new IniAdapter();
        ConfigNode config = adapter.read(new ByteArrayInputStream(input));

        // The configuration must be a root node that contains two sections
        assertNotNull(config);
        assertEquals(ConfigNode.Type.ROOT_NODE, config.getType());
        assertEquals(2, config.getChildNodes().size());

        ConfigNode[] sections = new ConfigNode[2];
        config.getChildNodes().toArray(sections);

        assertEquals(ConfigNode.Type.SECTION, sections[0].getType());
        assertEquals(ConfigNode.Type.SECTION, sections[1].getType());

        // Check the options in the first section
        assertEquals("sectionA", sections[0].getName());
        assertEquals(2, sections[0].getChildNodes().size());
        ConfigNode[] sectionAOptions = new ConfigNode[2];
        sections[0].getChildNodes().toArray(sectionAOptions);

        assertEquals(ConfigNode.Type.SIMPLE_VALUE, sectionAOptions[0].getType());
        assertEquals("option1", sectionAOptions[0].getName());
        assertEquals("foo", sectionAOptions[0].getSimpleValue());
        assertEquals(ConfigNode.Type.SIMPLE_VALUE, sectionAOptions[1].getType());
        assertEquals("option2", sectionAOptions[1].getName());
        assertEquals("bar", sectionAOptions[1].getSimpleValue());

        // Check the options in the second section
        assertEquals("sectionB", sections[0].getName());
        assertEquals(1, sections[1].getChildNodes().size());
        ConfigNode[] sectionBOptions = new ConfigNode[1];
        sections[1].getChildNodes().toArray(sectionBOptions);

        assertEquals(ConfigNode.Type.SIMPLE_VALUE, sectionBOptions[0].getType());
        assertEquals("option3", sectionBOptions[0].getName());
        assertEquals("baz", sectionBOptions[0].getSimpleValue());
    }
}