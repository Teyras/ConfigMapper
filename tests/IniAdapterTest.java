import cz.cuni.mff.ConfigMapper.IniAdapter;
import cz.cuni.mff.ConfigMapper.Nodes.Root;
import cz.cuni.mff.ConfigMapper.Nodes.Section;
import cz.cuni.mff.ConfigMapper.Nodes.SimpleValue;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
		Root config = adapter.read(new ByteArrayInputStream(input));

		// The configuration must be a root node that contains two sections
		assertNotNull(config);
		assertEquals(2, config.getSections().size());

		Section[] sections = new Section[2];
		config.getSections().toArray(sections);

		// Check the options in the first section
		assertEquals("sectionA", sections[0].getName());
		assertEquals(2, sections[0].getValues().size());
		SimpleValue[] sectionAOptions = new SimpleValue[2];
		sections[0].getValues().toArray(sectionAOptions);

		assertEquals("option1", sectionAOptions[0].getName());
		assertEquals("foo", sectionAOptions[0].getValue());
		assertEquals("option2", sectionAOptions[1].getName());
		assertEquals("bar", sectionAOptions[1].getValue());

		// Check the options in the second section
		assertEquals("sectionB", sections[0].getName());
		assertEquals(1, sections[1].getValues().size());
		SimpleValue[] sectionBOptions = new SimpleValue[1];
		sections[1].getValues().toArray(sectionBOptions);

		assertEquals("option3", sectionBOptions[0].getName());
		assertEquals("baz", sectionBOptions[0].getValue());
	}
}