import cz.cuni.mff.ConfigMapper.Annotations.UndeclaredOptions;
import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.LoadingMode;
import cz.cuni.mff.ConfigMapper.MappingException;
import cz.cuni.mff.ConfigMapper.Nodes.ConfigRoot;
import cz.cuni.mff.ConfigMapper.Nodes.ScalarOption;
import cz.cuni.mff.ConfigMapper.Nodes.Section;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ConfigMapperLoadRelaxedTest {
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

		ConfigRoot config = new ConfigRoot("config.ini", Arrays.asList(
			section1
		));

		// Instantiate the mapper
		ConfigMapper mapper = new ConfigMapper();
		WithUndeclaredOptions object = mapper.load(config, WithUndeclaredOptions.class, LoadingMode.RELAXED);

		// Check the loaded values
		Map<String, String> expected = new HashMap<>();
		expected.put("section1#option1", "value");
		expected.put("section1#option2", "10");

		Assert.assertEquals(expected, object.other);
	}

	static class MultipleUndeclaredOptionsFields {
		@UndeclaredOptions
		public Map<String, String> other1 = new HashMap<>();

		@UndeclaredOptions
		public Map<String, String> other2 = new HashMap<>();
	}

	@Test(expected = MappingException.class)
	public void loadRelaxedMultipleUndeclaredContainersThrows() throws Exception {
		ConfigRoot config = new ConfigRoot("config.ini", Collections.emptyList());

		// Instantiate the mapper
		ConfigMapper mapper = new ConfigMapper();
		MultipleUndeclaredOptionsFields object = mapper.load(config, MultipleUndeclaredOptionsFields.class, LoadingMode.RELAXED);
	}

	@Test(expected = MappingException.class)
	public void loadRelaxedNoUndeclaredContainersThrows() throws Exception {
		ConfigRoot config = new ConfigRoot("config.ini", Collections.emptyList());

		// Instantiate the mapper
		ConfigMapper mapper = new ConfigMapper();
		ConfigMapperLoadBasicTest.BasicMappedClass object = mapper.load(config, ConfigMapperLoadBasicTest.BasicMappedClass.class, LoadingMode.RELAXED);
	}
}