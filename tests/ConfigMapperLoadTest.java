import cz.cuni.mff.ConfigMapper.Annotations.*;
import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.LoadingMode;
import cz.cuni.mff.ConfigMapper.MappingException;
import cz.cuni.mff.ConfigMapper.Nodes.*;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by teyras on 29.3.16.
 */
public class ConfigMapperLoadTest {
	static class BasicMappedClass {
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
		Section section1 = new Section("section1", Arrays.asList(
			new ScalarOption("optionString", "value"),
			new ScalarOption("optionInt", "10")
		));

		Section section2 = new Section("section2", Arrays.asList(
			new ScalarOption("optionBool", "on")
		));

		Root config = new Root("config.ini", Arrays.asList(
			section1,
			section2
		));

		// Instantiate the mapper
		ConfigMapper mapper = new ConfigMapper();
		BasicMappedClass object = mapper.load(config, BasicMappedClass.class, LoadingMode.STRICT);

		// Check the loaded values
		assertEquals("value", object.optionString);
		assertEquals(10, object.optionInt);
		assertEquals(true, object.optionBool);
	}

	static class WithList {
		@ConfigOption(section = "section")
		List<String> list;
	}

	@Test
	public void loadList() throws Exception {
		List<String> listValue = Arrays.asList(
			"foo",
			"bar",
			"baz"
		);

		Root config = new Root("", Collections.singletonList(
			new Section("section", Collections.singletonList(
				new ListOption("list", listValue,',')
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		WithList object = mapper.load(config, WithList.class, LoadingMode.STRICT);

		assertEquals(listValue, object.list);

		// Make sure that the list can be modified
		object.list.add("boo");
		object.list.remove(0);

		assertEquals(Arrays.asList("bar", "baz", "boo"), object.list);
	}

	@Test(expected = MappingException.class)
	public void loadUndeclaredThrows() throws Exception {
		// Set up testing config structure
		Section section1 = new Section("section1", Arrays.asList(
			new ScalarOption("optionFooBarBaz", "value")
		));

		Root config = new Root("config.ini", Arrays.asList(
			section1
		));

		// Instantiate the mapper
		ConfigMapper mapper = new ConfigMapper();
		BasicMappedClass object = mapper.load(config, BasicMappedClass.class, LoadingMode.STRICT);
	}

	static class WithUndeclaredOptions {
		@UndeclaredOptions
		public Map<String, String> other = new HashMap<>();
	}

	@Test
	public void loadRelaxed() throws Exception {
		// Set up testing config structure
		Section section1 = new Section("section1", Arrays.asList(
			new ScalarOption("option1", "value"),
			new ScalarOption("option2", "10")
		));

		Root config = new Root("config.ini", Arrays.asList(
			section1
		));

		// Instantiate the mapper
		ConfigMapper mapper = new ConfigMapper();
		WithUndeclaredOptions object = mapper.load(config, WithUndeclaredOptions.class, LoadingMode.RELAXED);

		// Check the loaded values
		Map<String, String> expected = new HashMap<>();
		expected.put("section1#option1", "value");
		expected.put("section1#option2", "10");

		assertEquals(expected, object.other);
	}

	static class MultipleUndeclaredOptionsFields {
		@UndeclaredOptions
		public Map<String, String> other1 = new HashMap<>();

		@UndeclaredOptions
		public Map<String, String> other2 = new HashMap<>();
	}

	@Test(expected = MappingException.class)
	public void loadRelaxedMultipleUndeclaredContainersThrows() throws Exception {
		Root config = new Root("config.ini", Collections.emptyList());

		// Instantiate the mapper
		ConfigMapper mapper = new ConfigMapper();
		MultipleUndeclaredOptionsFields object = mapper.load(config, MultipleUndeclaredOptionsFields.class, LoadingMode.RELAXED);
	}

	@Test(expected = MappingException.class)
	public void loadRelaxedNoUndeclaredContainersThrows() throws Exception {
		Root config = new Root("config.ini", Collections.emptyList());

		// Instantiate the mapper
		ConfigMapper mapper = new ConfigMapper();
		BasicMappedClass object = mapper.load(config, BasicMappedClass.class, LoadingMode.RELAXED);
	}

	static class StructuredSectionMappedClass {
		static class FooSection {
			@ConfigOption
			public String option;
		}

		@ConfigSection
		FooSection sectionA;

		@ConfigSection(name = "sectionAA")
		FooSection sectionB;
	}

	@Test
	public void loadStructured() throws Exception {
		Root config = new Root("", Arrays.asList(
			new Section("sectionA", Arrays.asList(
				new ScalarOption("option", "value")
			)),
			new Section("sectionAA", Arrays.asList(
				new ScalarOption("option", "value2")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		StructuredSectionMappedClass object = mapper.load(config, StructuredSectionMappedClass.class, LoadingMode.STRICT);

		assertEquals(object.sectionA.option, "value");
		assertEquals(object.sectionB.option, "value2");
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
	public void loadEnum() throws Exception {
		Root config = new Root("", Collections.singletonList(
			new Section("section", Arrays.asList(
				new ScalarOption("option1", "OOFF"),
				new ScalarOption("option2", "OFF")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		WithEnum object = mapper.load(config, WithEnum.class, LoadingMode.STRICT);

		assertEquals(WithEnum.OptionEnum.OFF, object.option1);
		assertEquals(WithEnum.OptionEnum.OFF, object.option2);
	}

	@Test(expected = MappingException.class)
	public void loadEnumUnknownConstant() throws Exception {
		Root config = new Root("", Collections.singletonList(
			new Section("section", Arrays.asList(
				new ScalarOption("option1", "FOO"),
				new ScalarOption("option2", "BAR")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		WithEnum object = mapper.load(config, WithEnum.class, LoadingMode.STRICT);
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
		Root config = new Root("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option1", "OFF"),
				new ScalarOption("option2", "ON")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		WithChainedEnum object = mapper.load(config, WithChainedEnum.class, LoadingMode.STRICT);

		assertEquals(WithChainedEnum.OptionEnum.ON, object.option1);
		assertEquals(WithChainedEnum.OptionEnum.OFF, object.option2);
	}

	static class WithIntegralConstraint {
		@ConfigOption(section = "section")
		@IntegralConstraint(min = -10, max = 10)
		int option;
	}

	@Test(expected = MappingException.class)
	public void loadIntegralConstraintLow() throws Exception {
		Root config = new Root("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option", "-100")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		WithIntegralConstraint object = mapper.load(config, WithIntegralConstraint.class, LoadingMode.STRICT);
	}

	@Test(expected = MappingException.class)
	public void loadIntegralConstraintHigh() throws Exception {
		Root config = new Root("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option", "100")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		WithIntegralConstraint object = mapper.load(config, WithIntegralConstraint.class, LoadingMode.STRICT);
	}

	@Test()
	public void loadIntegralConstraintCorrect() throws Exception {
		Root config = new Root("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option", "2")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		WithIntegralConstraint object = mapper.load(config, WithIntegralConstraint.class, LoadingMode.STRICT);

		assertEquals(2, object.option);
	}

	static class WithDecimalConstraint {
		@ConfigOption(section = "section")
		@DecimalConstraint(min = -10.333, max = 10.333)
		double option;
	}

	@Test(expected = MappingException.class)
	public void loadDecimalConstraintLow() throws Exception {
		Root config = new Root("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option", "-100")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		WithDecimalConstraint object = mapper.load(config, WithDecimalConstraint.class, LoadingMode.STRICT);
	}

	@Test(expected = MappingException.class)
	public void loadDecimalConstraintHigh() throws Exception {
		Root config = new Root("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option", "100")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		WithDecimalConstraint object = mapper.load(config, WithDecimalConstraint.class, LoadingMode.STRICT);
	}

	@Test()
	public void loadDecimalConstraintCorrect() throws Exception {
		Root config = new Root("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option", "10.11")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		WithDecimalConstraint object = mapper.load(config, WithDecimalConstraint.class, LoadingMode.STRICT);

		assertEquals(10.11, object.option, 0.001);
	}
}