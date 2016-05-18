import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.Annotations.ConfigSection;
import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.MappingException;
import cz.cuni.mff.ConfigMapper.Nodes.ConfigRoot;
import cz.cuni.mff.ConfigMapper.Nodes.ScalarOption;
import cz.cuni.mff.ConfigMapper.Nodes.Section;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

public class ConfigMapperSaveBasicTest {
	static class BasicMappedClass {
		@ConfigOption(section = "section1", description = "Lorem Ipsum")
		String optionString;

		@ConfigOption(section = "section1")
		int optionInt;
	}

	@Test
	public void saveBasic() throws Exception {
		BasicMappedClass object = new BasicMappedClass();
		object.optionString = "foo";
		object.optionInt = 10;

		ConfigMapper mapper = new ConfigMapper();
		ConfigRoot config = mapper.save(object, null, false);

		ScalarOption optionString = new ScalarOption("optionString", "foo");
		optionString.setDescription("Lorem Ipsum");

		ConfigRoot expected = new ConfigRoot("", Arrays.asList(
			new Section("section1", Arrays.asList(
				optionString,
				new ScalarOption("optionInt", "10")
			))
		));

		assertEquals(expected, config);
	}

	@Test(expected = MappingException.class)
	public void missingOptionThrows() throws Exception {
		BasicMappedClass object = new BasicMappedClass();
		object.optionString = null;
		object.optionInt = 10;

		ConfigMapper mapper = new ConfigMapper();
		mapper.save(object, null, false);
	}

	static class StructuredMappedClass {
		static class SectionClass {
			@ConfigOption(name = "optionStringFoo")
			String optionString;

			@ConfigOption
			int optionInt;
		}

		@ConfigSection(description = "Section 1")
		SectionClass section1;
	}

	@Test
	public void saveStructured() throws Exception {
		StructuredMappedClass object = new StructuredMappedClass();
		object.section1 = new StructuredMappedClass.SectionClass();
		object.section1.optionString = "foo";
		object.section1.optionInt = 10;

		ConfigMapper mapper = new ConfigMapper();
		ConfigRoot config = mapper.save(object, null, false);

		Section section = new Section("section1", Arrays.asList(
			new ScalarOption("optionStringFoo", "foo"),
			new ScalarOption("optionInt", "10")
		));
		section.setDescription("Section 1");

		ConfigRoot expected = new ConfigRoot("", Arrays.asList(
			section
		));

		assertEquals(expected, config);
	}

	@Test(expected = MappingException.class)
	public void missingSectionThrows() throws Exception {
		StructuredMappedClass object = new StructuredMappedClass();
		object.section1 = null;

		ConfigMapper mapper = new ConfigMapper();
		mapper.save(object, null, false);
	}

	static class OptionalOptionMappedClass {
		@ConfigOption(section = "section", optional = true)
		Integer option;
	}

	@Test
	public void saveOptionalOptionNotPresent() throws Exception {
		OptionalOptionMappedClass object = new OptionalOptionMappedClass();
		object.option = null;

		ConfigMapper mapper = new ConfigMapper();
		ConfigRoot config = mapper.save(object, null, false);

		ConfigRoot expected = new ConfigRoot("", Collections.emptyList());

		assertEquals(expected, config);
	}

	static class OptionalSectionMappedClass {
		static class SectionClass {
			@ConfigOption
			int option;
		}

		@ConfigSection(optional = true)
		SectionClass section;
	}

	@Test
	public void saveOptionalSectionNotPresent() throws Exception {
		OptionalSectionMappedClass object = new OptionalSectionMappedClass();
		object.section = null;

		ConfigMapper mapper = new ConfigMapper();
		ConfigRoot config = mapper.save(object, null, false);

		ConfigRoot expected = new ConfigRoot("", Collections.emptyList());

		assertEquals(expected, config);
	}

	static class DefaultValuesMappedClass {
		@ConfigOption(section = "section", optional = true)
		int option = 42;
	}

	@Test
	public void saveWithoutDefaults() throws Exception {
		DefaultValuesMappedClass object = new DefaultValuesMappedClass();

		ConfigMapper mapper = new ConfigMapper();
		ConfigRoot config = mapper.save(object, null, false);

		ConfigRoot expected = new ConfigRoot("", Collections.emptyList());
		assertEquals(expected, config);
	}

	@Test
	public void saveNonDefaults() throws Exception {
		DefaultValuesMappedClass object = new DefaultValuesMappedClass();
		object.option = 123;

		ConfigMapper mapper = new ConfigMapper();
		ConfigRoot config = mapper.save(object, null, false);

		ConfigRoot expected = new ConfigRoot("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option", "123")
			))
		));
		assertEquals(expected, config);
	}
}
