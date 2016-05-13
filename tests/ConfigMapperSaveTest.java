import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.Annotations.ConstantAlias;
import cz.cuni.mff.ConfigMapper.Annotations.UndeclaredOptions;
import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.Nodes.ListOption;
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

	static class WithList {
		@ConfigOption(section = "section")
		List<String> list;
	}

	@Test
	public void testList() throws Exception {
		List<String> list = new ArrayList<>(Arrays.asList("foo", "bar", "baz"));

		WithList object = new WithList();
		object.list = list;

		ConfigMapper mapper = new ConfigMapper();
		Root config = mapper.save(object);

		Root expected = new Root("", Arrays.asList(
			new Section("section", Collections.singletonList(
				new ListOption("list", Arrays.asList("foo", "bar", "baz"))
			))
		));

		assertEquals(expected, config);
	}

	static class WithEnum {
		enum OptionEnum {
			ON,
			OFF
		}

		@ConfigOption(section = "section")
		@ConstantAlias(constant = "OFF", alias = "OOFF")
		OptionEnum option1;

		@ConfigOption(section = "section")
		OptionEnum option2;
	}

	@Test
	public void testEnum() throws Exception {
		WithEnum object = new WithEnum();
		object.option1 = WithEnum.OptionEnum.OFF;
		object.option2 = WithEnum.OptionEnum.ON;

		ConfigMapper mapper = new ConfigMapper();
		Root config = mapper.save(object);

		Root expected = new Root("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option1", "OOFF"),
				new ScalarOption("option2", "ON")
			))
		));

		assertEquals(expected, config);
	}

	static class WithChainedEnum {
		enum OptionEnum {
			ON,
			OFF
		}

		@ConfigOption(section = "section")
		@ConstantAlias(constant = "OFF", alias = "ON")
		@ConstantAlias(constant = "ON", alias = "OFF")
		OptionEnum option1;

		@ConfigOption(section = "section")
		@ConstantAlias(constant = "OFF", alias = "ON")
		@ConstantAlias(constant = "ON", alias = "OFF")
		OptionEnum option2;
	}

	@Test
	public void testChainedEnum() throws Exception {
		WithChainedEnum object = new WithChainedEnum();
		object.option1 = WithChainedEnum.OptionEnum.ON;
		object.option2 = WithChainedEnum.OptionEnum.OFF;

		ConfigMapper mapper = new ConfigMapper();
		Root config = mapper.save(object);

		Root expected = new Root("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option1", "OFF"),
				new ScalarOption("option2", "ON")
			))
		));

		assertEquals(expected, config);
	}
}
