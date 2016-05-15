import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.Annotations.DecimalConstraint;
import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.LoadingMode;
import cz.cuni.mff.ConfigMapper.MappingException;
import cz.cuni.mff.ConfigMapper.Nodes.Root;
import cz.cuni.mff.ConfigMapper.Nodes.ScalarOption;
import cz.cuni.mff.ConfigMapper.Nodes.Section;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ConfigMapperLoadWithDecimalConstraintTest {
	static class MappedObject {
		@ConfigOption(section = "section")
		@DecimalConstraint(min = -10.333, max = 10.333)
		double option;
	}

	@Test(expected = MappingException.class)
	public void loadLowThrows() throws Exception {
		Root config = new Root("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option", "-100")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		MappedObject object = mapper.load(config, MappedObject.class, LoadingMode.STRICT);
	}

	@Test(expected = MappingException.class)
	public void loadHighThrows() throws Exception {
		Root config = new Root("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option", "100")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		MappedObject object = mapper.load(config, MappedObject.class, LoadingMode.STRICT);
	}

	@Test()
	public void loadCorrect() throws Exception {
		Root config = new Root("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option", "10.11")
			))
		));

		ConfigMapper mapper = new ConfigMapper();
		MappedObject object = mapper.load(config, MappedObject.class, LoadingMode.STRICT);

		Assert.assertEquals(10.11, object.option, 0.001);
	}
}