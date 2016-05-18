import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.Annotations.IntegralConstraint;
import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.MappingException;
import cz.cuni.mff.ConfigMapper.Nodes.ConfigRoot;
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
		mapper.save(object, null, false);
	}

	@Test(expected = MappingException.class)
	public void saveHighThrows() throws Exception {
		MappedObject object = new MappedObject();
		object.option = 100;

		ConfigMapper mapper = new ConfigMapper();
		mapper.save(object, null, false);
	}

	@Test()
	public void saveCorrect() throws Exception {
		MappedObject object = new MappedObject();
		object.option = 1;

		ConfigMapper mapper = new ConfigMapper();
		ConfigRoot config = mapper.save(object, null, false);

		ConfigRoot expected = new ConfigRoot("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option", "1")
			))
		));

		assertEquals(expected, config);
	}
}
