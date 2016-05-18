import cz.cuni.mff.ConfigMapper.Annotations.ConfigOption;
import cz.cuni.mff.ConfigMapper.Annotations.ConstantAlias;
import cz.cuni.mff.ConfigMapper.ConfigMapper;
import cz.cuni.mff.ConfigMapper.Nodes.ConfigRoot;
import cz.cuni.mff.ConfigMapper.Nodes.ScalarOption;
import cz.cuni.mff.ConfigMapper.Nodes.Section;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class ConfigMapperSaveEnumTest {
	static class WithEnum {
		enum OptionEnum {
			ON,
			OFF
		}

		@ConfigOption(section = "section")
		@ConstantAlias(constant = "OFF", alias = "OOFF")
		OptionEnum option1;

		@ConfigOption(section = "section")
		OptionEnum option2;
	}

	@Test
	public void saveEnum() throws Exception {
		WithEnum object = new WithEnum();
		object.option1 = WithEnum.OptionEnum.OFF;
		object.option2 = WithEnum.OptionEnum.ON;

		ConfigMapper mapper = new ConfigMapper();
		ConfigRoot config = mapper.save(object, null, false);

		ConfigRoot expected = new ConfigRoot("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option1", "OOFF"),
				new ScalarOption("option2", "ON")
			))
		));

		Assert.assertEquals(expected, config);
	}

	static class WithChainedEnum {
		enum OptionEnum {
			ON,
			OFF
		}

		@ConfigOption(section = "section")
		@ConstantAlias(constant = "OFF", alias = "ON")
		@ConstantAlias(constant = "ON", alias = "OFF")
		OptionEnum option1;

		@ConfigOption(section = "section")
		@ConstantAlias(constant = "OFF", alias = "ON")
		@ConstantAlias(constant = "ON", alias = "OFF")
		OptionEnum option2;
	}

	@Test
	public void saveChainedEnum() throws Exception {
		WithChainedEnum object = new WithChainedEnum();
		object.option1 = WithChainedEnum.OptionEnum.ON;
		object.option2 = WithChainedEnum.OptionEnum.OFF;

		ConfigMapper mapper = new ConfigMapper();
		ConfigRoot config = mapper.save(object, null, false);

		ConfigRoot expected = new ConfigRoot("", Arrays.asList(
			new Section("section", Arrays.asList(
				new ScalarOption("option1", "OFF"),
				new ScalarOption("option2", "ON")
			))
		));

		Assert.assertEquals(expected, config);
	}
}