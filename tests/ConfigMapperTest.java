import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.Annotations.ConfigSection;
import cz.cuni.mff.ConfigMapper.Annotations.UndeclaredOptions;
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
public class ConfigMapperTest {
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
}