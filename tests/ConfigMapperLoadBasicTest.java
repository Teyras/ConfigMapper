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
public class ConfigMapperLoadBasicTest {
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

	@Test(expected = MappingException.class)
	public void missingOptionThrows() throws Exception {
		Root config = new Root("", Arrays.asList(
			new Section("section1", Arrays.asList(
				new ScalarOption("optionString", "foo"),
				new ScalarOption("optionInt", "110")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		BasicMappedClass object = mapper.load(config, BasicMappedClass.class, LoadingMode.STRICT);
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

	static class OptionalOptionMappedClass {
		@ConfigOption(section = "section", optional = true)
		int option;
	}

	@Test
	public void loadOptionalOption() throws Exception {
		Root config = new Root("", Collections.emptyList());

		ConfigMapper mapper = new ConfigMapper();
		OptionalOptionMappedClass object = mapper.load(config, OptionalOptionMappedClass.class, LoadingMode.STRICT);
		
		assertEquals(object.option, 0);
	}
}