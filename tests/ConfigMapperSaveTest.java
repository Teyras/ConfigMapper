import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.Annotations.UndeclaredOptions;
import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.Nodes.Root;
import cz.cuni.mff.ConfigMapper.Nodes.ScalarOption;
import cz.cuni.mff.ConfigMapper.Nodes.Section;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by teyras on 9.5.16.
 */
public class ConfigMapperSaveTest {
	static class BasicMappedClass {
		@ConfigOption(section = "section1")
		String optionString;

		@ConfigOption(section = "section1")
		int optionInt;
	}

	@Test
	public void testBasic() throws Exception {
		BasicMappedClass object = new BasicMappedClass();
		object.optionString = "foo";
		object.optionInt = 10;

		ConfigMapper mapper = new ConfigMapper();
		Root config = mapper.save(object);

		Root expected = new Root("", Arrays.asList(
			new Section("section1", Arrays.asList(
				new ScalarOption("optionString", "foo"),
				new ScalarOption("optionInt", "10")
			))
		));

		assertEquals(expected, config);
	}

	static class WithUndeclaredOptions {
		@UndeclaredOptions
		Map<String, String> options;
	}

	@Test
	public void testUndeclaredOpts() throws Exception {
		WithUndeclaredOptions object = new WithUndeclaredOptions();
		object.options = new LinkedHashMap<>();
		object.options.put("section1#foo", "bar");
		object.options.put("section2#baz", "gah");

		ConfigMapper mapper = new ConfigMapper();
		Root config = mapper.save(object);

		Root expected = new Root("", Arrays.asList(
			new Section("section1", Collections.singletonList(
				new ScalarOption("foo", "bar")
			)),
			new Section("section2", Collections.singletonList(
				new ScalarOption("baz", "gah")
			))
		));

		assertEquals(expected, config);
	}
}
