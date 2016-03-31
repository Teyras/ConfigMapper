import cz.cuni.mff.ConfigMapper.Adapters.IniAdapter;
import cz.cuni.mff.ConfigMapper.Nodes.*;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by teyras on 29.3.16.
 */
public class IniAdapterTest {

	@Test
	public void readBasic() throws Exception {
		byte[] input = (String.join("%n",
			"[sectionA]",
			"option1=foo",
			"option2=bar",
			"[sectionB]",
			"option3=baz:baz:baz"
		)).getBytes();

		IniAdapter adapter = new IniAdapter();
		Root config = adapter.read(new ByteArrayInputStream(input));

		// The configuration must be a root node that contains two sections
		assertNotNull(config);
		assertEquals(2, config.getChildren().size());

		ConfigNode[] sections = new ConfigNode[2];
		config.getChildren().toArray(sections);

		assertEquals(true, sections[0] instanceof Section);
		assertEquals(true, sections[1] instanceof Section);

		// Check the options in the first section
		Section sectionA = (Section) sections[0];
		assertEquals("sectionA", sectionA.getName());
		assertEquals(2, sectionA.getChildren().size());

		ConfigNode[] sectionAOptions = new ConfigNode[2];
		sectionA.getChildren().toArray(sectionAOptions);

		assertEquals(true, sectionAOptions[0] instanceof SimpleValue);
		assertEquals(true, sectionAOptions[1] instanceof SimpleValue);

		SimpleValue option1 = (SimpleValue) sectionAOptions[0];
		assertEquals("option1", option1.getName());
		assertEquals("foo", option1.getValue());

		SimpleValue option2 = (SimpleValue) sectionAOptions[1];
		assertEquals("option2", option2.getName());
		assertEquals("bar", option2.getValue());

		// Check the options in the second section
		Section sectionB = (Section) sections[1];
		assertEquals("sectionB", sectionB.getName());
		assertEquals(1, sectionB.getChildren().size());

		ConfigNode[] sectionBOptions = new ConfigNode[1];
		sectionB.getChildren().toArray(sectionBOptions);

		assertEquals(true, sectionBOptions[0] instanceof ListValue);

		ListValue option3 = (ListValue) sectionBOptions[0];
		assertEquals("option3", option3.getName());
		assertEquals(Arrays.asList("baz", "baz", "baz"), option3.getValue());
	}
}