import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.Annotations.IntegralConstraint;
import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.LoadingMode;
import cz.cuni.mff.ConfigMapper.MappingException;
import cz.cuni.mff.ConfigMapper.Nodes.ConfigRoot;
import cz.cuni.mff.ConfigMapper.Nodes.ScalarOption;
import cz.cuni.mff.ConfigMapper.Nodes.Section;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ConfigMapperLoadWithIntegralConstraintTest {
	static class MappedObject {
		@ConfigOption(section = "section")
		@IntegralConstraint(min = -10, max = 10)
		int option;
	}

	@Test(expected = MappingException.class)
	public void loadLowThrows() throws Exception {
		ConfigRoot config = new ConfigRoot("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option", "-100")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		MappedObject object = mapper.load(config, MappedObject.class, LoadingMode.STRICT);
	}

	@Test(expected = MappingException.class)
	public void loadHighThrows() throws Exception {
		ConfigRoot config = new ConfigRoot("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option", "100")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		MappedObject object = mapper.load(config, MappedObject.class, LoadingMode.STRICT);
	}

	@Test()
	public void loadCorrect() throws Exception {
		ConfigRoot config = new ConfigRoot("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option", "2")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		MappedObject object = mapper.load(config, MappedObject.class, LoadingMode.STRICT);

		Assert.assertEquals(2, object.option);
	}
}