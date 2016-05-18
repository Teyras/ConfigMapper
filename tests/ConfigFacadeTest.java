import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.Annotations.ConfigSection;
import cz.cuni.mff.ConfigMapper.Annotations.UndeclaredOptions;
import cz.cuni.mff.ConfigMapper.ConfigFacade;
import cz.cuni.mff.ConfigMapper.Adapters.IniAdapter;
import cz.cuni.mff.ConfigMapper.LoadingMode;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

class StringInputStream extends ByteArrayInputStream {
	public StringInputStream(String... lines) {
		super(String.join("\n", Arrays.asList(lines)).getBytes());
	}
}

public class ConfigFacadeTest {
	static class BasicMappedClass {
		@ConfigOption(section = "section1")
		public String optionString;

		@ConfigOption(section = "section1")
		public int optionInt;

		@ConfigOption(section = "section2")
		public boolean optionBool;
	}
	
	@Test
	public void loadIniBasic() throws Exception {
		StringInputStream input = new StringInputStream(
			"[section1]",
			"optionString = value",
			"optionInt = 234",
			"[section2]",
			"optionBool = on"
		);

		ConfigFacade facade = new ConfigFacade(new IniAdapter());
		BasicMappedClass object = facade.load(input, BasicMappedClass.class, LoadingMode.STRICT);

		assertEquals("value", object.optionString);
		assertEquals(234, object.optionInt);
		assertEquals(true, object.optionBool);
	}

	static class BasicMappedRelaxedClass {
		@ConfigOption(section = "section1")
		public String optionString;

		@ConfigOption(section = "section2")
		public boolean optionBool;

		@UndeclaredOptions
		public Map<String,String> undeclaredOpts = new HashMap<>();
	}

	@Test
	public void loadIniRelaxed() throws Exception {
		StringInputStream input = new StringInputStream(
			"[section1]",
			"optionString = value",
			"[section2]",
			"optionInt = 234",
			"optionBool = on"
		);

		ConfigFacade facadeRelaxed = new ConfigFacade(new IniAdapter());
		BasicMappedRelaxedClass objectRelaxed = facadeRelaxed.load(input, BasicMappedRelaxedClass.class, LoadingMode.RELAXED);

		assertEquals(true, objectRelaxed.optionBool);
		assertEquals("value", objectRelaxed.optionString);
		assertEquals("234", objectRelaxed.undeclaredOpts.get("section2#optionInt"));
	}

	static class NestedSectionMappedClass {
		static class FooSection {
			@ConfigOption(description = "An option")
			public String option = "A default value";
		}

		@ConfigSection(description = "Section A")
		FooSection sectionA;

		@ConfigSection(description = "Section B")
		FooSection sectionB;
	}

	@Test
	public void loadIniNested() throws Exception {
		StringInputStream input = new StringInputStream(
			"[sectionA]",
			"option = value",
			"[sectionB]",
			"option = value2"
		);

		ConfigFacade facade = new ConfigFacade(new IniAdapter());
		NestedSectionMappedClass object = facade.load(input, NestedSectionMappedClass.class, LoadingMode.STRICT);

		assertEquals(object.sectionA.option, "value");
		assertEquals(object.sectionB.option, "value2");
	}

	@Test
	public void saveDefaultsIni() throws Exception {
		ConfigFacade facade = new ConfigFacade(new IniAdapter());

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		facade.saveDefaults(NestedSectionMappedClass.class, output);

		String expected = String.join("\n", Arrays.asList(
			"[sectionA]",
			"; Section A",
			"option=A default value\t;An option",
			"[sectionB]",
			"; Section B",
			"option=A default value\t;An option",
			""
		));

		assertEquals(expected, output.toString());
	}

	static class DefaultValueMappedClass {
		@ConfigOption(section = "section", optional = true)
		int option = 100;
	}

	@Test
	public void saveDefaultValueExplicit() throws Exception {
		StringInputStream input = new StringInputStream(
			"[section]",
			"option = 100"
		);

		ConfigFacade facade = new ConfigFacade(new IniAdapter());
		DefaultValueMappedClass object = facade.load(input, DefaultValueMappedClass.class, LoadingMode.STRICT);

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		facade.save(object, output);

		String expected = String.join("\n", Arrays.asList(
			"[section]",
			"option=100",
			""
		));

		assertEquals(expected, output.toString());
	}
}