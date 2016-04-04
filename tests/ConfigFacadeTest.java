import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.Annotations.ConfigSection;
import cz.cuni.mff.ConfigMapper.Annotations.UndeclaredOptions;
import cz.cuni.mff.ConfigMapper.ConfigFacade;
import cz.cuni.mff.ConfigMapper.Adapters.IniAdapter;
import cz.cuni.mff.ConfigMapper.LoadingMode;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.Map;

import static org.junit.Assert.assertEquals;

class StringInputStream extends ByteArrayInputStream {
	public StringInputStream(String input) {
		super(input.getBytes());
	}
}

/**
 * Created by teyras on 30.3.16.
 */
public class ConfigFacadeTest {
	class BasicMappedClass {
		@ConfigOption(section = "section1")
		public String optionString;

		@ConfigOption(section = "section1")
		public int optionInt;

		@ConfigOption(section = "section2")
		public boolean optionBool;
	}
	
	class BasicMappedRelaxedClass {
		@ConfigOption(section = "section1")
		public String optionString;

		@ConfigOption(section = "section2")
		public boolean optionBool;
		
		@UndeclaredOptions
		public Map<String,String> undeclaredOpts;
	}

	@Test
	public void loadIniBasic() throws Exception {
		StringInputStream input = new StringInputStream(
			"[section1]\n" +
				"optionString = value\n" +
				"[section2]\n" +
				"optionInt = 234\n" +
				"optionBool = true\n"
		);

		ConfigFacade<BasicMappedClass> facade = new ConfigFacade<>(BasicMappedClass.class, new IniAdapter());
		BasicMappedClass object = facade.load(input, LoadingMode.STRICT);

		assertEquals(object.optionString, "value");
		assertEquals(object.optionInt, 234);
		assertEquals(object.optionBool, true);
		
		ConfigFacade<BasicMappedRelaxedClass> facadeRelaxed = new ConfigFacade<>(BasicMappedRelaxedClass.class, new IniAdapter());
		BasicMappedRelaxedClass objectRelaxed = facadeRelaxed.load(input, LoadingMode.RELAXED);
		
		assertEquals(objectRelaxed.optionBool, true);
		assertEquals(objectRelaxed.optionString, "value");
		assertEquals(objectRelaxed.undeclaredOpts.get("sectionB#optionInt"), "234");
	}
	
	class NestedSectionMappedClass {
		class FooSection {
			@ConfigOption
			public String option;
		}

		@ConfigSection
		FooSection sectionA;

		@ConfigSection
		FooSection sectionB;
	}

	@Test
	public void loadIniNested() throws Exception {
		StringInputStream input = new StringInputStream(
			"[sectionA]\n" +
				"option = value\n" +
				"[sectionB]\n" +
				"option = value2\n"
		);

		ConfigFacade<NestedSectionMappedClass> facade = new ConfigFacade<>(NestedSectionMappedClass.class, new IniAdapter());
		NestedSectionMappedClass object = facade.load(input, LoadingMode.STRICT);

		assertEquals(object.sectionA.option, "value");
		assertEquals(object.sectionB.option, "value2");
	}
}