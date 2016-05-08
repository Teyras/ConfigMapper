import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.Annotations.UndeclaredOptions;
import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.LoadingMode;
import cz.cuni.mff.ConfigMapper.MappingException;
import cz.cuni.mff.ConfigMapper.Nodes.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
}