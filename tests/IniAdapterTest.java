import cz.cuni.mff.ConfigMapper.Adapters.IniAdapter;
import cz.cuni.mff.ConfigMapper.Nodes.*;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
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

		Root expectedConfig = new Root("", Arrays.asList(
			new Section("sectionA", Arrays.asList(
				new SimpleValue("option1", "foo"),
				new SimpleValue("option2", "bar")
			)),
			new Section("sectionB", Arrays.asList(
				new ListValue("option3", Arrays.asList("baz", "baz", "baz"))
			))
		));

		IniAdapter adapter = new IniAdapter();
		Root config = adapter.read(new ByteArrayInputStream(input));

		assertEquals(expectedConfig, config);
	}
}