import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.Annotations.IntegralConstraint;
import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.MappingException;
import cz.cuni.mff.ConfigMapper.Nodes.Root;
import cz.cuni.mff.ConfigMapper.Nodes.ScalarOption;
import cz.cuni.mff.ConfigMapper.Nodes.Section;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ConfigMapperSaveWithIntegralConstraintTest {
	static class MappedObject {
		@ConfigOption(section = "section")
		@IntegralConstraint(min = -10, max = 10)
		int option;
	}

	@Test(expected = MappingException.class)
	public void saveLowThrows() throws Exception {
		MappedObject object = new MappedObject();
		object.option = -100;

		ConfigMapper mapper = new ConfigMapper();
		mapper.save(object, false);
	}

	@Test(expected = MappingException.class)
	public void saveHighThrows() throws Exception {
		MappedObject object = new MappedObject();
		object.option = 100;

		ConfigMapper mapper = new ConfigMapper();
		mapper.save(object, false);
	}

	@Test()
	public void saveCorrect() throws Exception {
		MappedObject object = new MappedObject();
		object.option = 1;

		ConfigMapper mapper = new ConfigMapper();
		Root config = mapper.save(object, false);

		Root expected = new Root("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option", "1")
			))
		));

		assertEquals(expected, config);
	}
}
